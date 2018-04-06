package Client.UI.PopupWindows;

import Client.Core;
import Client.UDP.UDPClient;
import Communication.Message;
import javafx.fxml.FXML;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import static Client.UI.CoreUI.myUser;

public class JoinGroupController {
    private String chatID;

    @FXML
    private javafx.scene.control.Button cancelButton, okButton;
    @FXML
    private javafx.scene.control.TextField textField;

    public void keyListener(KeyEvent event){
        if(event.getCode() == KeyCode.ENTER) {
            okButtonPress();
        }else if(event.getCode() == KeyCode.ESCAPE) {
            cancelButtonPress();
        }
    }

    public void okButtonPress() {
        // pointless safety
        if(textField.getText() != null && !textField.getText().isEmpty()) {
            chatID = textField.getText();
        }
        Message msg = new Message("OP_JON_GRP", chatID);
        try {
            msg.send(myUser.getMySocket());
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
        Message reply = new Message();
        if(Core.waitOkAnswer(reply, myUser.getMySocket())){
            String[] ports = reply.getData().split(":");
            int portIn = Integer.parseInt(ports[0]);
            int portOut = Integer.parseInt(ports[1]);
            UDPClient newChat = new UDPClient(portIn, portOut, chatID);
            Thread newChatThread = new Thread(newChat);
            newChatThread.start();
        }
        Stage stage = (Stage) okButton.getScene().getWindow();

        stage.close();
    }

    @FXML
    public void cancelButtonPress(){
        // get the stage to which cancelButton belongs
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}
