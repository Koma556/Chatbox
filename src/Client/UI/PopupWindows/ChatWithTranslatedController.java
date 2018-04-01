package Client.UI.PopupWindows;

import Communication.Message;
import javafx.fxml.FXML;
import javafx.stage.Stage;

import static Client.UI.TestUI.myUser;

public class ChatWithTranslatedController {

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
        Message msg = new Message("OP_TRS_MSG", username);
        msg.send(myUser.getMySocket());

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
