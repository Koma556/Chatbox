package Client;

import Client.UI.Controller;
import Client.UI.PopupWindows.BigErrorAlert;
import Communication.Message;
import javafx.application.Platform;

import java.net.Socket;

public class Heartbeat implements Runnable{
    private boolean done = false;
    private Socket sock;
    private Message heartBeatMessage;

    public Heartbeat(Socket sock){
        this.sock = sock;
    }

    public void isDone(){
        this.done = true;
    }

    @Override
    public void run() {
        while(!done){
            heartBeatMessage = new Message("OP_HEARTBEAT", "");
            try {
                heartBeatMessage.send(sock);
            } catch (java.io.IOException e) {
                BigErrorAlert majorError = new BigErrorAlert("Connection Error!", "We couldn't reach the server.", "Connection with server lost.", e);
                Platform.runLater(majorError);
                break;
            }
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
    }
}
