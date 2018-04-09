package Server;

import Communication.Message;

import java.net.Socket;
import java.net.SocketTimeoutException;

// takes messages put on a socket and writes them into another
public class MessageRoutingThread extends Thread{
    private Socket in, out;
    private Message theMessage;
    private boolean translationRequired;
    private String languageIn, languageOut, outUserName;
    private User inUser;
    private ChatConnectionWrapper myWrapper;

    public MessageRoutingThread(Socket in, Socket out, boolean translationRequired, String languageIn, String languageOut, User inUser, String outUserName){
        this.in = in;
        this.out = out;
        this.theMessage = new Message();
        this.translationRequired = translationRequired;
        this.languageIn = languageIn;
        this.languageOut = languageOut;
        this.inUser = inUser;
        this.outUserName = outUserName;
        this.myWrapper = inUser.listOfConnections.get(outUserName);
    }

    @Override
    public void run() {
        while(!in.isClosed() && !out.isClosed() && myWrapper.isActive()){
            try {
                theMessage.receive(in);
            } catch (SocketTimeoutException e) {
                myWrapper.closeConnection();
            }
            if(theMessage.getData() != null) {
                if(translationRequired){
                    String tmpData = TranslationEngine.translateThis(theMessage.getData(), languageIn, languageOut);
                    theMessage.setFields(theMessage.getOperation(), tmpData);
                }
                try {
                    theMessage.send(out);
                } catch (java.io.IOException e) {
                    e.printStackTrace();
                    myWrapper.closeConnection();
                }
            }
            else {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println("Closing chat on the side of user "+inUser.getName()+"\nmyWrapper.isActive == " + myWrapper.isActive());
        inUser.listOfConnections.remove(outUserName);
    }
}
