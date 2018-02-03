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

    public MessageHandler(User userOne, User userTwo){
        this.userOne = userOne;
        this.userTwo = userTwo;
    }

    // this method will receive messages from one user and forward them to the other as long as the chat is considered open
    @Override
    public void run() {
        connectMessageStream();
        if(areLogged()){
            // send messages from user one and receive messages from user two
            forUserOne = new MessageRoutingThread(toUserTwo, toUserOne);
            forUserOne.start();
            // send messages from user two and receive messages for user one
            forUserTwo = new MessageRoutingThread(toUserOne, toUserTwo);
            forUserTwo.start();
        }else{
            // TODO: return error message here depending on who's closed
        }
        // TODO: close the chat
        try {
            forUserOne.join();
            forUserTwo.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void closeConnection(){
        forUserTwo.disableChat();
        forUserOne.disableChat();
    }

    // sends user Two a message telling him to open a serversocket
    // waits for an OP_OK answer from user Two
    private boolean warnUserTwo(){
        if(areLogged()) {
            Message warning = new Message("OP_INC_FRD_MSG", userOne.getName());
            warning.send(userTwo.getMySocket());
            warning.receive(userTwo.getMySocket());
            if(warning.getOperation().equals("OP_OK"))
                return true;
        }
        return false;
    }

    // opens a new socket towards both users
    private void connectMessageStream(){
        // TODO: reply with an error to the client
        try{
            toUserOne = new Socket(userOne.getCurrentUsrAddr(), userOne.getMyPort());
        } catch (IOException e) {
            System.out.println("Couldn't open a socket with user 1, " + userOne.getName());
        }
        try{
            toUserTwo = new Socket(userTwo.getCurrentUsrAddr(), userTwo.getMyPort());
        } catch (IOException e) {
            System.out.println("Couldn't open a socket with user 2, " + userTwo.getName());
        }
    }

    // tells me if both users are online
    private synchronized boolean areLogged(){
        return (userOne.isLogged() && userTwo.isLogged());
    }

}
