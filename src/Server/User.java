package Server;

import Communication.Message;
import Server.ChatroomUDP.ExistingChatRoomException;
import Server.ChatroomUDP.MulticastChatroom;
import Server.ChatroomUDP.NoSuchChatRoomException;
import Server.ChatroomUDP.NoSuchUserException;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.Socket;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import static Server.Core.chatroomUDP;

public class User implements Serializable{
    private String name;
    private boolean isLogged;
    private HashMap<String, User> friendList;
    private transient InetAddress currentUsrAddr;
    private transient Socket mySocket;
    private transient int myPort;
    transient HashMap<String, MessageHandler> listOfConnections;
    transient HashMap<String, MulticastChatroom> myChatrooms;

    public void removeConnection(String connection){
        if(listOfConnections.containsKey(connection)) {
            listOfConnections.get(connection).closeConnection();
            listOfConnections.remove(connection);
        }
    }

    public User(String name, Socket mySocket){
        this.name = name;
        this.friendList = new HashMap<String, User>();
        this.mySocket = mySocket;
        // the user InetAddress
        this.currentUsrAddr = mySocket.getInetAddress();
        this.myPort = mySocket.getPort() + 1;
    }

    public User(){
        this.friendList = new HashMap<String, User>();
    }

    public void createListOfConnections(){
        this.listOfConnections = new HashMap<>();
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

    public HashMap<String, User> getFriendList() {
        return friendList;
    }

    // for direct communication between two clients when transferring files
    public InetAddress getCurrentUsrAddr() {
        return currentUsrAddr;
    }

    // requires the database of all users to work
    public void addFriend(String friendName, ConcurrentHashMap<String, User> Database){
        friendList.put(friendName, Database.get(friendName));
    }

    // only requires the key for which to search
    public void removeFriend(String friendName){
        Message reply;
        if(friendList.containsKey(friendName)){
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
        return true;
    }

    public void cleanupRMI(){
        try {
            Core.loginCaller.logout(name);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public synchronized void logout(){
        isLogged = false;
        // unregister user from chatrooms
        String[] tmp = myChatrooms.keySet().toArray(new String[myChatrooms.size()]);
        if(tmp.length != 0)
        for (String chatroom: tmp) {
            try {
                myChatrooms.get(chatroom).removeMe(name);
                myChatrooms.remove(chatroom);
            } catch (NoSuchUserException e) {
                // ignore
            }
        }
    }

    // creates a chatroom with chatID as its identifier
    // replies with an error if a chatroom with the same name already exists
    // adds the newly created chatroom to the list of active chatrooms for this user
    // registers the user with the newly created chatroom
    public void createChatroom(String chatID){
        Message reply;
        try {
            myChatrooms.put(chatID, chatroomUDP.create(chatID));
            myChatrooms.get(chatID).addMe(name, this);
            myChatrooms.get(chatID).setOwner(name);
            // send back the UDP port the server is opening its Socket on as a reply.
            reply = new Message("OP_OK", Integer.toString(myChatrooms.get(chatID).getPort()));
        } catch (ExistingChatRoomException e) {
            reply = new Message("OP_ERR", "Chatroom already exists.");
        }
        reply.send(mySocket);
    }

    public void addMe(String chatID){
        Message reply;
        if(!myChatrooms.containsKey(chatID)){
            try {
                myChatrooms.put(chatID, chatroomUDP.getChatroom(chatID));
                myChatrooms.get(chatID).addMe(name, this);
                reply = new Message("OP_OK", "Added to chatroom.");
            } catch (NoSuchChatRoomException e) {
                reply = new Message("OP_ERR", "Chatroom doesn't exist.");
            }
        } else
            reply = new Message("OP_ERR", "Already Registered with this Chatroom.");
        reply.send(mySocket);
    }

    public void removeMe(String chatID){
        Message reply;
        if(myChatrooms.containsKey(chatID)){
            try {
                myChatrooms.get(chatID).removeMe(name);
                myChatrooms.remove(chatID);
                reply = new Message("OP_OK", "Properly removed from Chatroom.");
            } catch (NoSuchUserException e){
                reply = new Message("OP_ERR", "You were never registered with that Chatroom!");
            }
        } else
            reply = new Message("OP_ERR", "No such chatroom in your registrations.");
    }

    // create a single string to send as data for the list of current chat rooms.
    // 1 means the User is already registered with that chatroom, 0 that it isn't.
    public void chatlist(){
        StringBuilder chatlist = new StringBuilder();
        for (String chatroom: chatroomUDP.chatlist()) {
            if(myChatrooms.containsKey(chatroom)){
                chatlist.append(chatroom);
                chatlist.append(",1;");
            } else {
                chatlist.append(chatroom);
                chatlist.append(",0;");
            }
        }
        Message reply = new Message("OP_CHT_LST", chatlist.toString());
        reply.send(mySocket);
    }

    public void closeChat(String chatID){
        Message reply;
        try {
            MulticastChatroom tmp = chatroomUDP.getChatroom(chatID);
            if(tmp.getOwner().equals(name)){
                chatroomUDP.closechat(chatID);
                reply = new Message ("OP_OK", "Chatroom Closed.");
            } else {
                reply = new Message ("OP_ERR", "You are not the Owner of this Chat Room.");
            }
        } catch (NoSuchChatRoomException e) {
            reply = new Message ("OP_ERR", "No Such Chatroom.");
        }
        reply.send(mySocket);
    }
}
