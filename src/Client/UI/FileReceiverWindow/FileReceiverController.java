package Client.UI.FileReceiverWindow;

import Client.UI.TestUI;
import Communication.Message;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.Socket;

import static Client.UI.Controller.listOfFileReceiverProcesses;

public class FileReceiverController {
    @FXML
    private javafx.scene.control.Button refuseButton, acceptButton, cancelButton;
    @FXML
    private Label fromLabel, filenameLabel;
    private Socket sock;
    private int id;

    public void setStatusLabel(String from, String filename) {
        fromLabel.textProperty().setValue("Receiving file from User:" + from);
        filenameLabel.textProperty().setValue("Filename: " +filename);
    }

    public void setSock(Socket sock){
        this.sock = sock;
        this.id = sock.getPort();
    }

    public void acceptButtonPress(){
        Message reply = new Message("OP_OK", "");
        reply.send(sock);
        //receive stuff and update progress bar
        cancelButton.setDisable(false);
        refuseButton.setDisable(true);
        acceptButton.setDisable(true);
    }

    public void refuseButtonPress(){
        Message reply = new Message("OP_ERR", "User refused transfer");
        reply.send(sock);
        if(listOfFileReceiverProcesses.containsKey(id))
            listOfFileReceiverProcesses.get(id).setDone(true);
        Stage stage = (Stage) refuseButton.getScene().getWindow();
        stage.close();
    }

    public void cancelButtonPress(){
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    public void stop(){
        if(listOfFileReceiverProcesses.containsKey(id))
            listOfFileReceiverProcesses.get(id).setDone(true);
    }
}
