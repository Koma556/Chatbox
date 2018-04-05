package Client;

import Client.UI.PopupWindows.BigErrorAlert;
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
            BigErrorAlert bigErrorAlert = new BigErrorAlert("Port Busy!","Couldn't start chat engine.", "Port " + TestUI.sessionClientPort + " busy, exiting client.\nRestarting might fix this.", e);
            Platform.runLater(bigErrorAlert);
            System.exit(1);
        } catch (IOException e) {
            e.printStackTrace();
            BigErrorAlert bigErrorAlert = new BigErrorAlert("Couldn't connect!","Error while binding chat engine port.", e.getMessage(), e);
            Platform.runLater(bigErrorAlert);
            System.exit(1);
        }
        done = false;
        while (!done) {
            // listens for incoming connections
            // also cleans socket after a connection has been successfully established
            newChat = null;
            try {
                newChat = connectionToFriend.accept();
            } catch (SocketException e) {
                // fail quietly
            } catch (IOException e) {
                e.printStackTrace();
            }
            // each new connection is then associated with a new thread
            // this process discerns between chat requests and incoming files
            if (newChat != null) {
                FirstMessageListener listener = new FirstMessageListener();
                listener.listenToFirstMessage(newChat);
                if(listener.getMode().equals("chat")) {
                    Runnable chatInstance = new ChatInstance(newChat, listener.getName());
                    openChats.execute(chatInstance);
                }
            }
        }
        //System.out.println("FriendchatsListener shutting down.");
        openChats.shutdown();
        openTransfers.shutdown();
        while (!openChats.isTerminated()) {
            // wait
        }
    }
}
