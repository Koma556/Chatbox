package Client.UI.chatPane;

import Client.UI.TestUI;

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
            TestUI.controller.writeToChatTab(user, data);
        else if(mode.equals("udp")) {
            TestUI.controller.writeToUdpChatTab(user, data);
            if(data.equals("-Server Closing the Chatroom-"))
                TestUI.controller.lockChatTabWrites(user);
            //System.out.println("UPD "+user+" RECEIVED: "+data);
        }
    }
}
