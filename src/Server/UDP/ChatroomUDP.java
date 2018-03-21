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

        try (DatagramSocket socket = new DatagramSocket(portIn);) {
            DatagramPacket packet = new DatagramPacket(
                    new byte[LENGTH], LENGTH);
            InetAddress multicastGroup = InetAddress.getByName("239.1.1.1");
            while (chatroomsUDPcontrolArray.get(ID)) {
                socket.receive(packet);
                DatagramPacket multicastPacket =
                        new DatagramPacket(packet.getData(),
                                packet.getOffset(),
                                packet.getLength(),
                                multicastGroup, portOut);
                socket.send(multicastPacket);
            }
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        chatroomsUDPcontrolArray.remove(ID);
    }
}
