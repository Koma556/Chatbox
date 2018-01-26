package Server;

import com.sun.istack.internal.NotNull;
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Core {

    public static boolean done = false;

    public static void main(String[] args) {

        ConcurrentHashMap<String, User> myDatabase = new ConcurrentHashMap<String, User>();
        // default port picked at random
        int port = 61543;
        ServerSocket connector = null;
        ExecutorService clientHandlers = Executors.newCachedThreadPool();
        // load the port on which to run the server from the ./server.properties file
        try{
            port = getServerPort();
        }
        catch (IOException ioe){
            ioe.printStackTrace();
        }

        // run a loop to accept connections
        // start a thread for each one and restart the loop
        // listening for incoming connections is a blocking operation
        // requires a ServerSocket
        try {
            connector = new ServerSocket(port);
        } catch (BindException e) {
            System.out.println("Port " + port + " busy, couldn't bind it. Please choose a different one in the server.properties file.");
        } catch(IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        // main loop, in here we will accept the clients and hand them off to a handler thread
        while(!done){
            System.out.println("Accepting a new connection on port "+port+".");
            Socket sock = null;
            try {
                sock = connector.accept();
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
            if(sock != null) {
                Runnable clientInstance = new ClientInstance(myDatabase, sock, port);
                clientHandlers.execute(clientInstance);
            }
        }
        //TODO: thread dedicated to saving any changes to the User database
        //TODO: intercept sigterm
        clientHandlers.shutdown();
        while (!clientHandlers.isTerminated()) {
        }
        //TODO: save user database
        System.out.println("Server shutdown complete.");
    }

    public static int getServerPort() throws IOException{

        int portInt;

        //to load application's properties, we use this class
        Properties mainProperties = new Properties();

        FileInputStream file;

        //the base folder is ./, the root of the server.properties file
        String path = "./server.properties";

        //load the file handle for main.properties
        file = new FileInputStream(path);

        //load all the properties from this file
        mainProperties.load(file);

        //we have loaded the properties, so close the file handle
        file.close();

        //retrieve the property we are intrested, the app.port
        portInt = Integer.parseInt(mainProperties.getProperty("server.port"));

        return portInt;
    }
}
