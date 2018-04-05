package Client.NIO;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;

public class Sender implements Runnable {
    private InetAddress target;
    private int port;
    private String filePath;

    public Sender(InetAddress target, int port, String filePath){
        this.target = target;
        this.port = port;
        this.filePath = filePath;
    }

    @Override
    public void run() {
        System.out.println("I'm the Sender process.");
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
                 SocketChannel socketChannel = SocketChannel.open(server)) {
                fileChannel.transferTo(0, fileChannel.size(), socketChannel);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Sender process dying.");
    }
}
