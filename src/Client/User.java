package Client;

import Communication.Message;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class User implements Serializable{
    private String name;
    private boolean isLogged;
    private HashMap<String, User> friendList;
    private transient InetAddress currentUsrAddr;
    private transient Socket mySocket;
    private transient int myPort;
    private String[] tmpFriendList;

    public void setTmpFriendList(String[] list) {
        this.tmpFriendList = list;
    }

    public String[] getTmpFriendList() {
        return tmpFriendList;
    }

    public User(){
        this.friendList = new HashMap<String, User>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setMySocket(Socket sock){
        this.mySocket = sock;
        this.currentUsrAddr = sock.getInetAddress();
    }

    public Socket getMySocket() {
        return mySocket;
    }
}
