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
    }
}
