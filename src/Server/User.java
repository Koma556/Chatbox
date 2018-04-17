package Server;

import Communication.Message;
import Server.UDP.ChatroomUDP;
import Server.UDP.ThreadWrapper;

import java.io.Serializable;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import static Server.Core.*;

public class User implements Serializable{
    private String name;
    private boolean isLogged;
    private HashMap<String, User> friendList;
    private String language;
    private transient InetAddress currentUsrAddr;
    private transient Socket mySocket;
    private transient int myPort, myNIOPort;
    transient ConcurrentHashMap<String, ChatConnectionWrapper> listOfConnections;
    private ArrayList<String> joinedGroups;

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }


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
        this.joinedGroups = new ArrayList<>();
    }

    public User(){
        this.friendList = new HashMap<String, User>();
    }

    public void createListOfConnections(){
        this.listOfConnections = new ConcurrentHashMap<>();
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
        if(this.isLogged())
            return currentUsrAddr;
        else
            return null;
    }

    // requires the database of all users to work
    public void addFriend(String friendName, ConcurrentHashMap<String, User> Database){
        friendList.put(friendName, Database.get(friendName));
    }

    // only requires the key for which to search
    public void removeFriend(String friendName){
        if(friendList.containsKey(friendName)){
            friendList.remove(friendName);
        }
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
        String[] tmp = joinedGroups.toArray(new String[joinedGroups.size()]);
        Message empty = new Message();
        if(tmp.length != 0) {
            for (String group: tmp
                 ) {
                leaveChatGroup(group, empty);
            }
        }
        joinedGroups = new ArrayList<>();
    }

    // start a ChatroomUDP thread and adds it to the control arrays in Core
    // also adds the chatroom ID to the list of currently joined groups for this user
    public boolean createChatGroup(String chatID){
        int portUDPin = -1;
        int portUDPout = -1;
        if(!chatroomsUDPWrapper.containsKey(chatID)){
            portUDPin = getNextUpdPort();
            portUDPout = getNextUpdPort();
            if((portUDPin != -1) && (portUDPout != -1)) {
                // add the control array variable first and foremost
                chatroomsUDPcontrolArray.put(chatID, true);
                // create new chatroom thread
                try {
                    DatagramSocket udpSocket = new DatagramSocket(portUDPin);
                    ChatroomUDP chatroom = new ChatroomUDP(chatID, portUDPin, portUDPout, udpSocket);
                    Thread chatroomThread = new Thread(chatroom);
                    chatroomThread.start();
                    System.out.println("Thread created and started for UPD chat room " + chatID);
                    // add said thread to the wrapper interface
                    ThreadWrapper wrapper = new ThreadWrapper(chatID, chatroomThread, this.getName(), portUDPin, portUDPout, udpSocket);
                    // writing down that this user is indeed part of this new group
                    wrapper.addUser(name, this);
                    joinedGroups.add(chatID);
                    // adding the new group's wrapper to the full list of groups
                    chatroomsUDPWrapper.put(chatID, wrapper);
                    return true;
                } catch (SocketException e) {
                    e.printStackTrace();
                    return false;
                }
            }
        }
        return false;
    }

    public void deleteChatGroup(String chatID, Message reply){
        if(chatroomsUDPWrapper != null && chatroomsUDPWrapper.containsKey(chatID)){
            if(chatroomsUDPWrapper.get(chatID).shutdownThread(name, false)) {
                reply.setFields("OP_OK", "Chatroom " + chatID + " deleted.");
            } else {
                reply.setFields("OP_ERR", "You are not the owner of Chatroom " + chatID + ".");
            }
        } else {
            reply.setFields("OP_ERR", "Chatroom " + chatID + " doesn't exist.");
        }
    }

    public void joinChatGroup(String chatID, Message reply){
        if(chatroomsUDPWrapper != null && chatroomsUDPWrapper.containsKey(chatID)) {
            if (this.isInGroup(chatID)) {
                reply.setFields("OP_ERR", "You're already registered with Chatroom " + chatID + ".");
            }else {
                joinedGroups.add(chatID);
                chatroomsUDPWrapper.get(chatID).addUser(name, this);
                reply.setFields("OP_OK", "Chatroom " + chatID + " joined.");
            }
        }else {
            reply.setFields("OP_ERR", "Chatroom " + chatID + " doesn't exist.");
        }
    }

    public void leaveChatGroup(String chatID, Message msg){
        if(chatroomsUDPWrapper != null && chatroomsUDPWrapper.containsKey(chatID)) {
            if (this.isInGroup(chatID)) {
                joinedGroups.remove(chatID);
                chatroomsUDPWrapper.get(chatID).removeUser(name);
                msg.setFields("OP_OK", "You were removed from group " + chatID);
            }else {
                msg.setFields("OP_ERR", "You were not registered in group " + chatID);
            }
        }else {
            msg.setFields("OP_ERR", "Group " + chatID + " doesn't exist.");
        }
    }

    private boolean isInGroup(String chatID){
        if(joinedGroups != null && joinedGroups.contains(chatID))
            return true;
        else
            return false;
    }


    public String getAllGroupsList(){
        StringBuilder listOfGroups = new StringBuilder();
        for (String chatID: chatroomsUDPWrapper.keySet()){
            listOfGroups.append(chatID);
            listOfGroups.append(",");
        }
        return listOfGroups.toString();
    }

    private synchronized int getNextUpdPort(){
        if(busyUDPports.size() == 65535) {
            // no free UDP ports
            return -1;
        }
        while(busyUDPports.contains(UDPport)){
            if(UDPport < 65535){
                UDPport++;
            }
            else{
                UDPport = 2000;
            }
        }
        return UDPport++;
    }

    public void muteLeaveChatGroup(String id) {
        if(joinedGroups != null && joinedGroups.contains(id)){
            joinedGroups.remove(id);
        }
    }

    public void setMyNIOPort(int i) {
        System.out.println(i);
        this.myNIOPort = i;
    }

    public int getMyNIOPort(){
        return myNIOPort;
    }
}
