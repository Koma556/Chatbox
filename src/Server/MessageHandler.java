package Server;

import Communication.Message;

import java.io.IOException;
import java.net.Socket;

// this class requires:
// Two User objects from the server's database
public class MessageHandler extends Thread {
    private User userOne, userTwo;
    private boolean translationRequired;
    private Socket toUserOne, toUserTwo;
    private MessageRoutingThread forUserOne, forUserTwo;
    //private Message reply = new Message();

    public MessageHandler(User userOne, User userTwo, boolean translationRequired){
        this.userOne = userOne;
        this.userTwo = userTwo;
        this.translationRequired = translationRequired;
    }

    // this method will receive messages from one user and forward them to the other as long as the chat is considered open
    @Override
    public void run() {
        if(areLogged()){
            if(connectMessageStream()) {
                // send messages from user one and receive messages from user two
                forUserOne = new MessageRoutingThread(toUserTwo, toUserOne, translationRequired, userTwo.getLanguage(), userOne.getLanguage());
                forUserOne.start();
                // send messages from user two and receive messages for user one
                forUserTwo = new MessageRoutingThread(toUserOne, toUserTwo, translationRequired, userOne.getLanguage(), userTwo.getLanguage());
                forUserTwo.start();
            }
        }else{
            try {
                toUserOne.close();
                toUserTwo.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void closeConnection(){
        if (forUserTwo != null) {
            forUserTwo.disableChat();
            try {
                forUserTwo.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (forUserOne != null) {
            forUserOne.disableChat();
            try {
                forUserOne.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // opens a new socket towards both users by connecting to their respective ServerSockets
    private boolean connectMessageStream(){
        try {
            toUserOne = new Socket(userOne.getMySocket().getInetAddress(), userOne.getMyPort());
            // send user one the handshake message with the name of user two
            Message msg = new Message("OP_NEW_FCN", userTwo.getName());
            msg.send(toUserOne);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        try{
            toUserTwo = new Socket(userTwo.getMySocket().getInetAddress(), userTwo.getMyPort());
            Message msg = new Message("OP_NEW_FCN", userOne.getName());
            msg.send(toUserTwo);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    // tells me if both users are online
    private synchronized boolean areLogged(){
        return (userOne.isLogged() && userTwo.isLogged());
    }

}
