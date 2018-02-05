package Client.UI.chatPane;

import Client.UI.TestUI;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class ChatTabController {
    private String myName = TestUI.myUser.getName();

    @FXML
    private javafx.scene.control.TextArea visualizingTextAreaItem, typingTextAreaItem;

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
            typingTextAreaItem.clear();
            typingTextAreaItem.positionCaret(0);
        }
    }
    public void keyListener(KeyEvent event){
        if(event.getCode() == KeyCode.ENTER) {
            typeLine();
        }
    }

}
