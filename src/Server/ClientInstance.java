package Server;

import Communication.Message;
import Communication.User;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class ClientInstance implements Runnable {
    private ConcurrentHashMap<String, User> clientDB;
    private int serverPort;
    private Message reply, commandMsg;
    private boolean connected = false;
    private User myUser;
    private Socket sockCommands;
    private HashMap<String, MessageHandler> listOfConnections = new HashMap<String, MessageHandler>();
    private String tmpOperation, tmpData;

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
                            if (clientDB.get(commandMsg.getData()).login(sockCommands)) {
                                // finally save the current user being handled by this instance of the server
                                myUser = clientDB.get(commandMsg.getData());
                                replyCode = "OP_OK";
                                replyData = "User succesfully logged in.";
                                commandMsg.setFields(replyCode, replyData);
                                connected = true;
                            } else {
                                replyCode = "OP_ERR";
                                replyData = "User already logged.";
                                System.out.println(replyCode + ", " + replyData);
                                commandMsg.setFields(replyCode, replyData);
                                commandMsg.send(sockCommands);
                            }
                        } else {
                            replyCode = "OP_ERR";
                            replyData = "User not registered.";
                            System.out.println(replyCode + ", " + replyData);
                            commandMsg.setFields(replyCode, replyData);
                            commandMsg.send(sockCommands);
                        }
                    } else if (commandMsg.getOperation().equals("OP_REGISTER")) {
                        if (clientDB.containsKey(commandMsg.getData())) {
                            replyCode = "OP_ERR";
                            replyData = "Username taken.";
                            System.out.println(replyCode + ", " + replyData);
                            commandMsg.setFields(replyCode, replyData);
                            commandMsg.send(sockCommands);
                        } else {
                            myUser = new User(commandMsg.getData(), sockCommands);
                            clientDB.put(myUser.getName(), myUser);
                            myUser.login(sockCommands);
                            replyCode = "OP_OK";
                            replyData = "User Registered.";
                            connected = true;
                        }
                    } else {
                        replyCode = "OP_ERR";
                        replyData = "You must login or register first.";
                        System.out.println(replyCode + ", " + replyData);
                        commandMsg.setFields(replyCode, replyData);
                        commandMsg.send(sockCommands);
                    }
                }
            }
        }
        commandMsg.setFields(replyCode, replyData);
        System.out.println(replyCode+", " +replyData);
        commandMsg.send(sockCommands);
        // while the user is logged in and the socket through which we talk to him is open and connected
        while(connected && !sockCommands.isClosed() && sockCommands.isConnected()) {
            commandMsg.receive(sockCommands);
            if (commandMsg.getOperation() != null) {
                // TODO: write a comprehensive list of all commands and functions to handle them.
                tmpData = commandMsg.getData();
                switch(tmpOperation = commandMsg.getOperation()){
                    case "OP_LOGOUT":
                    {
                        connected = false;
                        replyCode = "OP_OK";
                        replyData = "You are logged out.";
                        commandMsg.setFields(replyCode, replyData);
                        commandMsg.debugPrint();
                        commandMsg.send(sockCommands);
                        myUser.logout();
                        break;
                    }
                    case "OP_MSG_FRD":
                    {
                        // checks if target is in your friend list
                        // runs a MessageHandler thread with args this user and target user, in this order
                        // saves a reference to the new MessageHandler so it can call a closeConnection() on it
                        if (myUser.isFriendWith(tmpData))
                        {
                            commandMsg.setFields("OP_OK", "Connecting.");
                            commandMsg.debugPrint();
                            listOfConnections.put(tmpData, new MessageHandler(myUser, clientDB.get(tmpData)));
                        }
                        else{
                            commandMsg.setFields("OP_ERR", "Not a friend");
                            commandMsg.debugPrint();
                        }
                        break;
                    }
                    case "OP_BYE_FRD":
                    {
                        // checks if User has an open connection with said friend
                        // calls the closeConnection() method on it
                        if (listOfConnections.containsKey(tmpData)){
                            listOfConnections.get(tmpData).closeConnection();
                        }
                        break;
                    }
                    case "OP_MSG_GRP":
                    {

                    }
                    case "OP_CRT_GRP":
                    {

                    }
                    case "OP_DEL_GRP":
                    {

                    }
                    case "OP_FRD_ADD":
                    {
                        if (!myUser.isFriendWith(tmpData)) {
                            if(clientDB.containsKey(tmpData)){
                                myUser.addFriend(tmpData, clientDB);
                                commandMsg.setFields("OP_OK", "Friend added");
                                commandMsg.debugPrint();
                            }else{
                                commandMsg.setFields("OP_ERR", "No such user");
                                commandMsg.debugPrint();
                            }
                        }else{
                            commandMsg.setFields("OP_ERR", "Already friends");
                            commandMsg.debugPrint();
                        }
                        break;
                    }
                    case "OP_FRD_RMV":
                    {

                    }
                    case "OP_SND_FIL":
                    {

                    }
                    case "OP_TRS_MSG":
                    {

                    }
                    default:
                    {
                        /*send error message*/
                        break;
                    }
                }
            }
        }
        System.out.println(myUser.getName() + " socket closed");
        try {
            // do a join on the open connection threads
            String[] connections = listOfConnections.keySet().toArray(new String[listOfConnections.size()]);
            for(String connection : connections){
                listOfConnections.get(connection).closeConnection();
                listOfConnections.get(connection).join();
            }
            myUser.logout();
            sockCommands.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
