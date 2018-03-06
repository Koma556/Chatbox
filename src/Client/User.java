package Client;

import Client.RMI.UserCallback;
import Client.RMI.UserCallbackImplementation;
import Communication.Message;
import Server.RMI.CallbackInterface;
import Server.RMI.LoginCallback;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
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
    private UserCallback userCallback, userStub;
    private CallbackInterface callbackInterface;

    public void setTmpFriendList(String[] list) {
        this.tmpFriendList = list;
    }

    public String[] getTmpFriendList() {
        return tmpFriendList;
    }

    public User(){
        this.friendList = new HashMap<String, User>();
    }

    // locks the remote registry and logs into it
    public void lockRegistry(){
        userCallback = new UserCallbackImplementation();
        try {
            userStub = (UserCallback) UnicastRemoteObject.exportObject(userCallback, 0);
            callbackInterface = (CallbackInterface) LocateRegistry.getRegistry().lookup(LoginCallback.OBJECT_NAME);
            callbackInterface.login(userStub, name);
        } catch (RemoteException e){
            System.out.println("Remote Exception when connecting to the registry.");
            System.exit(1);
        } catch (NotBoundException e) {
            System.out.println("Not Bound Exception when connecting to the registry.");
            System.exit(1);
        }
    }

    public void unlockRegistry(){
        if(callbackInterface != null) {
            try {
                callbackInterface.logout(name);
            } catch (RemoteException e) {
                System.out.println("RemoteException while logging out.");
            } finally {
                try{
                    UnicastRemoteObject.unexportObject(userCallback, true);
                } catch (NoSuchObjectException e) {
                    System.out.println("Counld not unexport :"+e.getMessage());
                }
            }
        }
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
