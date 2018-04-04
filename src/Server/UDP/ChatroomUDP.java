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
        //System.out.println("Chatroom UDP " + ID + " started.");
        DatagramPacket packet = new DatagramPacket(new byte[LENGTH], LENGTH);
        try {
            InetAddress multicastGroup = InetAddress.getByName("239.1.1.1");
            while (chatroomsUDPcontrolArray.get(ID)) {
                socket.receive(packet);
                System.out.println("Chatroom " + ID + " received data.");
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
        }catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally{
            socket.close();
        }
        chatroomsUDPcontrolArray.remove(ID);
        System.out.println(ID + " thread closing.");
    }
}
