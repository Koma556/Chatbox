package Client.UI.PopupWindows;

import Client.Core;
import Communication.Message;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import static Client.UI.CoreUI.myUser;

public class ChatWithController {
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
        // pointless safety
        if(textField.getText() != null && !textField.getText().isEmpty()) {
            username = textField.getText();
        }

        // ask server to open a connection with the specified friend
        // server will at the same time try and open a connection with me on my serversocket
        Message msg = new Message("OP_MSG_FRD", username);
        try {
            msg.send(myUser.getMySocket());
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
        // wait for an OP_OK or OP_ERR, and if an error has occurred display it
        msg.setFields(null,null);
        if(!Core.waitOkAnswer(msg, myUser.getMySocket())){
            Warning warning = new Warning("Error!",
                    "Couldn't start a chat with " + username,
                    msg.getData());
            Platform.runLater(warning);
        } else {
            Stage stage = (Stage) okButton.getScene().getWindow();
            stage.close();
        }
    }

    @FXML
    public void cancelButtonPress(){
        // get the stage to which cancelButton belongs
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}
