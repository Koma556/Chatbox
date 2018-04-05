package Client.NIO;

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
            System.out.println("selecting...");
            while (true) {
                selector.selectedKeys().clear();
                selector.select();

                for (SelectionKey key : selector.selectedKeys()) {
                    if(key.isAcceptable()){
                        try{
                            SocketChannel client =((ServerSocketChannel)key.channel()).accept();
                            System.out.println("Got client");
                            client.configureBlocking(false);
                            ByteBuffer[] attachments = new ByteBuffer[2];
                            attachments[0] = ByteBuffer.allocate(Integer.BYTES);
                            attachments[1] = ByteBuffer.allocate(1024);
                            client.register(selector, SelectionKey.OP_READ, attachments);
                            System.out.println("New client accepted");
                        }catch (IOException e){
                            System.out.println("Error accepting client!");
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
                                    // replycode == 0
                                    buffer.putInt(0);
                                    attachment.add(fileName);
                                    client.register(selector, SelectionKey.OP_WRITE, attachment);
                                    System.out.println("Receiving file " + fileName);
                                }
                            }
                        } catch (IOException e) {
                            System.out.println("Error reading from client: " + e.getMessage());
                            key.cancel();
                        }
                    }
                    if (key.isWritable()){
                        try
                        {
                            SocketChannel client =(SocketChannel) key.channel();
                            ArrayList<Object> attachment = (ArrayList<Object>) key.attachment();
                            ByteBuffer buffer = (ByteBuffer) attachment.get(0);
                            buffer.flip();
                            // this never exits
                            client.write(buffer);

                            String fileName = (String) attachment.get(1);
                            System.out.println(fileName);

                            FileChannel fileChannel = new FileOutputStream("./"+fileName).getChannel();
                            long retval = 1;
                            long position = 0;
                            // receives from the SocketChannel client until there are no more bytes to read
                            // saves to fileChannel
                            while(retval != 0){
                                retval = fileChannel.transferFrom(client, position, Long.MAX_VALUE);
                                position += retval;
                            }
                        } catch (IOException e){
                            System.out.println("Error writing to client: " + e.getMessage());
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

