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
            reply.setFields("OP_ERR", "Partner offline");
            if (userTwo.getMySocket() == null || userTwo.getMySocket().isClosed())
                if (userOne.getMySocket() == null || userOne.getMySocket().isClosed())
                    reply = null;
                else
                    reply.send(userOne.getMySocket());
            else
                reply.send(userTwo.getMySocket());
        }
        // joins on the chat threads
        try {
            if(forUserOne != null)
                forUserOne.join();
            if(forUserTwo != null)
                forUserTwo.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void closeConnection(){
        if (forUserTwo != null)
            forUserTwo.disableChat();
        if (forUserOne != null)
            forUserOne.disableChat();
    }

    // sends user Two a message telling him to open a serversocket
    // waits for an OP_OK answer from user Two
    private boolean warnUserTwo(){
        if(areLogged()) {
            Message warning = new Message("OP_INC_FRD_MSG", userOne.getName());
            warning.send(userTwo.getMySocket());
            warning.receive(userTwo.getMySocket());
            /*
            while(warning.getOperation() == null && userTwo.getMySocket().isConnected() && !userTwo.getMySocket().isClosed()) {
                System.out.println("Looping inside warning.");
                if (warning.getOperation() != null && warning.getOperation().equals("OP_OK")) {
                    // NEVER ENDS UP HERE
                    System.out.println("Warning received an OP_OK");
                    return true;
                }
                else if (warning.getOperation() == null) {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }else if (warning.getOperation() != null && !warning.getOperation().equals("OP_OK"))
                    return false;
            }
            */
            return true;
        }
        return false;
    }

    // opens a new socket towards both users
    private boolean connectMessageStream(){
        if(warnUserTwo()) {
            System.out.println("Successfully warned user two.");
            try {
                toUserTwo = new Socket(userTwo.getCurrentUsrAddr(), userTwo.getMyPort());
                System.out.println("Opened a socket with user two.");
            } catch (IOException e) {
                System.out.println("Couldn't open a socket with user 2, " + userTwo.getName());
                reply = new Message("OP_ERR", "Your serversocket might be closed.");
                reply.send(userTwo.getMySocket());
            }
            try{
                reply = new Message("OP_OK", "Connecting.");
                reply.send(userOne.getMySocket());
                toUserOne = new Socket(userOne.getCurrentUsrAddr(), userOne.getMyPort());
                return true;
            } catch (IOException e) {
                System.out.println("Couldn't open a socket with user 1, " + userOne.getName());
                reply = new Message("OP_ERR", "Your serversocket might be closed.");
                reply.send(userOne.getMySocket());
            }
        }else{
            reply = new Message("OP_ERR", "User " + userTwo.getName() + " is not online.");
            reply.send(userOne.getMySocket());
        }
        return false;
    }

    // tells me if both users are online
    private synchronized boolean areLogged(){
        return (userOne.isLogged() && userTwo.isLogged());
    }

}
