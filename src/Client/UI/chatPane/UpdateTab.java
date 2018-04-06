package Client.UI.chatPane;

import Client.UI.CoreUI;

public class UpdateTab implements Runnable {
    private String user, data, mode;

    public UpdateTab(String username, String content, String mode){
        this.user = username;
        this.data = content;
        this.mode = mode;
    }

    @Override
    public void run() {
        if(mode.equals("tcp"))
            CoreUI.controller.writeToChatTab(user, data);
        else if(mode.equals("udp")) {
            CoreUI.controller.writeToUdpChatTab(user, data);
            if(data.equals("-Server Closing the Chatroom-"))
                CoreUI.controller.lockChatTabWrites(user);
        }
    }
}
