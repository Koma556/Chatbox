package Client.UI.NIOui;

import Client.Core;
import Client.NIO.Sender;
import Client.UI.PopupWindows.Alerts;
import Communication.Message;
import javafx.fxml.FXML;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import static Client.UI.CoreUI.myUser;

public class SenderController {
    private String username;
    private String filePath, fileName;

    @FXML
    private javafx.scene.control.Button cancelButton, okButton;
    @FXML
    private javafx.scene.control.TextField textField;
    @FXML
    private javafx.scene.text.Text fileNameText;

    public void keyListener(KeyEvent event){
        if(event.getCode() == KeyCode.ENTER) {
            okButtonPress();
        }else if(event.getCode() == KeyCode.ESCAPE) {
            cancelButtonPress();
        }
    }

    public void chooseButtonPress(){
        Stage stage = new Stage();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        File file = fileChooser.showOpenDialog(stage);
        filePath = file.getPath();
        fileName = file.getName();
        fileNameText.setText(fileName);
    }

    public String getFilePath(){
        return filePath;
    }

    public void okButtonPress() {
        boolean error = false;
        Sender sender = null;
        if(textField.getText() != null && !textField.getText().isEmpty() && filePath != null) {
            username = textField.getText();
            try {
                Message msg = new Message("OP_SND_FIL", username);
                msg.send(myUser.getMySocket());
                Message reply = new Message();
                Core.waitOkAnswer(reply, myUser.getMySocket());
                String tmp[] = reply.getData().split(":");
                sender = new Sender(InetAddress.getByName(tmp[0]), Integer.parseInt(tmp[1]), filePath, fileName);
            } catch (UnknownHostException e) {
                Alerts alert = new Alerts("Transfer Failed", "Couldn't contact partner.", "Your friend might have gone offline.");
                error = true;
            } catch (IOException e) {
                Alerts alert = new Alerts("Transfer Failed", "Couldn't open specified file.", "File at " + filePath + " couldn't be read.");
                error = true;
            }
            if(!error) {
                Thread senderThread = new Thread(sender);
                senderThread.start();
                Stage stage = (Stage) okButton.getScene().getWindow();
                stage.close();
            }
        }
    }

    @FXML
    public void cancelButtonPress(){
        // get the stage to which cancelButton belongs
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}
