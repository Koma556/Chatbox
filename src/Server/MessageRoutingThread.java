package Server;

import Communication.Message;

import java.net.Socket;
import java.net.SocketTimeoutException;

// takes messages put on a socket and writes them into another
public class MessageRoutingThread extends Thread{
    private Socket in, out;
    private Message theMessage;
    private boolean chatActive;

    public MessageRoutingThread(Socket in, Socket out){
        this.in = in;
        this.out = out;
        this.theMessage = new Message();
    }

    public void disableChat() {
        chatActive = false;
        System.out.println("CLOSING CHAT");
    }

    @Override
    public void run() {
        chatActive = true;
        while(!in.isClosed() && !out.isClosed() && chatActive){
            try {
                theMessage.receive(in);
            } catch (SocketTimeoutException e) {
                chatActive = false;
            }
            if(theMessage.getData() != null)
                theMessage.send(out);
            else
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
        }
        if(out.isConnected() && !out.isClosed()) {
            theMessage.setFields("OP_END_CHT", "");
            theMessage.send(out);
        }
        if(in.isConnected() && !in.isClosed()){
            theMessage.setFields("OP_END_CHT", "");
            theMessage.send(out);
        }
    }
}
