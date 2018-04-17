package Client.UI.PopupWindows;

import Client.Core;
import Client.UI.CoreUI;
import Communication.Message;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.net.SocketTimeoutException;

import static Client.UI.CoreUI.myUser;

public class UserLookupController {
    private String username, userToAdd;

    @FXML
    private javafx.scene.control.Button closeButton, searchButton, addHimButton;
    @FXML
    private javafx.scene.control.TextField textField;
    @FXML
    private javafx.scene.text.Text lookupLabel;

    public boolean changed(ObservableValue<? extends String> observable,
                        String oldValue) {

        return true;
    }

    public void keyListener(KeyEvent event){
        if(event.getCode() == KeyCode.ENTER) {
            searchButtonPress();
        }else if(event.getCode() == KeyCode.ESCAPE) {
            closeButtonPress();
        }
    }

    public void closeButtonPress(){
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }

    public void searchButtonPress(){
        if(textField.getText() != null && !textField.getText().isEmpty()) {
            username = textField.getText();
        }
        Message msg = new Message("OP_LKP_USR", username);
        try {
            msg.send(myUser.getMySocket());
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
        Message reply = new Message();
        try {
            reply.receive(myUser.getMySocket());
        } catch (SocketTimeoutException e) {
            e.printStackTrace();
        }
        Color status;
        if(Core.waitOkAnswer(reply, myUser.getMySocket())){
            status = Color.GREEN;
            userToAdd = username;
            addHimButton.setDisable(false);
        }else{
            status = Color.RED;
            addHimButton.setDisable(true);
        }
        lookupLabel.setFill(status);
        lookupLabel.setText(reply.getData());
    }

    public void addUserButtonPress() {
        Message msg = new Message("OP_FRD_ADD", userToAdd);
        try {
            msg.send(myUser.getMySocket());
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
        Message reply = new Message();
        if(Core.waitOkAnswer(reply, myUser.getMySocket())){
            // update GUI's friend list, doesn't wait for an OK confirmation
            CoreUI.controller.populateListView();
            Stage stage = (Stage) addHimButton.getScene().getWindow();
            myUser.getFriendOnlineStatus();
            stage.close();
        } else {
            Warning warning = new Warning("Error!",
                    "Couldn't add friend " + username,
                    reply.getData());
            Platform.runLater(warning);
        }
    }

    public void registerListener(){
        // registering a listener to invalidate the answer each time the user types into the search box
        textField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                                String oldValue, String newValue) {
                addHimButton.setDisable(true);
                lookupLabel.setText("User Lookup");
                lookupLabel.setFill(Color.BLACK);
            }
        });
    }
}
