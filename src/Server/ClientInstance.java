package Server;

import Communication.Message;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.concurrent.ConcurrentHashMap;

import static Server.Core.chatroomsUDPWrapper;

public class ClientInstance implements Runnable {
    private ConcurrentHashMap<String, User> clientDB;
    private int serverPort;
    private Message commandMsg;
    private boolean connected = false;
    private User myUser;
    private Socket sockCommands;
    //private HashMap<String, MessageHandler> listOfConnections = new HashMap<String, MessageHandler>();
    private String tmpOperation, tmpData;
    private int heartbeatTimer = 4000;

    // this class will take care to receive newly connected clients and handle their requests
    // sockCommands is non-null by definition, no need to check it
    public ClientInstance(ConcurrentHashMap<String, User> clientDB, Socket sock, int port){
        this.clientDB = clientDB;
        this.sockCommands = sock;
        try {
            sockCommands.setSoTimeout(2000);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        this.serverPort = port;
    }

    @Override
    public void run() {
        String replyCode = "";
        String replyData = "";
        int i = 0;
        while(!connected) {
            if(!sockCommands.isClosed() && sockCommands.isConnected()) {
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
                            if (clientDB.get(username).login(sockCommands)) {
                                // finally save the current user being handled by this instance of the server
                                myUser = clientDB.get(username);
                                myUser.setMyPort(Integer.parseInt(tmpDataArray[1]));
                                myUser.setMyNIOPort(Integer.parseInt(tmpDataArray[2]));
                                replyCode = "OP_OK_FRDL";
                                replyData = myUser.transmitFriendList();
                                commandMsg.setFields(replyCode, replyData);
                                try {
                                    commandMsg.send(sockCommands);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    break;
                                }
                                connected = true;
                            } else {
                                replyCode = "OP_ERR";
                                replyData = "User already logged.";
                                commandMsg.setFields(replyCode, replyData);
                                try {
                                    commandMsg.send(sockCommands);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    break;
                                }
                                break;
                            }
                        } else {
                            replyCode = "OP_ERR";
                            replyData = "User not registered.";
                            commandMsg.setFields(replyCode, replyData);
                            try {
                                commandMsg.send(sockCommands);
                            } catch (IOException e) {
                                e.printStackTrace();
                                break;
                            }
                            break;
                        }
                    } else if (commandMsg.getOperation().equals("OP_REGISTER")) {
                        String[] tmpDataArray = commandMsg.getData().split(",");
                        String username = tmpDataArray[0];
                        if (clientDB.containsKey(username)) {
                            replyCode = "OP_ERR";
                            replyData = "Username taken.";
                            commandMsg.setFields(replyCode, replyData);
                            try {
                                commandMsg.send(sockCommands);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            break;
                        } else {
                            myUser = new User(username, sockCommands);
                            clientDB.put(myUser.getName(), myUser);
                            myUser.login(sockCommands);
                            myUser.setMyPort(Integer.parseInt(tmpDataArray[1]));
                            myUser.setMyNIOPort(Integer.parseInt(tmpDataArray[2]));
                            myUser.setLanguage(tmpDataArray[3]);
                            replyCode = "OP_OK";
                            replyData = "User Registered.";
                            commandMsg.setFields(replyCode, replyData);
                            try {
                                commandMsg.send(sockCommands);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            connected = true;
                        }
                    } else {
                        replyCode = "OP_ERR";
                        replyData = "You must login or register first.";
                        commandMsg.setFields(replyCode, replyData);
                        try {
                            commandMsg.send(sockCommands);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                } else {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        if(connected)
            myUser.createListOfConnections();

        // while the user is logged in and the socket through which we talk to him is open and connected
        while(connected && !sockCommands.isClosed() && sockCommands.isConnected() && heartbeatTimer > 0) {
            try {
                commandMsg.receive(sockCommands);
            } catch (SocketTimeoutException e) {
                connected = false;
                break;
            }
            if (commandMsg.getOperation() != null) {
                tmpData = commandMsg.getData();
                switch(tmpOperation = commandMsg.getOperation()){
                    case "OP_HEARTBEAT":
                    {
                        heartbeatTimer = 4000;
                        break;
                    }
                    case "OP_LOGOUT":
                    {
                        connected = false;
                        replyCode = "OP_OK";
                        replyData = "User logged out.";
                        commandMsg.setFields(replyCode, replyData);
                        try {
                            commandMsg.send(sockCommands);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                    case "OP_MSG_FRD":
                    {
                        // checks if target is in your friend list
                        // runs a MessageHandler thread with args this user and target user, in this order
                        // saves a reference to the new MessageHandler so it can call a closeConnection() on it
                        if (myUser.isFriendWith(tmpData))
                        {
                            if(myUser.listOfConnections == null || myUser.listOfConnections.size() == 0 || !myUser.listOfConnections.containsKey(tmpData)) {
                                MessageHandler newMessageHandler = new MessageHandler(myUser, clientDB.get(tmpData), false);
                                myUser.listOfConnections.put(tmpData, new ChatConnectionWrapper());
                                clientDB.get(tmpData).listOfConnections.put(myUser.getName(), new ChatConnectionWrapper());
                                newMessageHandler.start();
                            }
                        }
                        break;
                    }
                    case "OP_END_CHT":
                    {
                        // checks if User has an open connection with said friend
                        // calls the closeConnection() method on it
                        if (myUser.listOfConnections.containsKey(tmpData)){
                            System.out.println("Trying to close the chat with "+ tmpData);
                            myUser.listOfConnections.get(tmpData).closeConnection();
                            myUser.listOfConnections.remove(tmpData);
                            if(clientDB.get(tmpData).listOfConnections.containsKey(myUser.getName())){
                                clientDB.get(tmpData).listOfConnections.remove(myUser.getName());
                            }
                        }
                        break;
                    }
                    case "OP_CRT_GRP":
                    {
                        String chatID = commandMsg.getData();
                        if(myUser.createChatGroup(chatID)){
                            // send the IN and OUT ports for the UDP room to the client, formatted as IN:OUT
                            commandMsg.setFields("OP_OK", chatroomsUDPWrapper.get(chatID).getPorts());
                            try {
                                commandMsg.send(sockCommands);
                            } catch (IOException e) {
                                e.printStackTrace();
                                connected = false;
                            }
                        }else{
                            commandMsg.setFields("OP_ERR", "Couldn't create Chatroom.");
                            try {
                                commandMsg.send(sockCommands);
                            } catch (IOException e) {
                                e.printStackTrace();
                                connected = false;
                            }
                        }
                        break;
                    }
                    case "OP_DEL_GRP":
                    {
                        String chatID = commandMsg.getData();
                        if(myUser.deleteChatGroup(chatID)){
                            commandMsg.setFields("OP_OK", "Group deleted.");
                        } else {
                            commandMsg.setFields("OP_ERR", "Group not deleted.");
                        }
                        try {
                            commandMsg.send(sockCommands);
                        } catch (IOException e) {
                            e.printStackTrace();
                            connected = false;
                        }
                        break;
                    }
                    case "OP_JON_GRP":
                    {
                        String chatID = commandMsg.getData();
                        if(myUser.joinChatGroup(chatID))
                            commandMsg.setFields("OP_OK", chatroomsUDPWrapper.get(chatID).getPorts());
                        else
                            commandMsg.setFields("OP_ERR", "Can't join Group.");
                        try {
                            commandMsg.send(sockCommands);
                        } catch (IOException e) {
                            e.printStackTrace();
                            connected = false;
                        }
                        break;
                    }
                    case "OP_LEV_GRP":
                    {
                        String chatID = commandMsg.getData();
                        myUser.leaveChatGroup(chatID);
                        break;
                    }
                    case "OP_GET_GRP":
                    {
                        commandMsg.setFields("OP_OK", myUser.getAllGroupsList());
                        try {
                            commandMsg.send(sockCommands);
                        } catch (IOException e) {
                            e.printStackTrace();
                            connected = false;
                        }
                        break;
                    }
                    case "OP_FRD_ADD":
                    {
                        // the OP_FRD_ADD operation doesn't expect an answer from the server
                        // instead it checks the status by requesting a new friend list
                        if (!myUser.isFriendWith(tmpData) && !tmpData.equals(myUser.getName())) {
                            if(clientDB.containsKey(tmpData)){
                                User tmpUsr = clientDB.get(tmpData);
                                myUser.addFriend(tmpData, clientDB);
                                clientDB.get(tmpData).addFriend(myUser.getName(), clientDB);
                                // TODO: ping user on its friendchatListener with OP_NEW_FRD
                                if(tmpUsr.isLogged()) {
                                    try {
                                        Socket tmpSock = new Socket(tmpUsr.getMySocket().getInetAddress(), tmpUsr.getMyPort());
                                        Message poke = new Message("OP_NEW_FRD", myUser.getName());
                                        poke.send(tmpSock);
                                        tmpSock.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                                commandMsg.setFields("OP_OK", "Friend added");
                            }else{
                                commandMsg.setFields("OP_ERR", "No such user");
                            }
                        }else{
                            commandMsg.setFields("OP_ERR", "Already friends");
                        }
                        break;
                    }
                    case "OP_FRDL_GET":
                    {
                        replyCode = "OP_OK_FRDL";
                        replyData = myUser.transmitFriendList();
                        commandMsg.setFields(replyCode, replyData);
                        try {
                            commandMsg.send(sockCommands);
                        } catch (IOException e) {
                            e.printStackTrace();
                            connected = false;
                        }
                        break;
                    }
                    case "OP_FRD_RMV":
                    {
                        if (myUser.isFriendWith(tmpData) && !tmpData.equals(myUser.getName())) {
                            myUser.removeFriend(tmpData);
                        }
                        break;
                    }
                    case "OP_SND_FIL":
                    {
                        // the only thing the server does here is confirm if the two users are friends
                        // and if the second user is online
                        // then it sends the inetaddress and port of the second user to the first
                        User tmpUser = clientDB.get(commandMsg.getData());
                        if(myUser.isFriendWith(commandMsg.getData()) && tmpUser.isLogged()){
                            String replyDataBuilder = tmpUser.getMySocket().getInetAddress().toString().replace("/", "") +
                                    ":" +
                                    tmpUser.getMyNIOPort();
                            commandMsg.setFields("OP_OK", replyDataBuilder);
                            try {
                                commandMsg.send(sockCommands);
                            } catch (IOException e) {
                                e.printStackTrace();
                                connected = false;
                            }
                        }else{
                            commandMsg.setFields("OP_ERR", "Not friends");
                            try {
                                commandMsg.send(sockCommands);
                            } catch (IOException e) {
                                e.printStackTrace();
                                connected = false;
                            }
                        }
                        break;
                    }
                    case "OP_TRS_MSG":
                    {
                        boolean requiresTranslation;
                        if (myUser.isFriendWith(tmpData))
                        {
                            if(myUser.listOfConnections == null || myUser.listOfConnections.size() == 0 || !myUser.listOfConnections.containsKey(tmpData)) {
                                // server checks if the two users share the same language
                                if(myUser.getLanguage().equals(clientDB.get(tmpData).getLanguage())) {
                                    requiresTranslation = false;
                                }else {
                                    requiresTranslation = true;
                                }
                                MessageHandler newMessageHandler = new MessageHandler(myUser, clientDB.get(tmpData), requiresTranslation);
                                myUser.listOfConnections.put(tmpData, new ChatConnectionWrapper());
                                clientDB.get(tmpData).listOfConnections.put(myUser.getName(), new ChatConnectionWrapper());
                                newMessageHandler.start();
                            }
                        }
                        break;
                    }
                    case "OP_LKP_USR":
                    {
                        if(clientDB.containsKey(commandMsg.getData())){
                            commandMsg.setFields("OP_OK", "User found.");
                        }else{
                            commandMsg.setFields("OP_ERR", "User not found.");
                        }
                        try {
                            commandMsg.send(sockCommands);
                        } catch (IOException e) {
                            e.printStackTrace();
                            connected = false;
                        }
                        break;
                    }
                    default:
                    {
                        break;
                    }
                }
            } else {
                try {
                    Thread.sleep(50);
                    heartbeatTimer = heartbeatTimer - 50;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            commandMsg.setFields(null, null);
        }
        if(myUser != null) {
            try {
                // close all currently open connection threads
                if(myUser.listOfConnections.size() != 0) {
                    String[] connections = myUser.listOfConnections.keySet().toArray(new String[myUser.listOfConnections.size()]);
                    for (String connection : connections) {
                        myUser.listOfConnections.get(connection).closeConnection();
                        myUser.listOfConnections.remove(connection);
                    }
                }
                myUser.logout();
                // if the connection has been cut by the heartbeat timer the RMI won't have been called properly
                // call the cleanup function for it
                if(heartbeatTimer <= 0){
                    myUser.cleanupRMI();
                }
                sockCommands.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
