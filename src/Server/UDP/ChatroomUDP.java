package Server.UDP;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import static Server.Core.chatroomsUDPcontrolArray;

public class ChatroomUDP implements Runnable {
    public final int LENGTH=512;

    private String ID;
    private int portIn, portOut;

    public ChatroomUDP(String ID, int portIn, int portOut){
        this.ID = ID;
        this.portIn = portIn;
        this.portOut = portOut;
    }

    @Override
    public void run() {
        //System.out.println("Chatroom UDP " + ID + " started.");
        try (DatagramSocket socket = new DatagramSocket(portIn);) {
            DatagramPacket packet = new DatagramPacket(
                    new byte[LENGTH], LENGTH);
            InetAddress multicastGroup = InetAddress.getByName("239.1.1.1");
            socket.setSoTimeout(10000);
            while (chatroomsUDPcontrolArray.get(ID)) {
                socket.receive(packet);
                //System.out.println("Chatroom "+ ID +" received data.");
                DatagramPacket multicastPacket =
                        new DatagramPacket(packet.getData(),
                                packet.getOffset(),
                                packet.getLength(),
                                multicastGroup, portOut);
                socket.send(multicastPacket);
            }
            // Goodbye message to all clients
            String goodbye = "-Server Closing the Chatroom-";
            DatagramPacket multicastPacket =
                    new DatagramPacket(goodbye.getBytes("UTF-8"),
                            0,
                            goodbye.getBytes("UTF-8").length,
                            multicastGroup, portOut);
            socket.send(multicastPacket);
            socket.close();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        chatroomsUDPcontrolArray.remove(ID);
    }
}
