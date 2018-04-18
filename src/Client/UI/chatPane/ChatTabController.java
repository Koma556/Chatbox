package Client.UI.chatPane;

import Client.Core;
import Client.UI.Controller;
import Client.UI.CoreUI;
import Client.UI.PopupWindows.Warning;
import Communication.Message;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;

import static Client.UI.CoreUI.myUser;

public class ChatTabController {
    private String mode, myName = CoreUI.myUser.getName(), udpNameField;
    private Socket chatSocket;
    private DatagramSocket udpChatSocket;
    private InetAddress address= myUser.getMySocket().getInetAddress();
    private int portIn, udpNameLength, udpTextFieldSize;

    @FXML
    private javafx.scene.control.TextArea visualizingTextAreaItem, typingTextAreaItem;
    @FXML
    private javafx.scene.control.Button sendButton;
    // this is a textbox counting down the BYTES (not characters) left to the user per single message
    @FXML
    private javafx.scene.text.Text characterCounter;

    public void setChatSocket(Socket sock){
        this.chatSocket = sock;
        this.mode = "tcp";
        characterCounter.setText("0/500");
    }

    public void setUdpChatSocket(DatagramSocket udpChatSocket, int portIn) {
        this.udpChatSocket = udpChatSocket;
        this.mode = "udp";
        this.portIn = portIn;
        this.udpNameField = "<" + myName + ">: ";
        try {
            this.udpNameLength = udpNameField.getBytes("UTF-8").length;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        // due to the chosen UDP max size, and the way udp messages are implemented, the max amount of
        // bytes which a user can transmit vary according to the length of his own name
        this.udpTextFieldSize = 512 - udpNameLength;
        characterCounter.setText("0/"+udpTextFieldSize);
    }

    // the AddLine functions print the line on screen
    // the tcp variant takes into account who the sender of that line is
    // the udp variant assumes the name of the sender to be baked with the line itself
    public void addLine(String username, String content){
        String[] contents = content.split("\n");
        for(String line: contents) {
            visualizingTextAreaItem.appendText("<" + username + "> " + line + "\n");
        }
    }

    public void addLine(String content){
        String[] contents = content.split("\n");
        for(String line: contents) {
            visualizingTextAreaItem.appendText(line + "\n");
        }
    }

    // simple function which updates the display on the bottom right according to the used characters
    // is called by the typingTextAreaItem onKeyTyped fxml property
    public void countCharacters(){
        String tmp = typingTextAreaItem.getText().trim();
        if(mode.equals("tcp")){
            // up to 500 bytes name excluded
            int byteCount = tmp.getBytes().length;
            characterCounter.setText(byteCount+"/500");
            if(byteCount > 500)
                characterCounter.setFill(Color.RED);
            else
                characterCounter.setFill(Color.BLACK);
        }else if(mode.equals("udp")){
            // up to 512 including name
            try {
                int byteCount = tmp.getBytes("UTF-8").length;
                characterCounter.setText(byteCount+"/"+udpTextFieldSize);
                if(byteCount > udpTextFieldSize)
                    characterCounter.setFill(Color.RED);
                else
                    characterCounter.setFill(Color.BLACK);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

        }
    }

    // reads and prepares the text found in typingTextAreaItem, then sends it to the proper socket according to
    // whichever kind of chat this controller is tied to.
    public void typeLine(){
        String tmp;
        if(mode.equals("tcp")) {
            tmp = typingTextAreaItem.getText().trim();
            if (!tmp.equals("") && tmp.getBytes().length < 500) {
                this.addLine(myName, tmp);
                Message sendLine = new Message("OP_FRD_CHT_MSG", tmp);
                try {
                    sendLine.send(chatSocket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                typingTextAreaItem.clear();
                typingTextAreaItem.positionCaret(0);
            }
        }else {
            StringBuilder tmpBuilder = new StringBuilder();
            try {
                tmp = typingTextAreaItem.getText().trim();
                if (!tmp.equals("")) {
                    tmpBuilder.append(udpNameField);
                    tmpBuilder.append(tmp);
                    String sendString = tmpBuilder.toString();
                    if(sendString.getBytes("UTF-8").length < 512) {
                        DatagramPacket p = new DatagramPacket(
                                sendString.getBytes("UTF-8"), 0,
                                sendString.getBytes("UTF-8").length,
                                address, portIn);
                        udpChatSocket.send(p);
                        typingTextAreaItem.clear();
                        typingTextAreaItem.positionCaret(0);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // listens to Enter button presses when the typingTextAreaItem is highlighted, then invokes the typeLine function
    public void keyListener(KeyEvent event){
        if(event.getCode() == KeyCode.ENTER) {
            typeLine();
        }
    }

    // used when the other end of the chat closes the connection, simply disables the text area
    public void lockWrite() {
        typingTextAreaItem.setEditable(false);
        typingTextAreaItem.setDisable(true);
        sendButton.setDisable(true);
    }

    // takes care to send the closing handshake, it's also called when the tab is closed from the x button
    public void onClose(String username) {
        Message msg = new Message();
        if(mode.equals("tcp")) {
            msg.setFields("OP_END_CHT", username);
        }else if (mode.equals("udp")){
            if(Controller.openGroupChats.containsKey(username)) {
                Controller.openGroupChats.replace(username, false);
            }
            msg.setFields("OP_LEV_GRP", username);
        }
        try {
            msg.send(CoreUI.myUser.getMySocket());
            if(mode.equals("udp")){
                // this is needed to eat up a reply the server will give us, which might include errors when closing from the menu component
                Message consumeReply = new Message();
                if(!Core.waitOkAnswer(consumeReply, myUser.getMySocket())) {
                    // this branch should never be triggered
                    Warning warning = new Warning("Chatroom Error!",
                            "Couldn't leave chatroom " + username,
                            consumeReply.getData());
                    Platform.runLater(warning);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
