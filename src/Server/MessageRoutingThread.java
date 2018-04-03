package Server;

import Communication.Message;

import java.net.Socket;
import java.net.SocketTimeoutException;

// takes messages put on a socket and writes them into another
public class MessageRoutingThread extends Thread{
    private Socket in, out;
    private Message theMessage;
    private boolean chatActive, translationRequired;
    private String languageIn, languageOut;

    public MessageRoutingThread(Socket in, Socket out, boolean translationRequired, String languageIn, String languageOut){
        this.in = in;
        this.out = out;
        this.theMessage = new Message();
        this.translationRequired = translationRequired;
        this.languageIn = languageIn;
        this.languageOut = languageOut;
    }

    public void disableChat() {
        chatActive = false;
    }

    @Override
    public void run() {
        chatActive = true;
        while(!in.isClosed() && !out.isClosed() && chatActive){
            try {
                theMessage.receive(in);
            } catch (SocketTimeoutException e) {
                chatActive = false;
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
                }
            }
            else
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
        }
        if(out.isConnected() && !out.isClosed()) {
            theMessage.setFields("OP_END_CHT", "");
            try {
                theMessage.send(out);
            } catch (java.io.IOException e) {
                e.printStackTrace();
            }
        }
        if(in.isConnected() && !in.isClosed()){
            theMessage.setFields("OP_END_CHT", "");
            try {
                theMessage.send(out);
            } catch (java.io.IOException e) {
                e.printStackTrace();
            }
        }
    }
}
