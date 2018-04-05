package Client.NIO;

import Client.UI.NIOui.ReceiveConfirmation;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import static Client.UI.TestUI.myUser;
import static Client.UI.TestUI.sessionNIOPort;

public class FileReceiverServer implements Runnable{
    public final static int BLOCK_SIZE = 1024;

    @Override
    public void run() {
        try (ServerSocketChannel server = ServerSocketChannel.open();
             Selector selector = Selector.open()) {

            server.bind(new InetSocketAddress("localhost", sessionNIOPort));
            server.configureBlocking(false);

            myUser.setMyNIO(selector);

            server.register(selector, SelectionKey.OP_ACCEPT);
            //System.out.println("selecting...");
            while (true) {
                selector.selectedKeys().clear();
                selector.select();

                for (SelectionKey key : selector.selectedKeys()) {
                    if(key.isAcceptable()){
                        try{
                            SocketChannel client =((ServerSocketChannel)key.channel()).accept();
                            //System.out.println("Got client");
                            client.configureBlocking(false);
                            ByteBuffer[] attachments = new ByteBuffer[2];
                            attachments[0] = ByteBuffer.allocate(Integer.BYTES);
                            attachments[1] = ByteBuffer.allocate(1024);
                            client.register(selector, SelectionKey.OP_READ, attachments);
                            //System.out.println("New client accepted");
                        }catch (IOException e){
                            //System.out.println("Error accepting client!");
                            e.printStackTrace();
                        }
                    }
                    if (key.isReadable()) {
                        try {
                            SocketChannel client = (SocketChannel) key.channel();
                            ByteBuffer[] buffers = (ByteBuffer[]) key.attachment();
                            client.read(buffers);
                            if (!buffers[0].hasRemaining()) {
                                buffers[0].flip();
                                int length = buffers[0].getInt();
                                if (length == buffers[1].position()) {
                                    String fileName = new String(buffers[1].array(), 0, buffers[1].position()).trim();
                                    ByteBuffer buffer = ByteBuffer.allocate(BLOCK_SIZE);
                                    ArrayList<Object> attachment = new ArrayList<>();
                                    attachment.add(buffer);
                                    // replycode = 0 means the user accepts the file
                                    // replycode = 1 means it has been refused
                                    ReceiveConfirmation confirmDialog = new ReceiveConfirmation(fileName);
                                    FutureTask query = new FutureTask(confirmDialog);
                                    Platform.runLater(query);
                                    boolean go = false;
                                    String savePath = (String) query.get();
                                    if(savePath != null) {
                                        go = true;
                                        buffer.putInt(0);
                                    }else{
                                        buffer.putInt(1);
                                    }
                                    buffer.flip();
                                    client.write(buffer);
                                    attachment.add(fileName);
                                    attachment.add(savePath);
                                    if(go) {
                                        client.register(selector, SelectionKey.OP_WRITE, attachment);
                                    }else{
                                        key.cancel();
                                    }
                                    //System.out.println("Receiving file " + fileName);

                                }
                            }
                        } catch (IOException e) {
                            //System.out.println("Error reading from client: " + e.getMessage());
                            key.cancel();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        }
                    }
                    if (key.isWritable()){
                        try
                        {
                            SocketChannel client =(SocketChannel) key.channel();
                            ArrayList<Object> attachment = (ArrayList<Object>) key.attachment();
                            //ByteBuffer buffer = (ByteBuffer) attachment.get(0);
                            String fileName = (String) attachment.get(1);
                            String savePath = (String) attachment.get(2);
                            System.out.println("Saving file to: "+savePath+fileName);

                            // TODO: Let user choose if they want to receive the file and let user choose where to (use the replycode)
                            FileChannel fileChannel = new FileOutputStream(savePath+fileName).getChannel();
                            long retval = 1;
                            long position = 0;
                            // receives from the SocketChannel client until there are no more bytes to read
                            // saves to fileChannel
                            while(retval != 0){
                                retval = fileChannel.transferFrom(client, position, Long.MAX_VALUE);
                                position += retval;
                            }
                            key.cancel();
                        } catch (IOException e){
                            //System.out.println("Error writing to client: " + e.getMessage());
                            e.printStackTrace();
                            key.cancel();
                        }
                    }
                }
            }
        } catch (ClosedChannelException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

