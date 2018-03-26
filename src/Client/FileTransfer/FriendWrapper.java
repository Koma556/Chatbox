package Client.FileTransfer;

import java.net.InetAddress;

public class FriendWrapper {
    private InetAddress address;
    private int port;
    private String name;

    public FriendWrapper(InetAddress address, int port, String name){
        this.address = address;
        this.name = name;
        this.port = port;
    }

    public InetAddress getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    public String getName() {
        return name;
    }
}
