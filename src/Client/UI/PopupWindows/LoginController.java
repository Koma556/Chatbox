package Client.UI.PopupWindows;

import Client.FriendchatsListener;
import Client.Core;

import Client.NIO.FileReceiverServer;
import Client.UI.CoreUI;
import javafx.fxml.FXML;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.Socket;

import static Client.UI.CoreUI.myUser;
import static Client.UI.CoreUI.sessionClientPort;
import static Client.UI.CoreUI.sessionNIOPort;

public class LoginController {

    // this is a string array containing the IP and port on which the server is running the RMI registry
    // it's exchanged after a login or registration by the relative static methods
    private String[] tmpRMIloc;

    @FXML
    // FMXL annotation is a must
    private javafx.scene.control.Button cancelButton, okButton;
    @FXML
    private javafx.scene.control.TextField usernameTextField, serverIPTextField, serverPortTextField;
    @FXML
    private javafx.scene.text.Text userNameValidationText;

    public void keyListener(KeyEvent event){
        if(event.getCode() == KeyCode.ENTER) {
            okButtonPress();
        }else if(event.getCode() == KeyCode.ESCAPE) {
            cancelButtonPress();
        }
    }

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
        StringBuilder bundleThePort = new StringBuilder();
        bundleThePort.append(username).append(",").append(sessionClientPort).append(",").append(sessionNIOPort);
        Socket mySocket = Core.connect(serverIP, serverPort);
        if((tmpRMIloc = Core.Login(bundleThePort.toString(), mySocket)) != null){

            // setting the newly acquired fields within CoreUI.myUser
            myUser.setName(username);
            myUser.setMySocket(mySocket);
            myUser.startHeartMonitor();
            // registering RMI callback
            myUser.lockRegistry(tmpRMIloc[0], tmpRMIloc[1]);

            // booting up the chat and file transfer listeners
            Thread listenForChats = new Thread(new FriendchatsListener());
            listenForChats.start();
            Thread listenForNIO = new Thread(new FileReceiverServer());
            listenForNIO.start();

            CoreUI.controller.populateListView();
            CoreUI.controller.enableControls();

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
    }

    @FXML
    public void cancelButtonPress(){
        // get the stage to which cancelButton belongs
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}
