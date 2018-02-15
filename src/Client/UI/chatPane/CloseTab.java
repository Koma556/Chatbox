package Client.UI.chatPane;

import Client.UI.TestUI;

import java.util.ArrayList;

public class CloseTab implements Runnable {
    private String user;

    public CloseTab(String user){
        this.user = user;
    }

    @Override
    public void run() {
        ArrayList<String> removeThis = new ArrayList<>();
        removeThis.add(user);
        TestUI.controller.clearChatPane(removeThis);
    }
}
