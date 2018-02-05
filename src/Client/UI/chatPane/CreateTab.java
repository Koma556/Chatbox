package Client.UI.chatPane;

import Client.UI.TestUI;

import java.net.Socket;

public class CreateTab implements Runnable{
    private String user;
    private Socket chatSocket;

    public CreateTab(String username, Socket sock){
        this.user = username;
        this.chatSocket = sock;
    }
    @Override
    public void run() {
        TestUI.controller.addChatPane(user, chatSocket);
        // TODO: read messages on this socket and pass them to the chatpane controller
        return;
    }
}
