package Client.UI;

import Communication.Message;
import Communication.User;
import javafx.fxml.FXML;
import javafx.stage.Stage;

public class AddFriendController {
    private String username;
    private User myUser;

    @FXML
    private javafx.scene.control.Button cancelButton, okButton;
    @FXML
    private javafx.scene.control.TextField textField;

    public void setUser(User myUser){
        this.myUser = myUser;
    }

    public void okButtonPress() {
        // pointless safety
        if(textField.getText() != null && !textField.getText().isEmpty()) {
            username = textField.getText();
        }
        Message msg = new Message("OP_FRD_ADD", username);
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
