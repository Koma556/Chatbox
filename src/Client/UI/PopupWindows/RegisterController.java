package Client.UI.PopupWindows;

import Client.FriendchatsListener;
import Client.Core;

import Client.NIO.FileReceiverServer;
import Client.UI.CoreUI;
import Communication.IsoUtil;
import Communication.Message;
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
        boolean error = false;

        // set language the same as system locale if left blank
        Locale currentLocale = Locale.getDefault();
        userLanguage = currentLocale.getLanguage();
        // pointless safety
        if(usernameTextField.getText() != null && !usernameTextField.getText().isEmpty()) {
            username = usernameTextField.getText();
        } else {
            error = true;
        }
        if(userLanguageTextField.getText() != null) {
            String userLanguageTmp;
            if(!userLanguageTextField.getText().isEmpty()){
                // if the user has put in a valid language code it gets accepted, overwriting the default system locale
                userLanguageTmp = userLanguageTextField.getText();
                if(IsoUtil.isValidISOLanguage(userLanguageTmp)) {
                    userLanguage = userLanguageTmp;
                } else {
                    Warning wrongLocale = new Warning("Warning",
                            "Invalid Language",
                            userLanguageTmp+" is not a valid language code, language was set to "+userLanguage);
                    Platform.runLater(wrongLocale);
                }
            } else {
                Warning wrongLocale = new Warning("Warning",
                        "Language was left Blank",
                        "You have put in no language code. Your language has been set to " + userLanguage + " according to your System.");
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
        Message registrationMessage = new Message("OP_REGISTER", registrationDataBundle.toString());
        if(!error && Core.Register(mySocket,registrationMessage)){
            // setting the newly acquired fields within myUser
            myUser.setName(username);
            myUser.setMyLanguage(userLanguage);
            myUser.setMySocket(mySocket);
            myUser.startHeartMonitor();
            myUser.lockRegistry(serverIP, registrationMessage.getData());

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
            if(!error) {
                userNameValidationText.setText(registrationMessage.getData());
            }
            else {
                userNameValidationText.setText("Please choose a name.");
            }
            try {
                mySocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
