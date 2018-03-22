package Client.UI.PopupWindows;

import Client.Core;
import Client.UI.Controller;
import Client.UI.TestUI;
import Communication.Message;
import javafx.fxml.FXML;
import javafx.stage.Stage;

import static Client.UI.TestUI.myUser;

public class LeaveGroupController {
    private String chatID;

    @FXML
    private javafx.scene.control.Button cancelButton, okButton;
    @FXML
    private javafx.scene.control.TextField textField;

    public void okButtonPress() {
        // pointless safety
        if (textField.getText() != null && !textField.getText().isEmpty()) {
            chatID = textField.getText();
        }
        Message msg = new Message("OP_LEV_GRP", chatID);
        msg.send(myUser.getMySocket());
        Message reply = new Message();
        if(Core.waitOkAnswer(reply, myUser.getMySocket())){
            TestUI.controller.closeUdpChatThread(chatID);
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
