package Client.RMI;

import Client.UI.ModifyFriendlistStatus;
import javafx.application.Platform;

import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;

public class ChatClientImplementation extends RemoteObject implements ChatClient {
    @Override
    public void login(String whichClient) throws RemoteException {
        // the server has notified us that whichClient just logged in
        System.out.println("RMI received by client");
        ModifyFriendlistStatus login = new ModifyFriendlistStatus(whichClient, true);
        Platform.runLater(login);
    }

    @Override
    public void logout(String whichClient) throws RemoteException {
        // the server has notified us that whichClient just logged out
        System.out.println("RMI received by client");
        ModifyFriendlistStatus logout = new ModifyFriendlistStatus(whichClient, false);
        Platform.runLater(logout);
    }
}
