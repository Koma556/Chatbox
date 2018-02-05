package Client.UI.chatPane;

import Client.UI.TestUI;

public class UpdateTab implements Runnable {
    private String user, data;

    public UpdateTab(String username, String content){
        this.user = username;
        this.data = content;
    }

    @Override
    public void run() {
        TestUI.controller.writeToChatTab(user, data);
    }
}
