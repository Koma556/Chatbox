package Client;

import Client.UI.TestUI;
import javafx.application.Platform;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FriendchatsListener extends Thread {
    private Socket newChat;
    private static ServerSocket connectionToFriend;
    private static boolean done = false;
    private ExecutorService openChats = Executors.newCachedThreadPool();
    private int serverSocketPort;

    public FriendchatsListener(){
        this.serverSocketPort = TestUI.myUser.getMyPort();
    }

    public static void stopServer(){
        done = true;
        //System.out.println("public static void stopServer was called and done is " + done);
        try {
            connectionToFriend.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            connectionToFriend = new ServerSocket(serverSocketPort);
        } catch (BindException e) {
            System.out.println("Port " + serverSocketPort + " busy, couldn't bind it. Please try a different one.");
            System.exit(1);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        System.out.println("Listener online");
        while (!done) {
            // listens for incoming connections
            // also cleans socket after a connection has been successfully established
            newChat = null;
            try {
                newChat = connectionToFriend.accept();
            } catch (SocketException e) {
                System.out.println("Server was closed because user is logging out.");
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Oh no something happened while receiving a new connection from the server!");
            }
            // each new connection is then associated with a new thread
            if (newChat != null) {
                Runnable chatInstance = new ChatInstance (newChat);
                openChats.execute(chatInstance);
            }
        }
        System.out.println("FriendchatsListener shutting down.");
        openChats.shutdown();
        while (!openChats.isTerminated()) {
            // wait
        }
        /*
        while(true){
            i++;
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Still alive");
        }
        */
    }
}
