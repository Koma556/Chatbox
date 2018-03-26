package Client;

import Client.FileTransfer.FileReceiveInstance;
import Client.UI.TestUI;

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
    private ExecutorService openChats = Executors.newCachedThreadPool(), openTransfers = Executors.newCachedThreadPool();

    public FriendchatsListener(){

    }

    public static void stopServer(){
        done = true;
        try {
            connectionToFriend.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            connectionToFriend = new ServerSocket(TestUI.sessionClientPort);
        } catch (BindException e) {
            System.out.println("Port " + TestUI.sessionClientPort + " busy, couldn't bind it. Please try a different one.");
            System.exit(1);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        System.out.println("Listener online");
        // TODO: Check every single done or exit variable and set it right before the loop which it controls
        done = false;
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
            // this process discerns between chat requests and incoming files
            if (newChat != null) {
                FirstMessageListener listener = new FirstMessageListener();
                listener.listenToFirstMessage(newChat);
                if(listener.getMode().equals("chat")) {
                    Runnable chatInstance = new ChatInstance(newChat, listener.getName());
                    openChats.execute(chatInstance);
                }else if(listener.getMode().equals("fileTransfer")){
                    Runnable fileReceiveInstance = new FileReceiveInstance(newChat, listener.getName());
                    openTransfers.execute(fileReceiveInstance);
                }
            }
        }
        System.out.println("FriendchatsListener shutting down.");
        openChats.shutdown();
        openTransfers.shutdown();
        while (!openChats.isTerminated()) {
            // wait
        }
    }
}
