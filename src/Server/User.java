package Server;

import Client.RMI.ChatClient;
import Communication.Message;
import Server.RMI.LoginCallback;
import Server.RMI.ServerCallbackHandler;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.Socket;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ConcurrentHashMap;

public class User implements Serializable{
    private String name;
    private boolean isLogged;
    // friendList are those I'm friend with, registeredFriends those who are friend with me
    private ConcurrentHashMap<String, User> friendList, registeredFriends;
    private transient InetAddress currentUsrAddr;
    private transient Socket mySocket;
    private transient int myPort;
    private transient LoginCallback myServerCallback;

    public User(String name, Socket mySocket){
        this.name = name;
        this.friendList = new ConcurrentHashMap<String, User>();
        this.registeredFriends = new ConcurrentHashMap<String, User>();
        this.mySocket = mySocket;
        // the user InetAddress
        this.currentUsrAddr = mySocket.getInetAddress();
        this.myPort = mySocket.getPort() + 1;
        // register the user with the server's registry
        restoreCallback();
    }

    public void restoreCallback(){
        try {
            this.myServerCallback = (LoginCallback) UnicastRemoteObject.exportObject(new ServerCallbackHandler(this), 0);
            Core.registry.rebind(LoginCallback.OBJECT_NAME, myServerCallback);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public ConcurrentHashMap getMyRegisteredFriends(){
        return registeredFriends;
    }

    public void addWatcher(String username, User watcher){
        registeredFriends.put(username, watcher);
    }

    public void deleteWatcher(String username){
        registeredFriends.remove(username);
    }

    public String getName() {
        return name;
    }

    public Socket getMySocket() {
        return mySocket;
    }

    public boolean isFriendWith(String name){
        if (friendList.containsKey(name))
            return true;
        else
            return false;
    }

    // this is the port client and server will agree to connect on to exchange user messages
    // client opens a serversocket on this port
    // server attempts connection to this port
    public synchronized int getMyPort(){
        return myPort;
    }

    public synchronized void setMyPort(int myPort){
        this.myPort = myPort;
    }

    public ConcurrentHashMap<String, User> getFriendList() {
        return friendList;
    }

    // for direct communication between two clients when transferring files
    public InetAddress getCurrentUsrAddr() {
        return currentUsrAddr;
    }

    // requires the database of all users to work
    public void addFriend(String friendName, ConcurrentHashMap<String, User> Database){
        friendList.put(friendName, Database.get(friendName));
        // add myself to that User's registeredFriends list
        friendList.get(friendName).addWatcher(name, this);
    }

    // only requires the key for which to search
    public void removeFriend(String friendName){
        Message reply;
        if(friendList.containsKey(friendName)){
            // remove myself from the other User's registeredFriends list
            friendList.get(friendName).deleteWatcher(name);
            friendList.remove(friendName);
            reply = new Message("OP_OK", friendName + " removed from your friendlist.");
        }
        else{
            reply = new Message("OP_ERR", "No such user in your friendlist.");
        }
        reply.send(mySocket);
    }

    // send list of friends to client as a string, each element separated by a comma
    public String transmitFriendList(){
        String[] myFriends = friendList.keySet().toArray(new String[friendList.size()]);
        StringBuilder sendFriends = new StringBuilder();
        for(String i : myFriends){
            sendFriends.append(i).append(",");
        }
        return sendFriends.toString();
    }

    public synchronized boolean isLogged() { return isLogged; }

    public synchronized boolean login(Socket sock){
        if(isLogged)
            return false;
        isLogged = true;
        this.mySocket = sock;
        this.currentUsrAddr = sock.getInetAddress();
        // tell all those registered on my C that I'm online
        return true;
    }

    public synchronized void logout(){
        isLogged = false;
        // tell all those registered on my C that I'm offline
    }

    public ChatClient getMyChatClient() {
        try {
            return myServerCallback.getMyChatClient();
        } catch (RemoteException e) {
            e.printStackTrace();
            return null;
        }
    }
}
