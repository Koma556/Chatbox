package Client;

import Communication.Message;

import java.net.Socket;

public class Heartbeat implements Runnable{
    private boolean done = false;
    private Socket sock;
    private Message heartBeatMessage;

    public Heartbeat(Socket sock){
        this.sock = sock;
    }

    public void isDone(){
        this.done = true;
    }

    @Override
    public void run() {
        while(!done){
            heartBeatMessage = new Message("OP_HEARTBEAT", "");
            heartBeatMessage.send(sock);
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
    }
}
