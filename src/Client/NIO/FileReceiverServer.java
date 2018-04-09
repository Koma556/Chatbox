package Client.NIO;

import Client.UI.NIOui.ReceiveConfirmation;
import Client.UI.PopupWindows.Alerts;
import javafx.application.Platform;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import static Client.UI.CoreUI.myUser;
import static Client.UI.CoreUI.sessionNIOPort;

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
            while (true) {
                selector.selectedKeys().clear();
                selector.select();

                for (SelectionKey key : selector.selectedKeys()) {
                    if(key.isValid() && key.isAcceptable()){
                        try{
                            SocketChannel client =((ServerSocketChannel)key.channel()).accept();
                            // a new client is trying to connect
                            client.configureBlocking(false);
                            ByteBuffer[] attachments = new ByteBuffer[3];
                            // allocate 2 byte buffers for size of the filename and filename
                            attachments[0] = ByteBuffer.allocate(Integer.BYTES);
                            attachments[1] = ByteBuffer.allocate(Integer.BYTES);
                            attachments[2] = ByteBuffer.allocate(1024);
                            client.register(selector, SelectionKey.OP_READ, attachments);
                        }catch (IOException e){
                            e.printStackTrace();
                        }
                    }
                    if (key.isValid() && key.isReadable()) {
                        try {
                            SocketChannel client = (SocketChannel) key.channel();
                            // recover the buffers I allocated earlier from the attachments
                            ByteBuffer[] buffers = (ByteBuffer[]) key.attachment();
                            client.read(buffers);
                            // from buffer[2] I will recover username of the user contacting me and filename of the file he's sending
                            // but I will recover the SIZE of those names from buffer[0] and buffer[1] respectively
                            int unameL = 0;
                            if (!buffers[1].hasRemaining()) {
                                buffers[1].flip();
                                unameL = buffers[1].getInt();
                            }
                            if (!buffers[0].hasRemaining()) {
                                buffers[0].flip();
                                int length = buffers[0].getInt();
                                // once obtained both lengths, start reading on buffer[2] until position == lengths
                                if (length+unameL == buffers[2].position()) {
                                    // obtain filename reading from start to length
                                    String fileName = new String(buffers[2].array(), 0, length).trim();
                                    String uName = null;
                                    // obtain username reading from where the filename ends
                                    if (unameL == buffers[2].position() - length) {
                                        uName = new String(buffers[2].array(), length, buffers[2].position()).trim();
                                    }
                                    ByteBuffer buffer = ByteBuffer.allocate(BLOCK_SIZE);
                                    ArrayList<Object> attachment = new ArrayList<>();
                                    // adding the buffer to attachments, for legacy reasons
                                    attachment.add(buffer);
                                    // replycode = 0 means the user accepts the file
                                    // replycode = 1 means it has been refused
                                    ReceiveConfirmation confirmDialog = new ReceiveConfirmation(fileName, uName);
                                    // run a confirmation dialog for the user to accept or refuse the incoming file
                                    FutureTask query = new FutureTask(confirmDialog);
                                    Platform.runLater(query);
                                    boolean go = false;
                                    String savePath = null;
                                    // if user accepted, reply with code 0, otherwise reply with code 1
                                    if(query.get() != null) {
                                        savePath = (String) query.get();
                                        go = true;
                                        buffer.putInt(0);
                                    }else{
                                        buffer.putInt(1);
                                    }
                                    // tell whoever is sending me this file my reply
                                    buffer.flip();
                                    client.write(buffer);
                                    // if I accepted the file move on to the next step
                                    // filename and savepath are added as attachments to the key
                                    // I have my user select a filepath in the confirmation window
                                    if(go) {
                                        attachment.add(fileName);
                                        attachment.add(savePath);
                                        client.register(selector, SelectionKey.OP_WRITE, attachment);
                                    }
                                    // otherwise, we cancel the key
                                    else{
                                        key.cancel();
                                    }
                                }
                            }
                        } catch (IOException e) {
                            key.cancel();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            key.cancel();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                            key.cancel();
                        }
                    }
                    if (key.isValid() && key.isWritable()){
                        try
                        {
                            SocketChannel client =(SocketChannel) key.channel();
                            // get filename and filepath out of the attachments
                            ArrayList<Object> attachment = (ArrayList<Object>) key.attachment();
                            String fileName = (String) attachment.get(1);
                            String savePath = (String) attachment.get(2);
                            // open a new file on my system at filepath/filename
                            FileChannel fileChannel = new FileOutputStream(savePath+"/"+fileName).getChannel();
                            long retval = 1;
                            long position = 0;
                            // receives from the SocketChannel client until there are no more bytes to read
                            // saves to fileChannel
                            while(retval != 0){
                                retval = fileChannel.transferFrom(client, position, Long.MAX_VALUE);
                                position += retval;
                            }
                            // closing the receiving FileChannel.
                            // this is very important, as without doing so
                            // following files won't transfer properly
                            fileChannel.close();
                            key.cancel();
                            Alerts alert = new Alerts("Transfer Complete", fileName + " has been saved to disk.", "File saved to: " + savePath);
                            Platform.runLater(alert);
                        } catch (IOException e){
                            e.printStackTrace();
                            key.cancel();
                        }
                    }
                }
            }
        } catch (ClosedChannelException e) {
            e.printStackTrace();
        } catch (ClosedSelectorException e) {
            // this exception is caught whenever we log out
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}

