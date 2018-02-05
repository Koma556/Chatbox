package Client.UI.PopupWindows;

import Client.FriendchatsListener;
import Client.Core;

import Client.UI.TestUI;
import javafx.fxml.FXML;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.Socket;

public class LoginController {

    private String[] tmpFrdLst;

    @FXML
    // FMXL annotation is a must
    private javafx.scene.control.Button cancelButton, okButton;
    @FXML
    private javafx.scene.control.TextField usernameTextField, serverIPTextField, serverPortTextField;
    @FXML
    private javafx.scene.text.Text userNameValidationText;

    // sets username, user socket and user friends as a String[]
    public void okButtonPress(){
        // instancing default options for username, localhost and server port
        String username = "Default", serverIP = "localhost";
        int serverPort = 62543;

        // pointless safety
        if(usernameTextField.getText() != null && !usernameTextField.getText().isEmpty()) {
            username = usernameTextField.getText();
        }
        if(serverIPTextField.getText() != null && !serverIPTextField.getText().isEmpty()) {
            serverIP = serverIPTextField.getText();
        }
        if(serverPortTextField.getText() != null && !serverPortTextField.getText().isEmpty()) {
            serverPort = Integer.parseInt(serverPortTextField.getText());
        }
        // launching the static method inside my Core class to connect via the above data
        Socket mySocket = Core.connect(username, serverIP, serverPort);
        if((tmpFrdLst = Core.Login(username, mySocket)) != null){
            // setting the newly acquired fields within TestUI.myUser

            TestUI.myUser.setName(username);
            TestUI.myUser.setMySocket(mySocket);
            TestUI.myUser.setTmpFriendList(tmpFrdLst);

            // TODO: Maybe let command handler reorganize friend list?

            // booting up the main command-handling thread
            Thread handleCommands = new Thread(new FriendchatsListener());
            handleCommands.start();

            TestUI.controller.populateListView();
            TestUI.controller.enableControls();

            // closing the window
            Stage stage = (Stage) okButton.getScene().getWindow();
            stage.close();
        }
        else{
            userNameValidationText.setFill(Color.RED);
            userNameValidationText.setText("Login Error.");
            try {
                mySocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //TODO: error handling
    }

    @FXML
    public void cancelButtonPress(){
        // get the stage to which cancelButton belongs
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}
