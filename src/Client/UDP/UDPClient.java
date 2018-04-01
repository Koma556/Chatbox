package Client.UDP;

import Client.ChatWrapper;
import Client.UI.Controller;
import Client.UI.chatPane.ChatTabController;
import Client.UI.chatPane.CreateTab;
import Client.UI.chatPane.LockTab;
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
            try (MulticastSocket socket = new MulticastSocket(portOut);) {
                DatagramPacket packet = new DatagramPacket(
                        new byte[LENGTH], LENGTH);
                InetAddress multicastGroup = InetAddress.getByName(
                        "239.1.1.1");
                socket.setSoTimeout(10000);
                socket.joinGroup(multicastGroup);
                CreateTab newTab = new CreateTab(chatID, s, portOut);
                Platform.runLater(newTab);

                while (Controller.openChatTabs.get(chatID).isActive()) {
                    socket.receive(packet);
                    // Print to tab
                    String tmpStr = new String(
                            packet.getData(),
                            packet.getOffset(),
                            packet.getLength(),
                            "UTF-8");
                    UpdateTab upTab = new UpdateTab(chatID, tmpStr, "udp");
                    Platform.runLater(upTab);
                    if(tmpStr.equals("-Server Closing the Chatroom-")){
                        Controller.openChatTabs.get(chatID).setActive(false);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                LockTab locktab = new LockTab(chatID);
                Platform.runLater(locktab);
                s.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
