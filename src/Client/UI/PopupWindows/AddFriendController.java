package Client.UI.PopupWindows;

import Client.Core;
import Client.UI.CoreUI;
import Communication.Message;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import static Client.UI.CoreUI.myUser;

public class AddFriendController {
    private String username;

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
        if(textField.getText() != null && !textField.getText().isEmpty()) {
            username = textField.getText();
        }
        Message msg = new Message("OP_FRD_ADD", username);
        try {
            msg.send(myUser.getMySocket());
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
        Message reply = new Message();
        if(Core.waitOkAnswer(reply, myUser.getMySocket())){
            // update GUI's friend list, doesn't wait for an OK confirmation
            CoreUI.controller.populateListView();
            Stage stage = (Stage) okButton.getScene().getWindow();
            myUser.getFriendOnlineStatus();
            stage.close();
        } else {
            Warning warning = new Warning("Error!",
                    "Couldn't add friend " + username,
                    reply.getData());
            Platform.runLater(warning);
        }
    }

    @FXML
    public void cancelButtonPress(){
        // get the stage to which cancelButton belongs
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}
