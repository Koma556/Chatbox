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

    public ChatInstance(Socket sock){
        this.chatSocket = sock;
    }

    @Override
    public void run() {
        // executes the chatInstance code on the main UI thread as requested by javaFX's specifications
        Message firstMsg = new Message();
        // while is never checked again?!
        exit = false;
        while(!exit) {
            try {
                firstMsg.receive(chatSocket);
            } catch (SocketTimeoutException e) {
                exit = true;
                try {
                    chatSocket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            // client waits for a welcome message which will contain the name of the user which started the chat
            if (firstMsg.getOperation() != null && firstMsg.getOperation().equals("OP_NEW_FCN")) {
                this.friendName = firstMsg.getData();
                exit = true;
            }
            else {
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
        try {
            chatSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
