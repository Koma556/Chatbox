package Client.UI.PopupWindows;

import Client.FriendchatsListener;
import Client.Core;

import Client.UI.TestUI;
import Communication.IsoUtil;
import javafx.fxml.FXML;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import sun.util.locale.InternalLocaleBuilder;

import java.io.IOException;
import java.net.Socket;
import java.util.Locale;

import static Client.UI.TestUI.myUser;
import static Client.UI.TestUI.sessionClientPort;

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
        if(serverIPTextField.getText() != null && !serverIPTextField.getText().isEmpty()) {
            serverIP = serverIPTextField.getText();
        }
        if(serverPortTextField.getText() != null && !serverPortTextField.getText().isEmpty()) {
            serverPort = Integer.parseInt(serverPortTextField.getText());
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
            }
        }
        // launching the static method inside my Core class to connect via the above data
        StringBuilder registrationDataBundle = new StringBuilder();
        registrationDataBundle.append(username).append(",").append(sessionClientPort).append(",").append(userLanguage);
        Socket mySocket = Core.connect(username, serverIP, serverPort);
        if(Core.Register(registrationDataBundle.toString(), mySocket)){
            // setting the newly acquired fields within myUser

            myUser.setName(username);
            myUser.setMyLanguage(userLanguage);
            myUser.setMySocket(mySocket);
            myUser.startHeartMonitor();
            myUser.lockRegistry();

            TestUI.controller.enableControls();

            // booting up the main command-handling thread
            Thread handleCommands = new Thread(new FriendchatsListener());
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
    }
}
