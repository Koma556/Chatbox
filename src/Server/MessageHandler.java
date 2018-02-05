package Server;

import Communication.Message;
import Communication.User;

import java.io.IOException;
import java.net.Socket;

// this class does the following:
// 1. Connect user A and B together if both are online
// 2. Route messages from A to B and vice versa if both are online
// 3. Warn user B that user A wants to start sending messages
// this class requires:
// Two User objects from the server's database
public class MessageHandler extends Thread {
    private User userOne, userTwo;
    private boolean chatIsOpen;
    private Socket toUserOne, toUserTwo;
    private MessageRoutingThread forUserOne, forUserTwo;
    private Message reply = new Message();

    public MessageHandler(User userOne, User userTwo){
        this.userOne = userOne;
        this.userTwo = userTwo;
    }

    // this method will receive messages from one user and forward them to the other as long as the chat is considered open
    @Override
    public void run() {
        if(areLogged()){
            if(connectMessageStream()) {
                // send messages from user one and receive messages from user two
                forUserOne = new MessageRoutingThread(toUserTwo, toUserOne);
                forUserOne.start();
                System.out.println("Started routing from two to one.");
                // send messages from user two and receive messages for user one
                forUserTwo = new MessageRoutingThread(toUserOne, toUserTwo);
                forUserTwo.start();
                System.out.println("Started routing from one to two.");
            }
        }else{
            System.out.println("One of the hosts went offline.");
            try {
                toUserOne.close();
                toUserTwo.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void closeConnection(){
        if (forUserTwo != null)
            forUserTwo.disableChat();
        if (forUserOne != null)
            forUserOne.disableChat();
    }

    // opens a new socket towards both users by connecting to their respective ServerSockets
    private boolean connectMessageStream(){
        try {
            toUserOne = new Socket(userOne.getMySocket().getInetAddress(), userOne.getMyPort());
            // send user one the handshake message with the name of user two
            Message msg = new Message("OP_NEW_FCN", userTwo.getName());
            msg.send(toUserOne);
            toUserTwo = new Socket(userTwo.getMySocket().getInetAddress(), userTwo.getMyPort());
            msg = new Message("OP_NEW_FCN", userOne.getName());
            msg.send(toUserTwo);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Couldn't bind with user one and two's ServerSockets on port 57382.");
            return false;
        }
        return true;
    }

    // tells me if both users are online
    private synchronized boolean areLogged(){
        return (userOne.isLogged() && userTwo.isLogged());
    }

}
