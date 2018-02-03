package Communication;

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

    public User(String name, Socket mySocket){
        this.name = name;
        this.friendList = new HashMap<String, User>();
        this.mySocket = mySocket;
        // the user InetAddress
        this.currentUsrAddr = mySocket.getInetAddress();
        this.myPort = mySocket.getPort() + 1;
    }

    public User(String name){
        this.name = name;
        this.friendList = new HashMap<String, User>();
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
    public int getMyPort(){
        if(myPort == mySocket.getPort()) {
            myPort++;
        }
        if(myPort <= 65535) {
            int tmp = myPort;
            myPort++;
            return tmp;
        }
        else
            return 49152;
    }

    public HashMap<String, User> getFriendList() {
        return friendList;
    }

    // for direct communication between two clients when transferring files
    public InetAddress getCurrentUsrAddr() {
        return currentUsrAddr;
    }

    // requires the database of all users to work
    public void addFriend(String friendName, ConcurrentHashMap<String, User> Database){
        Message reply;
        if(friendList.containsKey(friendName)){
            reply = new Message("OP_PRT_MSG", "Already friend with "+friendName);
        }
        else if(Database.containsKey(friendName)){
            friendList.put(friendName, Database.get(friendName));
            reply = new Message("OP_PRT_MSG", friendName + " added to friends!");
        }
        else{
            reply = new Message("OP_PRT_MSG", "No such user on this network.");
        }
        reply.send(mySocket);
    }

    // only requires the key for which to search
    public void removeFriend(String friendName){
        Message reply;
        if(friendList.containsKey(friendName)){
            friendList.remove(friendName);
            reply = new Message("OP_PRT_MSG", friendName + " removed from your friendlist.");
        }
        else{
            reply = new Message("OP_PRT_MSG", "No such user in your friendlist.");
        }
        reply.send(mySocket);
    }

    // send list of friends to client as a string, each element separated by a comma
    public void transmitFriendList(){
        String[] myFriends = friendList.keySet().toArray(new String[friendList.size()]);
        StringBuilder sendFriends = new StringBuilder();
        for(String i : myFriends){
            sendFriends.append(i).append(", ");
        }
        Message reply = new Message("OP_GET_LST", sendFriends.toString());
        reply.send(mySocket);
    }

    public synchronized boolean isLogged() { return isLogged; }

    // TODO: RMI Callback for the other clients
    public synchronized boolean login(){
        if(isLogged)
            return false;
        isLogged = true;
        return true;
    }

    // TODO: RMI Callback for the other clients
    public synchronized void logout(){
        isLogged = false;
    }

}
