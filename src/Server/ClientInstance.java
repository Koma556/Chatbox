package Server;

import Communication.Message;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class ClientInstance implements Runnable {
    private ConcurrentHashMap<String, User> clientDB;
    private int serverPort;
    private Message reply, commandMsg;
    private boolean connected = false;
    private User myUser;
    // sockMessages is a reusable socket this handler will open and close to send messages towards this client
    private Socket sockCommands, sockMessages = null;

    // this class will take care to receive newly connected clients and handle their requests
    // sockCommands is non-null by definition, no need to check it
    public ClientInstance(ConcurrentHashMap<String, User> clientDB, Socket sock, int port){
        this.clientDB = clientDB;
        this.sockCommands = sock;
        this.serverPort = port;
    }

    @Override
    public void run() {
        String replyCode = "";
        String replyData = "";
        int i = 0;
        while(!connected) {
            if(!sockCommands.isClosed()) {

                commandMsg = new Message();
                commandMsg.receive(sockCommands);

                if (commandMsg.getOperation() != null) {

                    if (commandMsg.getOperation().equals("OP_LOGIN")) {
                        if (clientDB.containsKey(commandMsg.getData())) {
                            // the login method takes care of concurrency
                            // TODO: debug logout function
                            if (clientDB.get(commandMsg.getData()).login()) {
                                // finally save the current user being handled by this instance of the server
                                myUser = clientDB.get(commandMsg.getData());
                                replyCode = "OP_OK";
                                replyData = "User succesfully logged in.";
                                connected = true;
                            } else {
                                replyCode = "OP_ERR";
                                replyData = "User already logged.";
                                System.out.println(replyCode + ", " + replyData);
                            }
                        } else {
                            replyCode = "OP_ERR";
                            replyData = "User not registered.";
                            System.out.println(replyCode + ", " + replyData);
                        }
                    } else if (commandMsg.getOperation().equals("OP_REGISTER")) {
                        if (clientDB.containsKey(commandMsg.getData())) {
                            replyCode = "OP_ERR";
                            replyData = "Username taken.";
                            System.out.println(replyCode + ", " + replyData);
                        } else {
                            myUser = new User(commandMsg.getData(), sockCommands);
                            clientDB.put(myUser.getName(), myUser);
                            myUser.login();
                            replyCode = "OP_OK";
                            replyData = "User Registered.";
                            connected = true;
                        }
                    } else {
                        replyCode = "OP_ERR";
                        replyData = "You must login or register first.";
                        System.out.println(replyCode + ", " + replyData);
                    }
                }
            }
        }
        //reply = new Message(replyCode, replyData, sockMessages);
        System.out.println(replyCode+", " +replyData);
        while(connected) {
            commandMsg.receive(sockCommands);

            if (commandMsg.getOperation() != null) {
                if(commandMsg.getOperation().equals("OP_CLOSE"))
                    connected = false;
            }
            /*
            switch (commandMsg.getOperation()) {
                // TODO: write a comprehensive list of all commands and functions to handle them.
            }
            */
        }
        myUser.logout();
        try {
            sockCommands.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
