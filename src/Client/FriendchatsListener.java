package Client;

import Client.UI.CoreUI;
import Client.UI.FriendListUpdate;
import Client.UI.ModifyFriendlistStatus;
import Client.UI.PopupWindows.Alerts;
import Client.UI.PopupWindows.BigErrorAlert;
import javafx.application.Platform;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/* Thread dedicated to listening in for new connections.
 * it's created only on a successful Register or Login and is destroyed on a logout
 */
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
            // create a server socket on sessionClientPort
            connectionToFriend = new ServerSocket(CoreUI.sessionClientPort);
        } catch (BindException e) {
            BigErrorAlert bigErrorAlert = new BigErrorAlert("Port Busy!","Couldn't start chat engine.", "Port " + CoreUI.sessionClientPort + " busy, exiting client.\nRestarting might fix this.", e);
            Platform.runLater(bigErrorAlert);
        } catch (IOException e) {
            e.printStackTrace();
            BigErrorAlert bigErrorAlert = new BigErrorAlert("Couldn't connect!","Error while binding chat engine port.", e.getMessage(), e);
            Platform.runLater(bigErrorAlert);
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
            // each new connection is then read for its first message, and the operation written within
            // if the operation requires a new chat to be run, a new thread is started
            // otherwise an update of the friendlist is queued
            if (newChat != null) {
                FirstMessageListener listener = new FirstMessageListener();
                listener.listenToFirstMessage(newChat);
                if(listener.getMode().equals("chat")) {
                    Runnable chatInstance = new ChatInstance(newChat, listener.getName());
                    openChats.execute(chatInstance);
                } else if (listener.getMode().equals("friendship")) {
                    // update friendlist and notify user
                    FriendListUpdate update = new FriendListUpdate();
                    Platform.runLater(update);
                    // to show the new user as online
                    ModifyFriendlistStatus usrAdd = new ModifyFriendlistStatus(listener.getName(), true);
                    Platform.runLater(usrAdd);
                    Alerts alerts = new Alerts("Friendship!",
                            "New friend correlation.",
                            "User " + listener.getName() + " added you to his friends!");
                    Platform.runLater(alerts);
                } else if (listener.getMode().equals("unfriended")) {
                    FriendListUpdate update = new FriendListUpdate();
                    Platform.runLater(update);
                }
            }
        }
        openChats.shutdown();
        openTransfers.shutdown();
        while (!openChats.isTerminated()) {
            // wait
        }
    }
}
