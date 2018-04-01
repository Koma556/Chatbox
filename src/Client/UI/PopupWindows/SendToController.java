package Client.UI.PopupWindows;

import Client.UI.TestUI;
import Communication.Message;
import javafx.fxml.FXML;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import static Client.UI.TestUI.myUser;

public class SendToController {
    private String username = null;
    @FXML
    private javafx.scene.control.Button cancelButton, okButton;
    @FXML
    private javafx.scene.control.TextField textField;



    public void okButtonPress() {
        // pointless safety
        if(textField.getText() != null && !textField.getText().isEmpty()) {
            username = textField.getText();
        }
        Stage stage = (Stage) okButton.getScene().getWindow();
        stage.close();
    }

    public void keyListener(KeyEvent event){
        if(event.getCode() == KeyCode.ENTER) {
            okButtonPress();
        }
    }

    public String getUsername(){
        return username;
    }

    @FXML
    public void cancelButtonPress(){
        // get the stage to which cancelButton belongs
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}
