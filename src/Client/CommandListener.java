package Client;

import Client.UI.TestUI;
import Communication.Message;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class CommandListener implements Runnable {
    private Socket sock, newChat;
    private Message commandMsg;
    private ServerSocket connectionToFriend;

    public CommandListener(){
        this.sock = TestUI.myUser.getMySocket();
    }

    @Override
    public void run() {
        System.out.println("Listener online");
        String tmpOperation;
        while(!sock.isClosed() && sock.isConnected()){
            commandMsg = new Message();
            commandMsg.receive(sock);
            if (commandMsg.getOperation() != null) {
                switch (tmpOperation = commandMsg.getOperation()){
                    // create a new serversocket on the shared known free port
                    case "OP_INC_FRD_MSG":{
                        try {
                            connectionToFriend = new ServerSocket(TestUI.myUser.getMyPort());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        // then connects the server to it
                        try {
                            newChat = connectionToFriend.accept();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Thread newChatHandler = new Thread(new ChatHandler(newChat, commandMsg.getData()));
                        newChatHandler.start();
                        Message reply = new Message("OP_OK", "Serversocket open.");
                        reply.send(sock);

                        break;
                    }
                }
            }else{
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        /*
        while(true){
            i++;
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Still alive");
        }
        */
    }
}
