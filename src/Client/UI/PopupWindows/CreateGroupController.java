package Client.UI.PopupWindows;

import Client.Core;
import Client.UDP.UDPClient;
import Client.UI.Controller;
import Client.UI.TestUI;
import Communication.Message;

import javafx.fxml.FXML;
import javafx.stage.Stage;

import java.net.SocketTimeoutException;

import static Client.UI.TestUI.myUser;

public class CreateGroupController {
    private String chatID;

    @FXML
    private javafx.scene.control.Button cancelButton, okButton;
    @FXML
    private javafx.scene.control.TextField textField;

    public void okButtonPress() {
        // pointless safety
        if(textField.getText() != null && !textField.getText().isEmpty()) {
            chatID = textField.getText();
        }
        Message msg = new Message("OP_CRT_GRP", chatID);
        msg.send(myUser.getMySocket());
        Message reply = new Message();
        try {
            reply.receive(myUser.getMySocket());
        } catch (SocketTimeoutException e) {
            e.printStackTrace();
        }
        if(Core.waitOkAnswer(reply, myUser.getMySocket())){
            String[] ports = reply.getData().split(":");
            int portIn = Integer.parseInt(ports[0]);
            int portOut = Integer.parseInt(ports[1]);
            UDPClient newChat = new UDPClient(portIn, portOut, chatID);
            Thread newChatThread = new Thread(newChat);
            newChatThread.start();
            Controller.openGroupChats.put(chatID, newChatThread);
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
