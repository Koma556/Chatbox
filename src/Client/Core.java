package Client;

import Client.UI.PopupWindows.BigErrorAlert;
import Client.UI.TestUI;
import Communication.Message;
import javafx.application.Platform;


import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class Core {

    public static Socket connect(String username, String host, int port){
        InetAddress serverAddr = null;
        Socket sock = null;

        try{
            serverAddr = InetAddress.getByName(host);
        } catch (IOException e) {
            //System.out.println("Invalid address; Using localhost");
            try {
                serverAddr = InetAddress.getLocalHost();
            } catch (UnknownHostException e1) {
                BigErrorAlert bigErrorAlert = new BigErrorAlert("Couldn't connect!","No Server Found at address.", "Port " + port + " busy, exiting client.", e);
                Platform.runLater(bigErrorAlert);
                //System.out.println("No server found on localhost at port "+port+", exiting client.");
                System.exit(1);
            }
        }
        try{
            //System.out.println("Attempting connection with "+host+" on port "+port);
            sock = new Socket(serverAddr, port);
        } catch (IOException e) {
            BigErrorAlert bigErrorAlert = new BigErrorAlert("Couldn't connect!","No Server Found at address.", "Couldn't connect with server " + serverAddr + " at port " + port + ", exiting client.", e);
            //System.out.println("Couldn't open a socket with the server.");
            System.exit(1);
        }
        return sock;
    }

    public static boolean Register(String dataBundle, Socket server) {
        Message msg = new Message("OP_REGISTER", dataBundle);
        try {
            msg.send(server);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return waitOkAnswer(msg, server);
    }

    public static String[] Login(String username, Socket server) {
        Message msg = new Message("OP_LOGIN", username);
        try {
            msg.send(server);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return retrieveFriendList(server);
    }

    public static void Logout(String username, Socket server){
        Message msg = new Message("OP_LOGOUT", username);
        try {
            msg.send(server);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void askRetrieveFriendList(){
        Message msg = new Message("OP_FRDL_GET", TestUI.myUser.getName());
        try {
            msg.send(TestUI.myUser.getMySocket());
        } catch (IOException e) {
            e.printStackTrace();
        }
        TestUI.myUser.setTmpFriendList(retrieveFriendList(TestUI.myUser.getMySocket()));
    }

    private static String[] retrieveFriendList(Socket server){
        boolean done = false;
        Message msg = new Message();
        while (!done) {
            try {
                msg.receive(server);
            } catch (SocketTimeoutException e) {
                done = true;
                e.printStackTrace();
                return null;
            }
            if (msg.getOperation() != null) {
                if (msg.getOperation().equals("OP_OK_FRDL")) {
                    done = true;
                    return msg.getData().split(",");
                }
                else {
                    done = true;
                    return null;
                }
            }
        }
        return null;
    }

    public static ArrayList<String> getListOfMulticastGroups(){
        Message msg = new Message("OP_GET_GRP", "");
        try {
            msg.send(TestUI.myUser.getMySocket());
        } catch (IOException e) {
            e.printStackTrace();
        }
        ArrayList<String> returnVal = new ArrayList<>();
        Message reply = new Message();
        if(waitOkAnswer(reply, TestUI.myUser.getMySocket())){
            if(reply.getData() != null) {
                String[] tmp = reply.getData().split(",");
                for (String data : tmp
                        ) {
                    returnVal.add(data);
                }
            }
        }

        return returnVal;
    }


    public static boolean waitOkAnswer(Message msg, Socket server){
        boolean done = false;
        int timeout = 10000;
        while (!done && timeout != 0) {
            try {
                msg.receive(server);
            } catch (SocketTimeoutException e) {
                // not working :(
                done = true;
                return false;
            }
            if (msg.getOperation() != null) {
                if (msg.getOperation().equals("OP_OK")) {
                    done = true;
                    return true;
                }
                else {
                    done = true;
                    return false;
                }
            }else{
                timeout = timeout -50;
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }
}
