package Client.UDP;

import Client.UI.Controller;
import Client.UI.CoreUI;
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
                socket.setSoTimeout(10000000);
                socket.joinGroup(multicastGroup);
                CreateTab newTab = new CreateTab(chatID, s, portIn);
                System.out.println("PortIn: " + portIn + "\nPortOut: " + portOut);
                Platform.runLater(newTab);
                // register this group with chatID key to the control hashmap openGroupChats
                Controller.openGroupChats.put(chatID, true);
                // while on the control variable
                // calls a nullpointerexception if in execution when a user is logging out
                try {
                    while (Controller.openGroupChats.get(chatID)) {
                        socket.receive(packet);
                        // Print to tab
                        String tmpStr = new String(
                                packet.getData(),
                                packet.getOffset(),
                                packet.getLength(),
                                "UTF-8");
                        System.out.println(tmpStr);
                        UpdateTab upTab = new UpdateTab(chatID, tmpStr, "udp");
                        Platform.runLater(upTab);
                        /* since the thread actually blocks on the receive method of the udp socket I use
                         * the received string to parse whether or not this thread should actually close
                         * this is useful when the close has been initiated by the user himself rather than the server
                         * because in that case, if no other user on the chat room sends a message and the chatroom itself
                         * is never closed, the receive method would never unlock.
                         * this could also be prevented by closing the socket itself, but since the client is already
                         * built in such a way that all sent messages, even the user's own, are echoed back to it,
                         * there is no way to try a more elegant solution.
                         * On top of that given the lack of a user list for chat rooms visible to all users,
                         * a notification that someone else left the chat might be most welcome.
                         */
                        if (tmpStr.equals("-Server Closing the Chatroom-") || tmpStr.equals("-User " + CoreUI.myUser.getName() + " left the group-")) {
                            if (Controller.openGroupChats.containsKey(chatID)) {
                                // close the loop and lock tab writes in case the leave action was not called from the tab close button
                                // or in case the chat room was closed by the server for whichever reason
                                Controller.openGroupChats.replace(chatID, false);
                                LockTab locktb = new LockTab(chatID);
                                Platform.runLater(locktb);
                            }
                        }
                    }
                } catch (NullPointerException e){
                    // user is logging out
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                Controller.allActiveChats.remove(chatID);
                Controller.openGroupChats.remove(chatID);
                s.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
