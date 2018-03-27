package Client.FileTransfer;

import Client.Core;
import Client.UI.FileSenderWindow.FileSenderStatusUpdate;
import Client.UI.FileSenderWindow.OpenFSWindow;
import Client.UI.TestUI;
import Communication.Message;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;


import java.io.File;
import java.io.IOException;
import java.net.Socket;

import static Client.UI.Controller.listOfFileSenderProcesses;
import static Client.UI.TestUI.myUser;

public class FileSendInstance implements Runnable {
    private File file;
    private FriendWrapper target;
    private Socket sock;
    private ObservableValue<String> text;

    public FileSendInstance(FriendWrapper target, File file){
        this.target = target;
        this.file = file;
    }

    @Override
    public void run() {
        // open a new stage showing transfer status
        // create socket to address at port and connect to it
        String text = "Connecting...";
        OpenFSWindow fs = new OpenFSWindow(text, target.getPort());
        Platform.runLater(fs);
        try {
            sock = new Socket(target.getAddress(), target.getPort());
            // send request over socket
            StringBuilder fileSpecifications = new StringBuilder();
            // send myName:filename to the user I'm contacting
            fileSpecifications.append(myUser.getName());
            fileSpecifications.append(":");
            fileSpecifications.append(file.getName().toString());
            Message request = new Message("OP_INC_FIL", fileSpecifications.toString());
            request.send(sock);
            text = "Sent Transfer Request";
            FileSenderStatusUpdate update = new FileSenderStatusUpdate(text, target.getPort());
            Platform.runLater(update);
            // wait for friend to accept
            Message answer = new Message();
            if(!Core.waitOkAnswer(answer, sock)){
                // show error and exit
                text = "Transfer Refused!";
                update = new FileSenderStatusUpdate(text, target.getPort());
                Platform.runLater(update);
            }else {
                text = "Starting Transfer";
                // show error and exit
                update = new FileSenderStatusUpdate(text, target.getPort());
                Platform.runLater(update);
                // put file into stream and send stream on socket
                // check on listOfFileSenderProcesses.get(target.gerPort()).getDone() to see if I have to stop
                while(!listOfFileSenderProcesses.get(target.getPort()).isDone()){
                    Thread.sleep(50);
                }
                // give confirmation when completed
                text = "Transfer Complete!";
                update = new FileSenderStatusUpdate(text, target.getPort());
                Platform.runLater(update);
                // close socket
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            listOfFileSenderProcesses.remove(target.getPort());
            System.out.println("Send Instance Closing");
            try{
                sock.close();
            }catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
