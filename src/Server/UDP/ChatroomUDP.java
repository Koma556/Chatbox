package Server.UDP;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;

import static Server.Core.chatroomsUDPcontrolArray;

public class ChatroomUDP implements Runnable {
    public final int LENGTH=512;

    private String ID;
    private int portIn, portOut;
    private DatagramSocket socket;

    public ChatroomUDP(String ID, int portIn, int portOut, DatagramSocket socket){
        this.ID = ID;
        this.portIn = portIn;
        this.portOut = portOut;
        this.socket = socket;
    }

    @Override
    public void run() {
        DatagramPacket packet = new DatagramPacket(new byte[LENGTH], LENGTH);
        try {
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
        }catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            // closing the chatroom by closing the socket
        } finally{
            chatroomsUDPcontrolArray.remove(ID);
            socket.close();
        }
    }
}
