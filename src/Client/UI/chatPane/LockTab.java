package Client.UI.chatPane;

import Client.UI.TestUI;

public class LockTab implements Runnable {
    private String user;

    public LockTab(String user){
        this.user = user;
    }
    @Override
    public void run() {
        TestUI.controller.lockChatTabWrites(user);
    }
}
