package Client;

import Communication.Message;
import Communication.User;

import java.net.Socket;

public class chatHandler extends Thread {
    private boolean done = false;
    private Socket chatSocket;
    private User myUser;
    private String friendUsername;

    public chatHandler(User myUser, Socket chatSocket, String friend){
        this.chatSocket = chatSocket;
        this.myUser = myUser;
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
