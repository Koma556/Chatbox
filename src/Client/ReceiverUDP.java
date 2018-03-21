package Client;

import Client.UI.TestUI;
import Client.UI.chatPane.CreateTab;
import javafx.application.Platform;

// called after the user joins or creates a Multicast Group.
public class ReceiverUDP implements Runnable{
    private String chatID;

    public ReceiverUDP(String chatID){
        this.chatID = chatID;
    }

    @Override
    public void run() {
        // establish UPD connection
        // start new chat tab
        /*
        CreateTab newTab = new CreateTab(chatID, UDPchatSocket);
        Platform.runLater(newTab);
        */
    }
}
