package Client.RMI;

import Client.UI.ModifyFriendlistStatus;
import javafx.application.Platform;

import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;

import static Client.UI.Controller.usrs;

public class UserCallbackImplementation extends RemoteObject implements UserCallback {

    public UserCallbackImplementation(){
    }

    @Override
    public void hasLoggedIn(String name) throws RemoteException {
        //System.out.println(name+" has logged in.");
        ModifyFriendlistStatus login = new ModifyFriendlistStatus(name, true);
        Platform.runLater(login);
    }

    @Override
    public void hasLoggedOut(String name) throws RemoteException {
        //System.out.println(name+" has logged out.");
        ModifyFriendlistStatus logout = new ModifyFriendlistStatus(name, false);
        Platform.runLater(logout);
    }
}
