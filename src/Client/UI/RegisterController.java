package Client.UI;

import Client.CommandListener;
import Client.Core;

import javafx.fxml.FXML;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.Socket;

import static Client.UI.TestUI.myUser;

public class RegisterController {

    @FXML
    private javafx.scene.control.Button cancelButton, okButton;
    @FXML
    private javafx.scene.control.TextField usernameTextField, serverIPTextField, serverPortTextField;
    @FXML
    private javafx.scene.text.Text userNameValidationText;

    @FXML
    public void cancelButtonPress(){
        // get the stage to which cancelButton belongs
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

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
        if(Core.Register(username, mySocket)){
            // setting the newly acquired fields within myUser

            myUser.setName(username);
            myUser.setMySocket(mySocket);

            // booting up the main command-handling thread
            Thread handleCommands = new Thread(new CommandListener());
            handleCommands.start();

            // closing the window
            Stage stage = (Stage) okButton.getScene().getWindow();
            stage.close();
        }
        else{
            userNameValidationText.setFill(Color.RED);
            userNameValidationText.setText("Username taken.");
            try {
                mySocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //TODO: error handling
    }
}
