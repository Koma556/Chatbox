package Client.UI.PopupWindows;

import Communication.Message;
import javafx.fxml.FXML;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.net.Socket;

import static Client.UI.TestUI.myUser;

public class ChatWithController {
    private String username;

    @FXML
    private javafx.scene.control.Button cancelButton, okButton;
    @FXML
    private javafx.scene.control.TextField textField;

    public void okButtonPress() {
        // pointless safety
        if(textField.getText() != null && !textField.getText().isEmpty()) {
            username = textField.getText();
        }
        // ask server to open a connection with the specified friend
        // server will at the same time try and open a connection with me on my serversocket
        Message msg = new Message("OP_MSG_FRD", username);
        msg.send(myUser.getMySocket());

        Stage stage = (Stage) okButton.getScene().getWindow();
        stage.close();
    }

    public void keyListener(KeyEvent event){
        if(event.getCode() == KeyCode.ENTER) {
            okButtonPress();
        }
    }

    @FXML
    public void cancelButtonPress(){
        // get the stage to which cancelButton belongs
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}
