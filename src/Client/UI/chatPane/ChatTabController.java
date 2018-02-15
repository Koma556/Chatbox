package Client.UI.chatPane;

import Client.UI.TestUI;
import Communication.Message;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.net.Socket;

public class ChatTabController {
    private String myName = TestUI.myUser.getName();
    private Socket chatSocket;

    @FXML
    private javafx.scene.control.TextArea visualizingTextAreaItem, typingTextAreaItem;
    @FXML
    private javafx.scene.control.Button sendButton;

    public void setChatSocket(Socket sock){
        this.chatSocket = sock;
    }

    public void addLine(String username, String content){
        String[] contents = content.split("\n");
        for(String line: contents) {
            visualizingTextAreaItem.appendText("<" + username + "> " + line + "\n");
        }
    }
    public void typeLine(){
        String tmp;

        if((tmp = typingTextAreaItem.getText()) != null && !tmp.equals("")){
            this.addLine(myName, tmp);
            Message sendLine = new Message("OP_FRD_CHT_MSG", tmp);
            sendLine.send(chatSocket);
            typingTextAreaItem.clear();
            typingTextAreaItem.positionCaret(0);
        }
    }
    public void keyListener(KeyEvent event){
        if(event.getCode() == KeyCode.ENTER) {
            typeLine();
        }
    }

    public void lockWrite() {
        typingTextAreaItem.setEditable(false);
        typingTextAreaItem.setDisable(true);
        sendButton.setDisable(true);
    }
}
