package Server.RMI;

import Client.RMI.ChatClient;

import java.net.Socket;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;

public interface LoginCallback extends Remote {
    public static final String OBJECT_NAME = "LOGIN_CALLBACK";

    public void login(ChatClient c) throws RemoteException;
    public void logout() throws RemoteException;
    /*
    // call this when logging
    public void register(ChatClient c) throws RemoteException;
    // call this when logging out
    public void unregister() throws RemoteException;
    */
    // I'm sorry
    public ChatClient getMyChatClient() throws RemoteException;
}
