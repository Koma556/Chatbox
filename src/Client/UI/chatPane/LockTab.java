package Client.UI.chatPane;

import Client.UI.CoreUI;

public class LockTab implements Runnable {
    private String user;

    public LockTab(String user){
        this.user = user;
    }
    @Override
    public void run() {
        CoreUI.controller.lockChatTabWrites(user);
    }
}
