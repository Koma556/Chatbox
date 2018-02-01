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

    public static void main(String[] args) throws IOException {

        int port = 61543;
        boolean done = false;
        String userName;
        InetAddress serverAddr = null;
        Socket sock = null;

        // parses the config values
        try {
            port = Integer.parseInt(GetProperties.getPropertiesFile().getProperty("server.port"));
        } catch (IOException e) {
            System.out.println("No config file found; Using default port.");
        }
        // see if a server.address field has been specified. If so, try and connect to it. Otherwise use localhost.
        /*
        try{
            serverAddr = InetAddress.getByName(GetProperties.getPropertiesFile().getProperty("server.address"));
        } catch (IOException e) {
            System.out.println("No or invalid address found in config file; Using localhost");
            try {
                serverAddr = InetAddress.getLocalHost();
            } catch (UnknownHostException e1) {
                System.out.println("No server found on localhost, exiting client.");
                System.exit(1);
            }
        }
        */

        // debug print to see which server I'm connected to
        System.out.println("Connected to: " + serverAddr.toString());

        Scanner keyboard = new Scanner(System.in);
        System.out.println("Enter username:");
        userName = keyboard.nextLine();

        try{
            sock = new Socket(serverAddr, port);
        } catch (IOException e) {
            System.out.println("Couldn't open a socket with the server.");
            System.exit(1);
        }

        while(!done) {
            System.out.println("Type 1 to register, 2 to login, 3 to quit.");
            int selection = keyboard.nextInt();
            if (selection == 1) {
                Message msg = new Message("OP_REGISTER", userName);
                msg.send(sock);
                System.out.println("Work's done.");
            }
            if (selection == 2) {
                Message msg = new Message("OP_LOGIN", userName);
                msg.send(sock);
                System.out.println("Work's done.");
            }
            if (selection == 3) {
                done = true;
                Message msg = new Message("OP_CLOSE", "");
                msg.send(sock);
                try {
                    sock.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
/*
    private static Properties getPropertiesFile() throws IOException {

        //to load application's properties, we use this class
        Properties mainProperties = new Properties();

        FileInputStream file;
        String path = "./server.properties";

        //load the file handle for main.properties
        file = new FileInputStream(path);

        //load all the properties from this file
        mainProperties.load(file);

        //we have loaded the properties, so close the file handle
        file.close();

        return mainProperties;
    }
    */
}
