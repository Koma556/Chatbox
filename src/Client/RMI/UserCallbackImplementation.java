package Client.RMI;

import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;

public class UserCallbackImplementation extends RemoteObject implements UserCallback {

    public UserCallbackImplementation(){
    }

    @Override
    public void hasLoggedIn(String name) throws RemoteException {
        System.out.println(name+" has logged in.");
    }

    @Override
    public void hasLoggedOut(String name) throws RemoteException {
        System.out.println(name+" has logged out.");
    }
}
