package Server;

import Communication.Message;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
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
                try {
                    commandMsg.receive(sockCommands);
                } catch (SocketTimeoutException e) {
                    break;
                }

                if (commandMsg.getOperation() != null) {
                    if (commandMsg.getOperation().equals("OP_LOGIN")) {
                        String[] tmpDataArray = commandMsg.getData().split(",");
                        String username = tmpDataArray[0];
                        if (clientDB.containsKey(username)) {
                            // the login method takes care of concurrency
                            // TODO: debug logout function
                            if (clientDB.get(username).login(sockCommands)) {
                                // finally save the current user being handled by this instance of the server
                                myUser = clientDB.get(username);
                                myUser.setMyPort(Integer.parseInt(tmpDataArray[1]));
                                replyCode = "OP_OK_FRDL";
                                replyData = myUser.transmitFriendList();
                                commandMsg.setFields(replyCode, replyData);
                                System.out.println(replyCode+", " +replyData);
                                commandMsg.send(sockCommands);
                                connected = true;
                            } else {
                                replyCode = "OP_ERR";
                                replyData = "User already logged.";
                                commandMsg.setFields(replyCode, replyData);
                                System.out.println(replyCode+", " +replyData);
                                commandMsg.send(sockCommands);
                            }
                        } else {
                            replyCode = "OP_ERR";
                            replyData = "User not registered.";
                            commandMsg.setFields(replyCode, replyData);
                            System.out.println(replyCode+", " +replyData);
                            commandMsg.send(sockCommands);
                        }
                    } else if (commandMsg.getOperation().equals("OP_REGISTER")) {
                        String[] tmpDataArray = commandMsg.getData().split(",");
                        String username = tmpDataArray[0];
                        if (clientDB.containsKey(username)) {
                            replyCode = "OP_ERR";
                            replyData = "Username taken.";
                            commandMsg.setFields(replyCode, replyData);
                            System.out.println(replyCode+", " +replyData);
                            commandMsg.send(sockCommands);
                        } else {
                            myUser = new User(username, sockCommands);
                            clientDB.put(myUser.getName(), myUser);
                            myUser.login(sockCommands);
                            myUser.setMyPort(Integer.parseInt(tmpDataArray[1]));
                            replyCode = "OP_OK";
                            replyData = "User Registered.";
                            commandMsg.setFields(replyCode, replyData);
                            System.out.println(replyCode+", " +replyData);
                            commandMsg.send(sockCommands);
                            connected = true;
                        }
                    } else {
                        replyCode = "OP_ERR";
                        replyData = "You must login or register first.";
                        System.out.println(replyCode + ", " + replyData);
                        commandMsg.setFields(replyCode, replyData);
                        System.out.println(replyCode+", " +replyData);
                        commandMsg.send(sockCommands);
                    }
                } else
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
            }
        }

        // while the user is logged in and the socket through which we talk to him is open and connected
        while(connected && !sockCommands.isClosed() && sockCommands.isConnected()) {
            try {
                commandMsg.receive(sockCommands);
            } catch (SocketTimeoutException e) {
                break;
            }
            if (commandMsg.getOperation() != null) {
                // TODO: write a comprehensive list of all commands and functions to handle them.
                tmpData = commandMsg.getData();
                switch(tmpOperation = commandMsg.getOperation()){
                    case "OP_LOGOUT":
                    {
                        connected = false;
                        replyCode = "OP_OK";
                        replyData = "User logged out.";
                        commandMsg.setFields(replyCode, replyData);
                        commandMsg.debugPrint();
                        commandMsg.send(sockCommands);
                        // and close all active connections
                        String[] myConnections = listOfConnections.keySet().toArray(new String[listOfConnections.size()]);
                        for(String connection : myConnections){
                            listOfConnections.get(connection).closeConnection();
                        }
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
                            MessageHandler newMessageHandler = new MessageHandler(myUser, clientDB.get(tmpData));
                            System.out.println("NEW CHAT FROM " + myUser.getName() + " TO " + tmpData);
                            listOfConnections.put(tmpData, newMessageHandler);
                            commandMsg.setFields(null, null);
                            newMessageHandler.start();
                        }
                        break;
                    }
                    // TODO: REMOVE COMMAND
                    case "OP_END_CHT":
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
                    case "OP_FRDL_GET":
                    {
                        replyCode = "OP_OK_FRDL";
                        replyData = myUser.transmitFriendList();
                        commandMsg.setFields(replyCode, replyData);
                        System.out.println(replyCode+", " +replyData);
                        commandMsg.send(sockCommands);
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
            } else
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
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
