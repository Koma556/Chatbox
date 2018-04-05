package Client.NIO;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;

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
                selector.select();

                for (SelectionKey key : selector.selectedKeys())
                {
                    if (key.isAcceptable()){
                        try{
                            ServerSocketChannel channel = (ServerSocketChannel) key.channel();
                            SocketChannel client= channel.accept();

                            String destination = "./tmp.bin";
                            client.configureBlocking(false);

                            ByteBuffer length= ByteBuffer.allocate(Integer.BYTES);
                            ByteBuffer message= ByteBuffer.allocate(1024);
                            ByteBuffer[] bfs = {length, message};
                            long response=client.read(bfs);
                            if (response==-1){
                                client.close();
                                key.cancel();
                                continue;
                            }
                            if (!bfs[0].hasRemaining()){
                                bfs[0].flip();
                                int l=bfs[0].getInt();
                                if(bfs[1].position()==l){
                                    bfs[1].flip();
                                    client.register(selector, SelectionKey.OP_READ, bfs[1]);
                                }
                            }
                        } catch(IOException e){
                            e.printStackTrace();
                        }
                    }
                    if (key.isReadable()){
                        try{
                            SocketChannel channel= (SocketChannel) key.channel();
                            ByteBuffer message= (ByteBuffer)key.attachment();
                            String destination = new String(((ByteBuffer) key.attachment()).array(), Charset.forName("UTF-8"));
                            //System.out.println("Destination is :" + destination);
                            FileChannel fileChannel = new FileOutputStream(destination).getChannel();
                            long retval = 1;
                            long position = 0;
                            // receives from the SocketChannel client until there are no more bytes to read
                            // saves to fileChannel
                            while(retval != 0){
                                retval = fileChannel.transferFrom(channel, position, Long.MAX_VALUE);
                                position += retval;
                            }
                            key.cancel();
                            channel.close();
                            fileChannel.close();
                        } catch(IOException e){
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch( IOException e){
            //System.out.println("NIO Listener closing.");
        }
    }
}
