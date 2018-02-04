package Client.UI;

import Communication.Message;
import javafx.fxml.FXML;
import javafx.stage.Stage;

import static Client.UI.TestUI.myUser;

public class AddFriendController {
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
        Message msg = new Message("OP_FRD_ADD", username);
        msg.send(myUser.getMySocket());
        // update GUI's friend list
        TestUI.controller.populateListView();
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
