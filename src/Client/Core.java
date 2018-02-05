package Client;

import Client.UI.TestUI;
import Communication.Message;


import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class Core {

    public static Socket connect(String username, String host, int port){
        InetAddress serverAddr = null;
        Socket sock = null;

        //TODO: change debug prints to error windows
        try{
            serverAddr = InetAddress.getByName(host);
        } catch (IOException e) {
            System.out.println("Invalid address; Using localhost");
            try {
                serverAddr = InetAddress.getLocalHost();
            } catch (UnknownHostException e1) {
                System.out.println("No server found on localhost at port "+port+", exiting client.");
                System.exit(1);
            }
        }
        try{
            System.out.println("Attempting connection with "+host+" on port "+port);
            sock = new Socket(serverAddr, port);
        } catch (IOException e) {
            System.out.println("Couldn't open a socket with the server.");
            System.exit(1);
        }
        return sock;
    }

    public static boolean Register(String username, Socket server) {
        Message msg = new Message("OP_REGISTER", username);
        msg.send(server);
        System.out.println("Sent registration message.");

        return waitOkAnswer(msg, server);
    }

    public static String[] Login(String username, Socket server) {
        Message msg = new Message("OP_LOGIN", username);
        msg.send(server);
        System.out.println("Sent login message.");

        return retrieveFriendList(msg, server);
    }

    public static boolean Logout(String username, Socket server){
        Message msg = new Message("OP_LOGOUT", username);
        msg.send(server);
        System.out.println("Sent logout message.");

        return waitOkAnswer(msg, server);
    }

    public static void askRetrieveFriendList(){
        Message msg = new Message("OP_FRDL_GET", TestUI.myUser.getName());
        msg.send(TestUI.myUser.getMySocket());
        TestUI.myUser.setTmpFriendList(retrieveFriendList(msg, TestUI.myUser.getMySocket()));
    }

    private static String[] retrieveFriendList(Message msg, Socket server){
        boolean done = false;
        while (!done) {
            msg.receive(server);
            if (msg.getOperation() != null) {
                if (msg.getOperation().equals("OP_OK_FRDL")) {
                    done =true;
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

    public static boolean waitOkAnswer(Message msg, Socket server){
        boolean done = false;
        while (!done) {
            msg.receive(server);
            if (msg.getOperation() != null) {
                if (msg.getOperation().equals("OP_OK")) {
                    done = true;
                    return true;
                }
                else {
                    done = true;
                    return false;
                }
            }
        }
        return false;
    }
}
