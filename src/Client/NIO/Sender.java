package Client.NIO;

import Client.UI.PopupWindows.Alerts;
import javafx.application.Platform;

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
                 SocketChannel client = SocketChannel.open(server)) {
                ByteBuffer buffer = ByteBuffer.allocate(1024);
                /*

                in.read(buffer);
                String fileName = "downloaded_" +
                        new String(buffer.array(), 0, buffer.position()).trim();
                int nameLength = buffer.position();
                */
                // add fileName as bytes in UTF-8 encoding to buffer
                buffer.put(fileName.getBytes());
                int nameLength = buffer.position();
                ByteBuffer lengthBuffer = ByteBuffer.allocate(Integer.BYTES);
                lengthBuffer.putInt(nameLength);
                lengthBuffer.flip();
                client.write(lengthBuffer);
                buffer.flip();
                client.write(buffer);
                buffer.clear();
                ByteBuffer[] bufferArray = new ByteBuffer[1];
                bufferArray[0] = ByteBuffer.allocate(Integer.BYTES);
                int responseCode = -1;
                long retVal = 0;
                while(client.read(bufferArray) != -1 && retVal < fileChannel.size()){
                    if(responseCode == -1){
                        if(!bufferArray[0].hasRemaining()){
                            // set the response code
                            bufferArray[0].flip();
                            responseCode = bufferArray[0].getInt();
                        }
                    }
                    if (responseCode == 0) { // ready to receive file
                        System.out.println("Starting to send.");
                        retVal =+ fileChannel.transferTo(0, fileChannel.size(), client);
                    }
                    if (responseCode == 1){
                        fileChannel.close();
                    }
                }
                fileChannel.close();
                System.out.println("Finished Transferring File.");

            } catch (IOException e) {
                Alerts alert = new Alerts("Transfer Refused", "Friend refused your file.", "The connection was refused by your peer.");
                Platform.runLater(alert);
            }
        }
    }
}
