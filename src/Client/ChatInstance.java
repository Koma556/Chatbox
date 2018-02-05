package Client;

import Client.UI.chatPane.CreateTab;
import Client.UI.chatPane.UpdateTab;
import Communication.Message;
import javafx.application.Platform;

import java.net.Socket;

public class ChatInstance implements Runnable {
    private Socket chatSocket;
    private String friendName;
    private boolean exit = false, done = false;

    public ChatInstance(Socket sock){
        this.chatSocket = sock;
    }

    @Override
    public void run() {
        // executes the chatInstance code on the main UI thread as requested by javaFX's specifications
        Message firstMsg = new Message();
        while(chatSocket.isConnected() && !chatSocket.isClosed() && !exit) {
            firstMsg.receive(chatSocket);
            // TODO: implement server side welcome message
            // client waits for a welcome message which will contain the name of the user which started the chat
            if (firstMsg.getOperation() != null && firstMsg.getOperation().equals("OP_NEW_FCN")) {
                this.friendName = firstMsg.getData();
                exit = true;
            } else {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        CreateTab newTab = new CreateTab(friendName, chatSocket);
        Platform.runLater(newTab);
        // TODO: when I receive a message, call an UpdateTab Runnable with the same method as above to process said message
        while (chatSocket.isConnected() && !chatSocket.isClosed() && !done) {
            Message msg = new Message();
            msg.receive(chatSocket);
            if(msg.getOperation()!= null){
                // check if this is a goodbye message
                if(msg.getOperation().equals("OP_END_CHT")){
                    done = true;
                }else {
                    // get the message and ask the main UI thread to update the tab
                    String tmpData = msg.getData();
                    UpdateTab upTab = new UpdateTab(friendName, tmpData);
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

    }
}
