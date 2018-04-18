package Server.UDP;

import Server.Core;

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
        String soundOfSilence = "--You are the only user in this Channel--";
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
                // warn user he's alone in the chatroom
                if(Core.chatroomsUDPWrapper.get(ID).numberOfUsers() == 1){
                    DatagramPacket silencePacket =
                            new DatagramPacket(soundOfSilence.getBytes("UTF-8"),
                                    0,
                                    soundOfSilence.getBytes("UTF-8").length,
                                    multicastGroup, portOut);
                    socket.send(silencePacket);
                }
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
