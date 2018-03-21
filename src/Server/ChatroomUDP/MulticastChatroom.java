package Server.ChatroomUDP;

import Communication.Message;
import Server.User;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

// a single chatroom instance
public class MulticastChatroom implements Runnable {
    private String chatID, owner;
    private ConcurrentHashMap<String, User> registeredUsers;
    private int port;

    public int getPort(){
        return port;
    }

    public MulticastChatroom(String chatID){
        this.chatID = chatID;
        this.registeredUsers = new ConcurrentHashMap<>();
    }

    public void setOwner(String owner){
        this.owner = owner;
    }

    public String getOwner(){
        return owner;
    }

    public void addMe(String username, User user){
        if(!registeredUsers.containsKey(username))
            registeredUsers.put(username, user);
    }

    public void removeMe(String username) throws NoSuchUserException{
        if(registeredUsers.containsKey(username))
            registeredUsers.remove(username);
        else
            throw new NoSuchUserException();
    }

    // Throws an exception if the chat-room doesn't exist or if no user registered on the
    // chat-room is online (with the exception of the user calling this command).
    public void chatroomMessage(String sender, String message){

    }


    @Override
    public void run() {

    }
}
