package Server.RMI;

import Client.RMI.ChatClient;
import Server.User;

import java.net.Socket;
import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;
import java.util.concurrent.ConcurrentHashMap;

public class ServerCallbackHandler extends RemoteObject implements LoginCallback {

    private ConcurrentHashMap<String, User> registeredFriends;
    public ChatClient myChatClient;
    private User user;
    private String name;

    public ServerCallbackHandler(User myUser){
        this.user = myUser;
        this.name = myUser.getName();
    }

    @Override
    public void login(ChatClient c) throws RemoteException {
        // update list of registered friends from the user himself
        this.myChatClient = c;
        this.registeredFriends = user.getMyRegisteredFriends();
        String[] myWatchers = registeredFriends.keySet().toArray(new String[registeredFriends.size()]);
        System.out.println(myWatchers);
        if(myWatchers != null) {
            for (String i : myWatchers) {
                try {
                    // call login on each ChatClient stub for all my friends
                    ChatClient tmpClient;
                    if((tmpClient = registeredFriends.get(i).getMyChatClient()) != null)
                        registeredFriends.get(i).getMyChatClient().login(name);
                } catch (RemoteException e) {
                    // do nothing
                }
            }
        }
    }

    @Override
    public void logout() throws RemoteException {
        // update list of registered friends from the user himself
        this.registeredFriends = user.getMyRegisteredFriends();
        String[] myWatchers = registeredFriends.keySet().toArray(new String[registeredFriends.size()]);
        if(myWatchers != null) {
            for (String i : myWatchers) {
                try {
                    // call logout on each ChatClient stub for all my friends
                    registeredFriends.get(i).getMyChatClient().logout(name);
                } catch (RemoteException e) {
                    // do nothing
                }
            }
        }
    }

    @Override
    public ChatClient getMyChatClient() throws RemoteException {
        return myChatClient;
    }
}
