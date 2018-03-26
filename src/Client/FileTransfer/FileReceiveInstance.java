package Client.FileTransfer;

import Client.UI.FileReceiverWindow.OpenFRWindow;
import Client.UI.TestUI;
import Communication.Message;
import javafx.application.Platform;

import java.net.Socket;

public class FileReceiveInstance implements Runnable {
    private Socket sock;
    private String userName, fileName;

    public FileReceiveInstance(Socket sock, String incoming){
        this.sock = sock;
        String[] tmp = incoming.split(":");
        this.userName = tmp[0];
        this.fileName = tmp[1];
    }
    @Override
    public void run() {
        if(TestUI.controller.fileReceive == null) {
            // TODO: File transfer accept request;
            // open new stage with file transfer request
            System.out.println(userName+fileName);
            OpenFRWindow fr = new OpenFRWindow(userName, fileName, sock);
            Platform.runLater(fr);
            // answer yes/no to request and ask where to save
            // close if no
            // show progress if yes?
            // confirm when done
            // close socket
        }else{
            Message reply = new Message("OP_ERR", "Busy with another transfer");
            reply.send(sock);
        }
        TestUI.controller.fileReceive = null;
    }
}
