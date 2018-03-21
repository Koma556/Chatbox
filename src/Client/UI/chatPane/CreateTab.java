package Client.UI.chatPane;

import Client.UI.TestUI;

import java.net.MulticastSocket;
import java.net.Socket;

public class CreateTab implements Runnable{
    private String user, chatID;
    private Socket chatSocket;
    private MulticastSocket socketUDP;

    // TCP version
    public CreateTab(String username, Socket sock){
        this.user = username;
        this.chatSocket = sock;
    }

    // UDP version
    public CreateTab(String chatID, MulticastSocket socketUDP){
        this.chatID = chatID;
        this.socketUDP = socketUDP;
    }

    @Override
    public void run() {
        if(chatSocket != null)
            TestUI.controller.addChatPane(user, chatSocket);
        else if (socketUDP != null)
            TestUI.controller.addUDPChatPane(chatID, socketUDP);
        return;
    }
}
