package Client;

import Communication.Message;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class FirstMessageListener{
    private boolean exit = false;
    private String mode, friendName;

    public FirstMessageListener(){}

    public String getMode(){
        return mode;
    }

    public String getName(){
        return friendName;
    }

    public void listenToFirstMessage(Socket chatSocket){
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
                this.mode = "chat";
                exit = true;
            }
            if (firstMsg.getOperation() != null && firstMsg.getOperation().equals("OP_INC_FIL")){
                this.friendName = firstMsg.getData();
                this.mode = "fileTransfer";
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
    }
}
