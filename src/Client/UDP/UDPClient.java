package Client.UDP;

import Client.UI.chatPane.CreateTab;
import Client.UI.chatPane.UpdateTab;
import javafx.application.Platform;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class UDPClient implements Runnable {
    private int portIn, portOut;
    private String chatID;
    public final int LENGTH=512;

    public UDPClient(int portIn, int portOut, String chatID){
        this.portIn = portIn;
        this.portOut = portOut;
        this.chatID = chatID;
    }

    @Override
    public void run() {
        try(DatagramSocket s = new DatagramSocket()){

            CreateTab newTab = new CreateTab(chatID, s, portOut);
            Platform.runLater(newTab);
            while(true) {
                // TODO: Unrecognized Windows Sockets error: 0: Cannot bind
                try (MulticastSocket socket = new MulticastSocket(portIn);) {
                    DatagramPacket packet = new DatagramPacket(
                            new byte[LENGTH], LENGTH);
                    InetAddress multicastGroup = InetAddress.getByName(
                            "239.1.1.1");
                    socket.setSoTimeout(100000000);
                    socket.joinGroup(multicastGroup);
                    // TODO: Global variable to stop the chat
                    while (!Thread.interrupted()) {
                        socket.receive(packet);
                        // Print to tab
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
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
