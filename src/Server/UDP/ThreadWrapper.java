package Server.UDP;

import Server.User;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
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

    public ThreadWrapper(String id, Thread thread, String owner, int portIn, int portOut, DatagramSocket socket){
        this.id = id;
        this.thread = thread;
        this.owner = owner;
        this.portIn = portIn;
        this.portOut = portOut;
        this.registeredUsers = new ConcurrentHashMap<>();
        this.socket = socket;
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
                DatagramPacket packet = new DatagramPacket(
                        new byte[LENGTH], LENGTH);
                InetAddress multicastGroup = null;
                multicastGroup = InetAddress.getByName("239.1.1.1");
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

    public synchronized boolean shutdownThread(String caller){
        if(caller.equals(owner)) {
            // set control array to false, shutting off the UDP server
            chatroomsUDPcontrolArray.replace(id, false);
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
