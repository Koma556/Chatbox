package Server.RMI;

import Client.RMI.UserCallback;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface CallbackInterface extends Remote {
    public static final String OBJECT_NAME="LOGIN_CALLBACK";

    public void login(UserCallback c, String name) throws RemoteException;
    public void logout(String name) throws RemoteException;
}
