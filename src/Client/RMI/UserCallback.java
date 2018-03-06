package Client.RMI;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface UserCallback extends Remote {
    public void hasLoggedIn(String name) throws RemoteException;
    public void hasLoggedOut(String name) throws RemoteException;
}
