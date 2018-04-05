package Client.NIO;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;

import static Client.UI.TestUI.myUser;
import static Client.UI.TestUI.sessionNIOPort;

public class ListenerNIO implements Runnable {
    @Override
    public void run() {
        try(ServerSocketChannel server = ServerSocketChannel.open();
            Selector selector = Selector.open()){

            server.bind(new InetSocketAddress("localhost", sessionNIOPort));
            server.configureBlocking(false);

            myUser.setMyNIO(selector);

            server.register(selector, SelectionKey.OP_ACCEPT);

            while(true){
                selector.selectedKeys().clear();
                System.out.println("selecting...");
                selector.select();

                for (SelectionKey key : selector.selectedKeys())
                {
                    if (key.isAcceptable()){
                        try{
                            ServerSocketChannel channel = (ServerSocketChannel) key.channel();
                            SocketChannel client= channel.accept();
                            // open controller to pick where to save the file
                            // return the path
                            String destination = "./tmp.bin";
                            System.out.println("New client "+ client.getRemoteAddress());
                            client.configureBlocking(false);
                            FileChannel fileChannel = new FileOutputStream(destination).getChannel();
                            long retval = 1;
                            long position = 0;
                            // receives from the SocketChannel client until there are no more bytes to read
                            // saves to fileChannel
                            while(retval != 0){
                                retval = fileChannel.transferFrom(client, position, Long.MAX_VALUE);
                                position += retval;
                            }
                            key.cancel();
                            channel.close();
                            fileChannel.close();
                            System.out.println("Saved file to "+ destination);
                        } catch(IOException e){
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch( IOException e){
            System.out.println("NIO Listener closing.");
        }
    }
}
