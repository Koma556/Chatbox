package Client;

import Client.UI.Controller;
import Client.UI.TestUI;
import Client.UI.chatPane.CreateTab;
import Client.UI.chatPane.LockTab;
import Client.UI.chatPane.UpdateTab;
import Communication.Message;
import javafx.application.Platform;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class ChatInstance implements Runnable {
    private Socket chatSocket;
    private String friendName;

    public ChatInstance(Socket sock, String friendName){
        this.chatSocket = sock;
        this.friendName = friendName;
    }

    @Override
    public void run() {
        CreateTab newTab = new CreateTab(friendName, chatSocket);
        Platform.runLater(newTab);
        while (chatSocket.isConnected() && !chatSocket.isClosed() && Controller.openChatTabs.containsKey(friendName) && Controller.openChatTabs.get(friendName).isActive()) {
            Message msg = new Message();
            try {
                msg.receive(chatSocket);
            } catch (SocketTimeoutException e) {
                Controller.openChatTabs.get(friendName).setActive(false);
            }
            if(msg.getOperation()!= null){
                // check if this is a goodbye message
                if(msg.getOperation().equals("OP_END_CHT")){
                    Controller.openChatTabs.get(friendName).setActive(false);
                    System.out.println("Received END CHAT operation.");
                }else {
                    // get the message and ask the main UI thread to update the tab
                    String tmpData = msg.getData();
                    UpdateTab upTab = new UpdateTab(friendName, tmpData, "tcp");
                    Platform.runLater(upTab);
                }
            } else {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        LockTab lockTab = new LockTab(friendName);
        Platform.runLater(lockTab);
        try {
            chatSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
