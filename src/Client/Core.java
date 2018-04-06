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

/* This class acts as a collection of static functions
 * to support networking operations between client and server
 */

public class Core {

    // tries to open a TCP socket with host at port
    public static Socket connect(String host, int port){
        InetAddress serverAddr = null;
        Socket sock = null;
        try{
            serverAddr = InetAddress.getByName(host);
        } catch (IOException e) {
            // using localhost instead
            try {
                serverAddr = InetAddress.getLocalHost();
            } catch (UnknownHostException e1) {
                BigErrorAlert bigErrorAlert = new BigErrorAlert("Couldn't connect!",
                        "No Server Found at address.",
                        "Server " + host + " or Port " + port + " busy, exiting client.", e);
                Platform.runLater(bigErrorAlert);
                System.exit(1);
            }
        }
        try{
            sock = new Socket(serverAddr, port);
        } catch (IOException e) {
            BigErrorAlert bigErrorAlert = new BigErrorAlert("Couldn't connect!",
                    "No Server Found at address.",
                    "Couldn't connect with server " + serverAddr + " at port " + port + ", exiting client.", e);
            Platform.runLater(bigErrorAlert);
            System.exit(1);
        }
        return sock;
    }

    // used by the RegisterController, sends a bundle of informations on the specified socket
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

    // waits for the reply from the server after an askRetrieveFriendList() call
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

    // waits for any "OP_OK" reply from the server for up to 10 seconds
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
