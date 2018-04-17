package Client.UI.PopupWindows;

import Client.FriendchatsListener;
import Client.Core;

import Client.NIO.FileReceiverServer;
import Client.UI.CoreUI;
import Communication.IsoUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.Socket;
import java.util.Locale;

import static Client.UI.CoreUI.myUser;
import static Client.UI.CoreUI.sessionClientPort;
import static Client.UI.CoreUI.sessionNIOPort;

public class RegisterController {

    @FXML
    private javafx.scene.control.Button cancelButton, okButton;
    @FXML
    private javafx.scene.control.TextField usernameTextField, serverIPTextField, serverPortTextField, userLanguageTextField;
    @FXML
    private javafx.scene.text.Text userNameValidationText;

    // this is a string array containing the IP and port on which the server is running the RMI registry
    // it's exchanged after a login or registration by the relative static methods
    private String[] tmpRMIloc;

    @FXML
    public void cancelButtonPress(){
        // get the stage to which cancelButton belongs
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    public void keyListener(KeyEvent event){
        if(event.getCode() == KeyCode.ENTER) {
            okButtonPress();
        }else if(event.getCode() == KeyCode.ESCAPE) {
            cancelButtonPress();
        }
    }

    public void okButtonPress(){
        // instancing default options for username, localhost and server port
        String username = "Default", serverIP = "localhost", userLanguage;
        int serverPort = 62543;

        // set language the same as system locale if left blank
        Locale currentLocale = Locale.getDefault();
        userLanguage = currentLocale.getLanguage();
        // pointless safety
        if(usernameTextField.getText() != null && !usernameTextField.getText().isEmpty()) {
            username = usernameTextField.getText();
        }
        if(userLanguageTextField.getText() != null && !userLanguageTextField.getText().isEmpty()) {
            String userLanguageTmp = userLanguageTextField.getText();
            // if the user has put in a valid language code it gets accepted, overwriting the default system locale
            if(IsoUtil.isValidISOLanguage(userLanguageTmp)) {
                userLanguage = userLanguageTmp;
            } else {
                Alerts wrongLocale = new Alerts("Warning",
                        "Invalid Language",
                        userLanguageTmp+" is not a valid language code, language was set to "+userLanguage);
                wrongLocale.run();
                Platform.runLater(wrongLocale);
            }
        }
        if(serverIPTextField.getText() != null && !serverIPTextField.getText().isEmpty()) {
            serverIP = serverIPTextField.getText();
        }
        if(serverPortTextField.getText() != null && !serverPortTextField.getText().isEmpty()) {
            serverPort = Integer.parseInt(serverPortTextField.getText());
        }

        // launching the static method inside my Core class to connect via the above data
        StringBuilder registrationDataBundle = new StringBuilder();
        // transmitting my username, the ports for chat and nio listeners and the language code in the format username,port,port,language
        registrationDataBundle.append(username).append(",").append(sessionClientPort).append(",").append(sessionNIOPort).append(",").append(userLanguage);
        Socket mySocket = Core.connect(serverIP, serverPort);
        if((tmpRMIloc = Core.Register(registrationDataBundle.toString(), mySocket)) != null){
            // setting the newly acquired fields within myUser

            myUser.setName(username);
            myUser.setMyLanguage(userLanguage);
            myUser.setMySocket(mySocket);
            myUser.startHeartMonitor();
            myUser.lockRegistry(tmpRMIloc[0], tmpRMIloc[1]);

            CoreUI.controller.enableControls();

            // booting up the chat and file transfer listeners
            Thread listenForChats = new Thread(new FriendchatsListener());
            listenForChats.start();
            Thread listenForNIO = new Thread(new FileReceiverServer());
            listenForNIO.start();

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
    }
}
