package Client;

import Client.UI.Controller;
import Client.UI.TestUI;
import Client.UI.chatPane.ChatTabController;
import Communication.Message;
import javafx.scene.control.Tab;

import java.net.DatagramSocket;
import java.net.Socket;

public class ChatWrapper {
    private boolean active;
    private String username, mode;
    private Tab tab;
    private ChatTabController controller;
    private Socket sock;
    private DatagramSocket dataSock;

    public ChatWrapper(String username, boolean active) {
        this.username = username;
        this.active = active;
    }

    public Socket getSock() {
        return sock;
    }

    public void setSock(Socket sock) {
        this.sock = sock;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public Tab getTab() {
        return tab;
    }

    public void setTab(Tab tab) {
        this.tab = tab;
    }

    public ChatTabController getController() {
        return controller;
    }

    public void setController(ChatTabController controller) {
        this.controller = controller;
    }

    public void onClose(){
        if(mode.equals("tcp")) {
            Message msg = new Message("OP_END_CHT", "");
            if (!sock.isClosed())
                msg.send(sock);
        }
        active = false;
        Controller.allActiveChats.remove(username);
        Controller.openChatTabs.remove(username);
    }

    public void setDatagramSocket(DatagramSocket sock) {
        this.dataSock = sock;
    }

    public DatagramSocket getDatagramSock(){
        return dataSock;
    }
}
