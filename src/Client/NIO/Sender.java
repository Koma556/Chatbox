package Client.NIO;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class Sender implements Runnable {
    private InetAddress target;
    private int port;
    private String filePath, fileName;

    public Sender(InetAddress target, int port, String filePath, String fileName){
        this.target = target;
        this.port = port;
        this.filePath = filePath;
        this.fileName = fileName;
    }

    @Override
    public void run() {
        SocketAddress server = new InetSocketAddress(target,port);
        RandomAccessFile file = null;
        boolean error = false;
        try {
            file = new RandomAccessFile(filePath, "rw");
        } catch (FileNotFoundException e) {
            error = true;
        }
        if(!error) {
            try (FileChannel fileChannel = file.getChannel();
                 ReadableByteChannel in = Channels.newChannel(System.in);
                 SocketChannel client = SocketChannel.open(server)) {
                /*
                ByteBuffer message= ByteBuffer.allocate(1024);
                message.wrap(fileName.getBytes("UTF-8"));
                message.flip();
                ByteBuffer length = ByteBuffer.allocate(Integer.BYTES);
                length.putInt(message.limit());
                length.flip();
                socketChannel.write(length);
                socketChannel.write(message);
                message.clear();
                */
                ByteBuffer buffer = ByteBuffer.allocate(1024);
                /*

                in.read(buffer);
                String fileName = "downloaded_" +
                        new String(buffer.array(), 0, buffer.position()).trim();
                int nameLength = buffer.position();
                */
                int nameLength = fileName.getBytes().length;
                ByteBuffer lengthBuffer = ByteBuffer.allocate(Integer.BYTES);
                lengthBuffer.putInt(nameLength);
                lengthBuffer.flip();
                client.write(lengthBuffer);
                buffer.flip();
                client.write(buffer);
                buffer.clear();
                ByteBuffer[] bufferArray = new ByteBuffer[2];
                bufferArray[0] = ByteBuffer.allocate(Integer.BYTES);
                bufferArray[1] = buffer;
                FileChannel out = null;
                int responseCode = -1;
                while(client.read(bufferArray) != -1){
                    if(responseCode == -1){
                        if(!bufferArray[0].hasRemaining()){
                            // set the response code
                            bufferArray[0].flip();
                            responseCode = bufferArray[0].getInt();
                        }
                    }
                    if (responseCode == 0) { // ready to receive file
                        fileChannel.transferTo(0, fileChannel.size(), client);
                    }
                    if (responseCode == 1){
                        throw new NoSuchFileException("Connection refused?");
                    }
                }
                out.close();
                System.out.println("Finished Transferring File.");

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
