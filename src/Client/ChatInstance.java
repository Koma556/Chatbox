package Client;

import Client.UI.CoreUI;
import Client.UI.chatPane.CreateTab;
import Client.UI.chatPane.LockTab;
import Client.UI.chatPane.UpdateTab;
import Communication.Message;
import javafx.application.Platform;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;

/* each TCP chat will be tied to a Runnable istance of this Class
 * the Runnable exits once it receives the OP_END_CHT message from the server
 */

public class ChatInstance implements Runnable {
    private Socket chatSocket;
    private String friendName;
    private boolean done = false;

    public ChatInstance(Socket sock, String friendName){
        this.chatSocket = sock;
        this.friendName = friendName;
    }

    @Override
    public void run() {
        CreateTab newTab = new CreateTab(friendName, chatSocket);
        Platform.runLater(newTab);
        done = false;
        while (chatSocket.isConnected() && !chatSocket.isClosed() && !done) {
            Message msg = new Message();
            try {
                msg.receive(chatSocket);
            } catch (SocketTimeoutException e) {
                done = true;
            }
            if(msg.getOperation()!= null){
                // check if this is a goodbye message
                if(msg.getOperation().equals("OP_END_CHT")){
                    done = true;
                    CoreUI.controller.allActiveChats.remove(friendName);
                    LockTab lockTab = new LockTab(friendName);
                    Platform.runLater(lockTab);
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
        try {
            chatSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
