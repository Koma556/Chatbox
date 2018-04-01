package Client.UI.PopupWindows;

import Client.Core;
import Communication.Message;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.net.SocketTimeoutException;

import static Client.UI.TestUI.myUser;

public class UserLookupController {
    private String username;

    @FXML
    private javafx.scene.control.Button closeButton, searchButton;
    @FXML
    private javafx.scene.control.TextField textField;
    @FXML
    private javafx.scene.text.Text lookupLabel;

    public void closeButtonPress(){
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }

    public void keyListener(KeyEvent event){
        if(event.getCode() == KeyCode.ENTER) {
            searchButtonPress();
        }
    }

    public void searchButtonPress(){
        if(textField.getText() != null && !textField.getText().isEmpty()) {
            username = textField.getText();
        }
        Message msg = new Message("OP_LKP_USR", username);
        msg.send(myUser.getMySocket());
        Message reply = new Message();
        try {
            reply.receive(myUser.getMySocket());
        } catch (SocketTimeoutException e) {
            e.printStackTrace();
        }
        Color status;
        if(Core.waitOkAnswer(reply, myUser.getMySocket())){
            status = Color.GREEN;
        }else{
            status = Color.RED;
        }
        lookupLabel.setFill(status);
        lookupLabel.setText(reply.getData());
    }
}
