package Client.UI.chatPane;

import Client.UI.TestUI;

public class CreateTab implements Runnable{
    private String user;

    public CreateTab(String username){
        this.user = username;
    }
    @Override
    public void run() {
        TestUI.controller.addChatPane(user);
        // TODO: read messages on this socket and pass them to the chatpane controller
        return;
    }
}
