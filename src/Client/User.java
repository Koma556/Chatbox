package Client;

import Client.RMI.ChatClient;
import Communication.Message;
import Server.RMI.LoginCallback;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class User implements Serializable{
    private String name;
    private boolean isLogged;
    private HashMap<String, User> friendList;
    private transient InetAddress currentUsrAddr;
    private transient Socket mySocket;
    private transient int myPort;
    private ArrayList<String> ArrayFriendList;
    private String[] tmpFriendList;
    private LoginCallback callback;
    private ChatClient chatClientStub;

    public void setTmpFriendList(String[] list) {
        this.tmpFriendList = list;
        this.ArrayFriendList = new ArrayList<>(Arrays.asList(tmpFriendList));
    }

    public ArrayList<String> getArrayFriendList() {
        return ArrayFriendList;
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

    public void setChatClient(ChatClient stub){
        this.chatClientStub = stub;
    }

    public ChatClient getChatClientStub() {
        return chatClientStub;
    }

    public void setLoginCallback(LoginCallback callback){
        this.callback = callback;
    }

    public LoginCallback getCallback() {
        return callback;
    }

    public Socket getMySocket() {
        return mySocket;
    }
}
