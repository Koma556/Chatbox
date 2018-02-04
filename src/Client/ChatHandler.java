package Client;

import Communication.Message;

import java.net.Socket;

public class ChatHandler extends Thread {
    private boolean done = false;
    private Socket chatSocket;
    private String friendUsername;

    public ChatHandler(Socket chatSocket, String friend){
        this.chatSocket = chatSocket;
        this.friendUsername = friend;
    }

    @Override
    public void run() {
        while(!chatSocket.isClosed() && chatSocket.isConnected() && !done){
            Message msg = new Message();
            msg.receive(chatSocket);
            // TODO: display on screen
            if(msg.getOperation()!= null)
                System.out.println("<" + friendUsername + ">: " + msg.getData());
        }
    }
}
