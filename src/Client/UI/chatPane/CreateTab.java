package Client.UI.chatPane;

import Client.UI.CoreUI;

import java.net.DatagramSocket;
import java.net.Socket;

public class CreateTab implements Runnable{
    private String user;
    private Socket chatSocket;
    private DatagramSocket udpChatSocket;
    private int portOut;

    public CreateTab(String username, Socket sock){
        this.user = username;
        this.chatSocket = sock;
    }

    public CreateTab(String chatID, DatagramSocket s, int portOut) {
        this.user = chatID;
        this.udpChatSocket = s;
        this.portOut = portOut;
    }

    @Override
    public void run() {
        if(chatSocket != null)
            CoreUI.controller.addChatPane(user, chatSocket);
        else if(udpChatSocket != null)
            CoreUI.controller.addChatPane(user, udpChatSocket, portOut);
        return;
    }
}
