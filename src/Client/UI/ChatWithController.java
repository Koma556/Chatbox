package Client.UI;

import Client.ChatHandler;
import Client.Core;
import Communication.Message;
import javafx.fxml.FXML;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import static Client.UI.TestUI.controller;
import static Client.UI.TestUI.myUser;

public class ChatWithController {
    private String username;
    private Socket chatSocket;

    @FXML
    private javafx.scene.control.Button cancelButton, okButton;
    @FXML
    private javafx.scene.control.TextField textField;

    public void okButtonPress() {
        // pointless safety
        if(textField.getText() != null && !textField.getText().isEmpty()) {
            username = textField.getText();
        }

        Message msg = new Message("OP_MSG_FRD", username);
        msg.send(myUser.getMySocket());
        if(Core.waitOkAnswer(msg, myUser.getMySocket())) {
            try {
                ServerSocket newChat = new ServerSocket(myUser.getMyPort());
                chatSocket = newChat.accept();
            } catch (IOException e) {
                e.printStackTrace();
            }
            controller.addChatPane(username);

            Thread chatHandler = new Thread(new ChatHandler(chatSocket, username));
            chatHandler.start();
        }else {
            System.out.println("Not a friend!");
        }
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
