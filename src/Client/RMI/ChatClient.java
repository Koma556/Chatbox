package Client.RMI;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ChatClient extends Remote {
    public void login(String whichClient) throws RemoteException;
    public void logout(String whichClient) throws RemoteException;
}
