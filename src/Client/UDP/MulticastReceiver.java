package Client.UDP;

import Client.UI.chatPane.ChatTabController;
import Client.UI.chatPane.UpdateTab;
import javafx.application.Platform;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class MulticastReceiver implements Runnable{
    private int portIn;
    private String chatID;
    public final int LENGTH=512;

    public MulticastReceiver(int portIn, String chatID){
        this.portIn = portIn;
        this.chatID = chatID;
    }

    @Override
    public void run() {
/*
        try(MulticastSocket socket = new MulticastSocket(portIn);){
            DatagramPacket packet = new DatagramPacket(
                    new byte[LENGTH], LENGTH);
            InetAddress multicastGroup= InetAddress.getByName(
                    "239.1.1.1");
            socket.setSoTimeout(100000000);
            socket.joinGroup(multicastGroup);
            // TODO: Global variable to stop the chat
            while(!Thread.interrupted()){
                socket.receive(packet);
                // Print to tab
                // TODO: parse message string to split user from text
                UpdateTab upTab = new UpdateTab(chatID, new String(
                        packet.getData(),
                        packet.getOffset(),
                        packet.getLength(),
                        "UTF-8"), "udp");
                Platform.runLater(upTab);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
*/
    }

}