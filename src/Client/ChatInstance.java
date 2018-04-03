package Client;

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
    private boolean exit = false, done = false;

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
