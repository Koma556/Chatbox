package Client;

import Communication.GetProperties;
import Communication.Message;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;


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

    public static boolean Login(String username, Socket server) {
        Message msg = new Message("OP_LOGIN", username);
        msg.send(server);
        System.out.println("Sent login message.");

        return waitOkAnswer(msg, server);
    }

    public static boolean Logout(String username, Socket server){
        Message msg = new Message("OP_LOGOUT", username);
        msg.send(server);
        System.out.println("Sent login message.");

        return waitOkAnswer(msg, server);
    }

    private static boolean waitOkAnswer(Message msg, Socket server){
        boolean done = false;
        while (!done) {
            msg.receive(server);
            if (msg.getOperation() != null) {
                if (msg.getOperation().equals("OP_OK")) {
                    done =true;
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
