package Server;

import Communication.Message;

import java.net.Socket;

// takes messages put on a socket and writes them into another
public class MessageRoutingThread extends Thread{
    private Socket in, out;
    private Message theMessage;
    private boolean chatActive = true;

    public MessageRoutingThread(Socket in, Socket out){
        this.in = in;
        this.out = out;
        this.theMessage = new Message();
    }

    public void disableChat() {
        chatActive = false;
    }

    @Override
    public void run() {
        while(!in.isClosed() && !out.isClosed() && chatActive){
            theMessage.receive(in);
            theMessage.send(out);
        }
    }
}
