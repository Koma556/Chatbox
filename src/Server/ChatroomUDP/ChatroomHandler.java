package Server.ChatroomUDP;

import Server.User;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

// main hub of all chatrooms on the server
public class ChatroomHandler implements Serializable{
    private ConcurrentHashMap<String, MulticastChatroom> activeChatrooms;

    public ChatroomHandler(){
        this.activeChatrooms = new ConcurrentHashMap<>();
    }

    // creates a chatroom with the specified chatID. Does NOT register the user
    // throws ExistingChatRoomException if the chatroom already exists
    public MulticastChatroom create(String chatID) throws ExistingChatRoomException{
        MulticastChatroom newChatroom = null;
        if(!activeChatrooms.containsKey(chatID)){
            newChatroom = new MulticastChatroom(chatID);
            activeChatrooms.put(chatID, newChatroom);
        } else {
            throw new ExistingChatRoomException();
        }
        return newChatroom;
    }

    // returns a list of all active chatrooms to the user calling this method
    // the user will need to compare this with the list of chatrooms it is registered with
    public String[] chatlist(){
        return activeChatrooms.keySet().toArray(new String[activeChatrooms.size()]);
    }

    // closes a chatroom and notifies all users of this event
    public void closechat(String chatID) throws NoSuchChatRoomException{
        if(activeChatrooms.containsKey(chatID)){
            activeChatrooms.get(chatID).chatroomMessage("SYSTEM", "Closing the chatroom.");
            activeChatrooms.remove(chatID);
        } else
            throw new NoSuchChatRoomException();
    }

    public MulticastChatroom getChatroom(String chatID) throws NoSuchChatRoomException{
        if(activeChatrooms.containsKey(chatID))
            return activeChatrooms.get(chatID);
        else
            throw new NoSuchChatRoomException();
    }
}
