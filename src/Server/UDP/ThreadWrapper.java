package Server.UDP;

import Server.User;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import static Server.Core.busyUDPports;
import static Server.Core.chatroomsUDPWrapper;
import static Server.Core.chatroomsUDPcontrolArray;


public class ThreadWrapper {
    public final int LENGTH=512;
    private int portIn, portOut;
    private Thread thread;
    private String id, owner;
    private ConcurrentHashMap<String, User> registeredUsers;
    private DatagramSocket socket;
    private InetAddress multicastGroup = null;

    public ThreadWrapper(String id, Thread thread, String owner, int portIn, int portOut, DatagramSocket socket){
        this.id = id;
        this.thread = thread;
        this.owner = owner;
        this.portIn = portIn;
        this.portOut = portOut;
        this.registeredUsers = new ConcurrentHashMap<>();
        this.socket = socket;
        try {
            multicastGroup = InetAddress.getByName("239.1.1.1");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public boolean hasUser(String name){
        if(registeredUsers.containsKey(name))
            return true;
        else
            return false;
    }

    public boolean addUser(String name, User user){
        if(!this.hasUser(name)) {
            registeredUsers.put(name, user);
            return true;
        }else{
            return false;
        }
    }

    public boolean removeUser(String name){
        if(this.hasUser(name)) {
            try{
                registeredUsers.remove(name);
                String goodbye = "-User " + name + " left the group-";
                DatagramPacket multicastPacket = new DatagramPacket(goodbye.getBytes("UTF-8"),
                        0,
                        goodbye.getBytes("UTF-8").length,
                        multicastGroup, portOut);
                socket.send(multicastPacket);
            } catch (UnknownHostException e) {
                e.printStackTrace();
                return false;
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return false;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }else {
            return false;
        }
    }

    public String getPorts(){
        return portIn+":"+portOut;
    }

    // proper way to shut down a UDP chat room, it will also take care to clean up the two support maps
    public synchronized boolean shutdownThread(String caller, boolean isServer){
        if(caller.equals(owner) || isServer) {
            // set control array to false, shutting off the UDP server
            chatroomsUDPcontrolArray.replace(id, false);
            // Goodbye message to all clients
            try {
                String goodbye = "-Server Closing the Chatroom-";
                DatagramPacket multicastPacket =
                        new DatagramPacket(goodbye.getBytes("UTF-8"),
                                0,
                                goodbye.getBytes("UTF-8").length,
                                multicastGroup, portOut);
                socket.send(multicastPacket);
                // close on the socket, throws an exception in the ChatroomUDP thread for this room which I then catch and use to quit
                socket.close();
                // remove all users
                String[] usernames = registeredUsers.keySet().toArray(new String[registeredUsers.size()]);
                ArrayList<User> tmpList = new ArrayList<>();
                for (String user:
                     usernames) {
                    tmpList.add(registeredUsers.get(user));
                }
                for (User user:
                        tmpList) {
                    user.muteLeaveChatGroup(id);
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            // clears up the port
            clearUDPport(portIn);
            clearUDPport(portOut);
            // remove this object from the list of active UDP servers
            chatroomsUDPWrapper.remove(id);
            return true;
        }
        else
            return false;
    }

    private synchronized void clearUDPport(Integer port){
        busyUDPports.remove(port);
    }

    public Thread getThread(){
        return thread;
    }

}
