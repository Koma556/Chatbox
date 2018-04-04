package Server;

import Communication.GetProperties;
import Server.RMI.CallbackInterface;
import Server.RMI.LoginCallback;
import Server.UDP.ThreadWrapper;

import java.io.*;
import java.net.*;
import java.rmi.AccessException;
import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashSet;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Core {

    public static boolean done = false;
    public static CallbackInterface loginCaller;
    public static ConcurrentHashMap<String, ThreadWrapper> chatroomsUDPWrapper = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String, Boolean> chatroomsUDPcontrolArray = new ConcurrentHashMap<>();
    public static int UDPport = 2000;
    public static HashSet<Integer> busyUDPports = new HashSet();
    public static Registry registry;

    public static void main(String[] args) {
        ConcurrentHashMap<String, User> myDatabase;
        String filePath = "";

        // loading or instancing User database
        try {
            filePath = (GetProperties.getPropertiesFile().getProperty("server.databasePath"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        File database = new File(filePath);
        if(!database.exists()) {
            // create database from scratch, file cration is handled inside the save deamon
            myDatabase = new ConcurrentHashMap<String, User>();
        }
        else{
            // deserialize old database
            myDatabase = Deserializer.deserialize(filePath);
            String[] keys = myDatabase.keySet().toArray(new String[myDatabase.size()]);
            for (String key: keys
                 ) {
                myDatabase.get(key).logout();
            }
        }

        // start RMI Registry
        try {
            String tmp[] = InetAddress.getLocalHost().toString().split("/");
            System.setProperty("java.rmi.server.hostname", tmp[1]);
            registry = LocateRegistry.createRegistry(1099);
            loginCaller = (CallbackInterface) UnicastRemoteObject.exportObject(new LoginCallback(myDatabase), 0);
            System.out.println(System.getProperty("java.rmi.server.hostname"));
            //registry = LocateRegistry.getRegistry();
            if(registry != null) {
                registry.rebind(CallbackInterface.OBJECT_NAME, loginCaller);
                System.out.println("RMI Registry Online");
            }
        } catch (RemoteException e) {
            System.out.println("Couldn't start the RMI Registry on port 1099, exiting with status 1.");
            System.exit(1);
        } catch (UnknownHostException e) {
            System.out.println("Couldn't get localost to start the RMI Registry at. Exiting.");
            System.exit(1);
        }

        // default port, can be changed in the server.properties file
        int port = 61543;
        ServerSocket connector = null;
        ExecutorService clientHandlers = Executors.newCachedThreadPool();
        // load the port on which to run the server from the ./server.properties filePath
        try{
            port = Integer.parseInt(GetProperties.getPropertiesFile().getProperty("server.port"));
        }
        catch (IOException ioe){
            ioe.printStackTrace();
        }

        // deamon with the task of saving the user database every 2 seconds
        SavestateDeamon databaseDeamon = new SavestateDeamon(myDatabase);
        Thread databaseDeamonThread = new Thread(databaseDeamon);
        databaseDeamonThread.start();

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
        Thread serverAcceptInstance = new Thread(new ServerAcceptThread(port, connector, myDatabase, clientHandlers));
        serverAcceptInstance.start();
        System.out.println("Type 'EXIT' to close the server.");
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        while(!done){
            try {
                String input = in.readLine();
                if(input.equalsIgnoreCase("exit")) {
                    done = true;
                    // closing the serversocket calls an exception on the
                    // accept method in the serverAcceptInstance thread.
                    connector.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        clientHandlers.shutdown();
        // this stops the database deamon
        databaseDeamon.stop();
        String[] listOfRoomsUDP = chatroomsUDPWrapper.keySet().toArray(new String[chatroomsUDPWrapper.size()]);
        for (String room: listOfRoomsUDP
             ) {
            chatroomsUDPWrapper.get(room).shutdownThread("", true);
        }
        System.out.println("Server shutdown complete.");
        // killing all RMI threads via a System.exit call.
        System.exit(0);
    }
}
