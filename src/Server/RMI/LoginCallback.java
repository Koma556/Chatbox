package Server.RMI;

import Client.RMI.UserCallback;
import Server.User;

import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;
import java.util.concurrent.ConcurrentHashMap;

public class LoginCallback extends RemoteObject implements CallbackInterface {
    ConcurrentHashMap<String, UserCallback> registeredUsers;
    ConcurrentHashMap<String, User> userDatabase;
    String[] loggedUsers;

    public LoginCallback(ConcurrentHashMap userDatabase){
        this.registeredUsers = new ConcurrentHashMap<>();
        this.userDatabase = userDatabase;
    }

    // Send over all online statuses when requiring a friendlist update
    @Override
    public void update(String name) throws RemoteException {
        loggedUsers = registeredUsers.keySet().toArray(new String[registeredUsers.size()]);
        User myTmp = userDatabase.get(name);
        UserCallback c = registeredUsers.get(name);
        for(String i : loggedUsers){
            // checks in the userDB if user i is friend with the user logging in
            if(myTmp.isFriendWith(i))
                c.hasLoggedIn(i);
        }
    }

    // a client calls this, adds its own stub to registeredUsers, the server tells everyone this client is online
    @Override
    public void login(UserCallback c, String name) throws RemoteException {
        loggedUsers = registeredUsers.keySet().toArray(new String[registeredUsers.size()]);
        User myTmp = userDatabase.get(name);
        for(String i : loggedUsers){
            // checks in the userDB if user i is friend with the user logging in
            if(userDatabase.get(i).isFriendWith(name))
                registeredUsers.get(i).hasLoggedIn(name);
            // Send over all online statuses when first logging in.
            /*
            if(myTmp.isFriendWith(i))
                c.hasLoggedIn(i);
            */
        }
        registeredUsers.put(name, c);
    }

    @Override
    public void logout(String name) throws RemoteException {
        registeredUsers.remove(name);
        loggedUsers = registeredUsers.keySet().toArray(new String[registeredUsers.size()]);
        for(String i : loggedUsers){
            // checks in the userDB if user i is friend with current user
            if(userDatabase.get(i).isFriendWith(name))
                registeredUsers.get(i).hasLoggedOut(name);
        }
    }
}
