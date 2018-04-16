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
    public static String registryInfo;

    public static void main(String[] args) {
        ConcurrentHashMap<String, User> myDatabase;
        String filePath = "";

        // loading or creating properties file
        String LocalPath = "." + File.separator + "server.properties";
        try{
            GetProperties.getPropertiesFile();
        } catch (IOException e) {
            System.out.println("No server.properties file was found, creating one and assigning database path to local directory.");
            try {
                PrintWriter writer = new PrintWriter(LocalPath, "UTF-8");
                writer.println("# The port on which to run the serversocket clients will connect to.\n" +
                        "server.port=62543\n" +
                        "# This has to be set to your machine's IP unless you want the server and clients to run on the same machine\n" +
                        "server.address=localhost\n" +
                        "# Location of the database file on your file system. If missing it will be created.\n" +
                        "server.databasePath=./userDB\n" +
                        "# Time between database writes in milliseconds.\n" +
                        "server.saveFreq=2000\n" +
                        "# I suggest commenting hostPath and hostRemote unless otherwise needed\n" +
                        "# If hostRemote is set to false or commented the server will create its own RMI registry on server.address\n" +
                        "#registry.hostRemote=false\n" +
                        "# This is the address at which the server will look for an already running RMI registry if hostRemote is set to true\n" +
                        "# This is also the address which will be communicated to the clients when they log, so it has to be reachable from their side.\n" +
                        "# If hostPath is commented but hostRemote is true the server will create the registry on server.address\n" +
                        "#registry.hostPath=localhost\n" +
                        "registry.hostPort=1099");
                writer.close();
            } catch (IOException e1) {
                System.out.println("Couldn't create new server.properties file, exiting with status 1.");
                System.exit(1);
            }
        }

        // checking whether or not server.address is instanced
        String serverPath = null;
        try{
            serverPath = (GetProperties.getPropertiesFile().getProperty("server.address"));
        } catch (IOException e) {
            System.out.println("Server Address not configured: Assuming this to be a test environment, setting address as localhost. (This influences RMI registry location");
            serverPath = "localhost";
            try {
                PrintWriter writer;
                writer = new PrintWriter(LocalPath, "UTF-8");
                writer.println("server.address=localhost");
                writer.close();
            } catch (IOException e1) {
                System.out.println("Couldn't create new user database, exiting with status 1.");
                System.exit(1);
            }
        }
        // loading or instancing User database
        try {
            filePath = (GetProperties.getPropertiesFile().getProperty("server.databasePath"));
            System.out.println("Database path is: "+ filePath);
        } catch (IOException e) {
            System.out.println("Database path was not found.\n" +
                    "New Database path set as current executable folder.");
            filePath = "./userDB";
            try {
                PrintWriter writer;
                writer = new PrintWriter(LocalPath, "UTF-8");
                writer.println("server.databasePath=./userDB");
                writer.close();
            } catch (IOException e1) {
                System.out.println("Couldn't create new user database, exiting with status 1.");
                System.exit(1);
            }
        }
        File database = new File(filePath);
        if(!database.exists()) {
            // create database from scratch, file creation is handled inside the save deamon
            // create database from scratch, file creation is handled inside the save deamon
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
        // check for the related properties in the config file first
        String registryPath;
        int registryPort;
        boolean isRemote;
        try {
            registryPath = GetProperties.getPropertiesFile().getProperty("registry.hostPath");
            String tmpPort = GetProperties.getPropertiesFile().getProperty("registry.hostPort");
            String tmpMode = GetProperties.getPropertiesFile().getProperty("registry.hostRemote");
            if(tmpPort != null) {
                registryPort = Integer.parseInt(tmpPort);
            } else {
                registryPort = 1099;
            }
            if(registryPath == null){
                registryPath = serverPath;
            }
            if(tmpMode != null){
                isRemote = Boolean.parseBoolean(tmpMode);
            } else {
                isRemote = false;
            }
            if(!isRemote && !registryPath.equals(serverPath)){
                System.out.println("WARNING; Settings for the RMI registry indicate that it should be created by this JVM, but registry.hostPath is different from server.address.\n" +
                        "The RMI registry will start on the server.address instead. To change this, please modify registry.hostRemote to true or comment out registry.hostPath.");
                registryPath = serverPath;
            }
        } catch (IOException e) {
            registryPath = serverPath;
            registryPort = 1099;
            isRemote = false;
        }
        try {
            System.setProperty("java.rmi.server.hostname", registryPath);
            LoginCallback exportObject = new LoginCallback(myDatabase);
            if(isRemote) {
                registry = LocateRegistry.getRegistry(registryPath, registryPort);
            } else {
                registry = LocateRegistry.createRegistry(registryPort);
            }
            try{
                loginCaller = (CallbackInterface) UnicastRemoteObject.exportObject(exportObject, 0);
            } catch (Exception e) {
                System.out.println("Could not export the RMI Object. Exiting.");
                System.exit(1);
            }

            if(registry != null) {
                registry.rebind(CallbackInterface.OBJECT_NAME, loginCaller);
                System.out.println("RMI Registry Online at address " + registryPath + " on port " + registryPort);
            }
        } catch (RemoteException e) {
            String tmpErr;
            if(isRemote){
                tmpErr = "bind";
            } else {
                tmpErr = "start";
            }
            System.out.println("Couldn't " + tmpErr + " the RMI Registry on port " + registryPort + " and address " + registryPath +", exiting with status 1.");
            e.printStackTrace();
            System.exit(1);
        }
        registryInfo = registryPath + ":" + registryPort;

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
        // unexporting the registry and killing all RMI threads via a System.exit call.
        try {
            registry.unbind(CallbackInterface.OBJECT_NAME);
            UnicastRemoteObject.unexportObject(registry, true);
            registry = null;
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        }
        System.out.println("Server shutdown complete.");
        System.exit(0);
    }
}
