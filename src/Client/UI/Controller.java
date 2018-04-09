package Client.UI;

import Client.FriendchatsListener;
import Client.Core;
import Client.UI.PopupWindows.MulticastGroupListController;
import Client.UI.chatPane.ChatTabController;
import Client.User;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class Controller {

    /* this is for every intent and purpose the heart of my graphical interface
     * everything the user can do passes through here
     * this is also the class whose only instance will be running on my main UI thread natively
     */
    // this hashmap is to memorize all open chat tabs in the Stage
    private HashMap<String, Tab> openChats = new HashMap<>();
    // this hashmap memorizes all active controllers to them associated
    public static HashMap<String, ChatTabController> openChatControllers = new HashMap<>();
    // this hashmap memorizes the activity status of open UDP multicast groups
    public static ConcurrentHashMap<String, Boolean> openGroupChats = new ConcurrentHashMap<>();
    // this arraylist memorizes the id by which I recorded each active chat
    private ArrayList<String> allActiveChats = new ArrayList<>();
    // this ObservableList is instead the friendlist for my user,
    // on it each user will light up Green when online, Red when offline
    public static ObservableList<ColoredText> usrs = null;

    // next we have all the javafx items tied to the UI element this controller represents
    @FXML
    private MenuItem loginMenuItem, registerMenuItem, logoutMenuItem, addFriendMenuItem, removeFriendMenuItem,
            chatWithMenuItem, chatWithTranslatedMenuItem, sendFileToMenuItem, createGroupChatMenuItem, joinGroupChatMenuItem,
            leaveGroupChatMenuItem, deleteGroupChatMenuItem, multicastGroupListMenuItem, searchForUserMenuItem, aboutMenuItem;
    @FXML
    private javafx.scene.control.ListView<ColoredText> friendListViewItem;
    @FXML
    private TabPane mainTabPane;

    public void enableControls(){
        logoutMenuItem.setDisable(false);
        searchForUserMenuItem.setDisable(false);
        addFriendMenuItem.setDisable(false);
        removeFriendMenuItem.setDisable(false);
        chatWithMenuItem.setDisable(false);
        chatWithTranslatedMenuItem.setDisable(false);
        sendFileToMenuItem.setDisable(false);
        createGroupChatMenuItem.setDisable(false);
        joinGroupChatMenuItem.setDisable(false);
        leaveGroupChatMenuItem.setDisable(false);
        deleteGroupChatMenuItem.setDisable(false);
        multicastGroupListMenuItem.setDisable(false);
        loginMenuItem.setDisable(true);
        registerMenuItem.setDisable(true);
    }

    public void disableControls(){
        logoutMenuItem.setDisable(true);
        searchForUserMenuItem.setDisable(true);
        addFriendMenuItem.setDisable(true);
        removeFriendMenuItem.setDisable(true);
        chatWithMenuItem.setDisable(true);
        chatWithTranslatedMenuItem.setDisable(true);
        sendFileToMenuItem.setDisable(true);
        createGroupChatMenuItem.setDisable(true);
        joinGroupChatMenuItem.setDisable(true);
        leaveGroupChatMenuItem.setDisable(true);
        deleteGroupChatMenuItem.setDisable(true);
        multicastGroupListMenuItem.setDisable(true);
        loginMenuItem.setDisable(false);
        registerMenuItem.setDisable(false);
    }

    /* The following set of functions, all marked as "popup window", have the task of loading an fxml component from
     * its file, creating a stage for it and displaying the stage for the user. The actual work happens inside the
     * newly loaded component. Names are indicative of their role and the UI element they are bound to.
     */
    // popup window
    public void chatWithMenuItem(){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("PopupWindows/chatWithMenuItem.fxml"));
            Parent root1 = (Parent) fxmlLoader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root1));
            stage.setResizable(false);
            stage.show();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    // popup window
    public void searchForUserMenuItem(){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("PopupWindows/userLookupWindow.fxml"));
            Parent root1 = (Parent) fxmlLoader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root1));
            stage.setResizable(false);
            stage.show();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    // popup window
    public void chatWithTranslatedMenuItem(){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("PopupWindows/chatWithTranslatedMenuItem.fxml"));
            Parent root1 = (Parent) fxmlLoader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root1));
            stage.setResizable(false);
            stage.show();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    // popup window
    public void alertItem(String title, String header, String content){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);

        alert.showAndWait();
    }

    // popup window
    public void registerMenuItem() {
        // opens a register window for the user
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("PopupWindows/registerWindow.fxml"));
            Parent root1 = (Parent) fxmlLoader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root1));
            stage.setResizable(false);
            stage.show();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    // popup window
    public void loginMenuItem() {
        // opens a login window for the user
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("PopupWindows/loginWindow.fxml"));
            Parent root1 = (Parent) fxmlLoader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root1));
            stage.setResizable(false);
            stage.show();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    // popup window
    public void createGroupChatMenuItem(){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("PopupWindows/createGroupWindow.fxml"));
            Parent root1 = (Parent) fxmlLoader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root1));
            stage.setResizable(false);
            stage.show();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    // popup window
    public void leaveGroupChatMenuItem(){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("PopupWindows/leaveGroupWindow.fxml"));
            Parent root1 = (Parent) fxmlLoader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root1));
            stage.setResizable(false);
            stage.show();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    // popup window
    public void deleteGroupChatMenuItem(){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("PopupWindows/deleteGroupWindow.fxml"));
            Parent root1 = (Parent) fxmlLoader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root1));
            stage.setResizable(false);
            stage.show();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    // popup window
    public void multicastGroupListMenuItem(){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("PopupWindows/multicastGroupListWindow.fxml"));
            Parent root1 = (Parent) fxmlLoader.load();

            // saving a reference to the controller tied to this new stage we just loaded
            MulticastGroupListController controller = fxmlLoader.<MulticastGroupListController>getController();
            // to then call a method within the controller itself
            controller.populateView();

            Stage stage = new Stage();
            stage.setScene(new Scene(root1));
            stage.show();

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    // popup window
    public void sendFileToMenuItem(){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("NIOui/sendFileToWindow.fxml"));
            Parent root1 = (Parent) fxmlLoader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root1));
            stage.setResizable(false);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // popup window
    public void addFriendMenuItem(){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("PopupWindows/addFriendWindow.fxml"));
            Parent root1 = (Parent) fxmlLoader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root1));
            stage.setResizable(false);
            stage.show();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    // popup window
    public void removeFriendMenuItem(){
        try{
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("PopupWindows/removeFriendWindow.fxml"));
            Parent root1 = (Parent) fxmlLoader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root1));
            stage.setResizable(false);
            stage.show();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    // popup window
    public void aboutMenuItem(){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Chatbox Info");
        alert.setHeaderText("A.A. 2017/2018 RCL final.");
        alert.setContentText("Code credited to Crea Giuseppe (the bad bits), stackoverflow (the okay bits) and class slides (the better bits).");
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.showAndWait();
    }

    // popup window
    public void joinGroupChatMenuItem(){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("PopupWindows/joinGroupWindow.fxml"));
            Parent root1 = (Parent) fxmlLoader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root1));
            stage.setResizable(false);
            stage.show();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    /* The following functions all have to do with the tab interface used for new chats
     * Be it TCP or UDP, when a new chat is required (either user-initiated or as a result of
     * a new connection on FriendchatsListened) these functions are required
     */

    // Add a chat tab for an UDP chat
    public void addChatPane(String username, DatagramSocket sock, int portIn){
        try {
            // like with popup windows we call the loader on the fxml file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("chatPane/additionalChatTabs.fxml"));
            // obtain the Tab object from the loader
            Tab newTabOfPane = (Tab) loader.load();
            // give it a proper name
            newTabOfPane.setText(username);
            // obtain the controller
            ChatTabController thisChatTab = loader.<ChatTabController>getController();
            // call a function on it to set the udp socket it will use for sending data
            thisChatTab.setUdpChatSocket(sock, portIn);
            // clear old chats with same group
            if(openChats.containsKey(username)){
                ArrayList<String> tmpArray = new ArrayList();
                tmpArray.add(username);
                clearChatPane(tmpArray);
            }
            // add the tab to the many control variables I declared above
            openChats.put(username, newTabOfPane);
            openChatControllers.put(username, thisChatTab);
            allActiveChats.add(username);
            // set the closing policy of this tab to a function on the controller itself
            newTabOfPane.setOnCloseRequest(e -> thisChatTab.onClose(username));
            // append the tab onto the mainTabPane element of our main UI, finally displaying it
            mainTabPane.getTabs().add(newTabOfPane);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    // Add a chat tab for a TCP chat
    public void addChatPane(String username, Socket sock){
        try {
            // exactly the same as the udp tab function above
            FXMLLoader loader = new FXMLLoader(getClass().getResource("chatPane/additionalChatTabs.fxml"));
            Tab newTabOfPane = (Tab) loader.load();
            newTabOfPane.setText(username);
            ChatTabController thisChatTab = loader.<ChatTabController>getController();
            // only difference, we call a method on the controller to set a tcp socket, instead of udp
            thisChatTab.setChatSocket(sock);

            if(openChats.containsKey(username)){
                ArrayList<String> tmpArray = new ArrayList();
                tmpArray.add(username);
                clearChatPane(tmpArray);
            }

            openChats.put(username, newTabOfPane);
            openChatControllers.put(username, thisChatTab);
            allActiveChats.add(username);

            newTabOfPane.setOnCloseRequest(e -> thisChatTab.onClose(username));
            mainTabPane.getTabs().add(newTabOfPane);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    /* support functions the chat threads will call via Platform.Runlater()
     * on an instance of the Client.UI.chatPane.UpdateTab class
     * they take care of writing a String on the tab identified by a certain String
     */
    public void writeToUdpChatTab(String chatID, String content){
        ChatTabController theChat = openChatControllers.get(chatID);
        theChat.addLine(content);
    }

    public void writeToChatTab(String username, String content){
        ChatTabController theChat = openChatControllers.get(username);
        theChat.addLine(username, content);
    }

    /* remove the chats with the users found in the arraylist chatsToRemove
     * the delete method of the ChatPane class requires a Tab object
     * hence the roundabout solution.
     */
    public void clearChatPane(ArrayList<String> chatsToRemove){
        for (String name: chatsToRemove) {
            mainTabPane.getTabs().remove(openChats.get(name));
            if(openChatControllers.containsKey(name)) {
                openChatControllers.get(name).onClose(name);
            }
        }
    }

    /* this calls the lockWrite method on a chat tab controller
     * like many other chat tab methods, it can't be called by the thread which needs to
     * it is instead called by an instance of Client.UI.chatPane.LockTab called via Platform.Runlater()
     */
    public void lockChatTabWrites(String chatToLock){
        ChatTabController theChat;
        if(openChatControllers.containsKey(chatToLock)) {
            theChat = openChatControllers.get(chatToLock);
            theChat.lockWrite();
        }
    }

    /* the following functions have two similar jobs. One is called when closing a single UDP group
     * the other, when closing all of them because the user is logging out
     */
    public void closeUdpChatThread(String chatID){
        ArrayList<String> tmp = new ArrayList<>();
        tmp.add(chatID);
        clearChatPane(tmp);
        openGroupChats.replace(chatID, false);
    }

    private void closeAllUdpChatThread(){
        String[] allUdpChats = openGroupChats.keySet().toArray(new String[openGroupChats.size()]);
        ArrayList<String> chatPaneRemovalIndex = new ArrayList<>();
        for (String chat: allUdpChats
             ) {
            openGroupChats.replace(chat, false);
            chatPaneRemovalIndex.add(chat);
        }
        clearChatPane(chatPaneRemovalIndex);
    }

    /* this is the logout method, its main task is cleaning up the program environment from any tracks of the current user
     * and warning the server that the current user is logging out
     */
    public void logoutMenuItem(){
        // first of all test if the user object exists and if it is still connected to the server
        // important to note that a disconnected user means a dead program, so that condition should always be verified
        if(CoreUI.myUser != null && CoreUI.myUser.getMySocket() != null && !CoreUI.myUser.getMySocket().isClosed()) {
            // if the user had active chats, we clear them up
            // this has to happen BEFORE we actually disconnect from the server, as the messages pass for the server first, the clients later
            if(allActiveChats != null)
                clearChatPane(allActiveChats);
            // calls the static Client.Core.Logout() method, trying to make the server aware we are closing the connection
            Core.Logout(CoreUI.myUser.getName(), CoreUI.myUser.getMySocket());
            try {
                // closes the socket
                CoreUI.myUser.getMySocket().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            // the following commands are all required to clean up the UI and active threads
            // first we delete the friend list
            clearFriendListView();
            // then disable all the controls
            disableControls();

            // if there were running UDP threads, we shut them down
            if(openGroupChats != null) {
                closeAllUdpChatThread();
            }
            // we also stop the two listeners for file transfer and incoming messages
            CoreUI.myUser.stopNIO();
            FriendchatsListener.stopServer();

            // and finally restore all maps and arrays for the next iteration
            allActiveChats = new ArrayList<>();
            Controller.openGroupChats = new ConcurrentHashMap<>();
            // one last thing to do before cleaning the User object is unregistering the RMI and stopping the heartbeat thread
            CoreUI.myUser.unlockRegistry();
            CoreUI.myUser.stopHeartMonitor();
            // then we let the old User object be garbaje collected
            CoreUI.myUser = new User();
        }
    }

    // close simply calls the above logout functions and also closes the UI
    // this is technically redundant since the closing policy for our application was set as calling logoutMenuItem()
    public void closeMenuItem(){
        logoutMenuItem();
        Platform.exit();
    }

    // fills the list view to the side of the UI with the User's friends
    public void populateListView(){
        // setting my personal CellFactory style for my ColoredText class
        friendListViewItem.setCellFactory(lv -> new ListCell<ColoredText>() {
            @Override
            protected void updateItem(ColoredText item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null) {
                    setText(null);
                    setTextFill(null);
                } else {
                    setText(item.getText());
                    setTextFill(item.getColor());
                }
            }
        });
        // call the static function for friend retrieval
        Core.askRetrieveFriendList();
        if(CoreUI.myUser.getTmpFriendList() != null) {
            // create an observable ArrayList collection and fill it with all users, all marked as offline
            // the caller for this function will also take care to call the below function to update their status
            usrs = FXCollections.observableArrayList();
            for (String friend : CoreUI.myUser.getTmpFriendList()) {
                ColoredText usr = new ColoredText(friend, Color.RED);
                usrs.add(usr);
            }
            friendListViewItem.setItems(usrs);
        }
    }

    // remove old listing of friend from the friendListView
    // replacing it with a new ColoredText containing its correct online status
    public void changeColorListViewItem(ColoredText item){
        for (ColoredText entry: usrs) {
            if(entry.getText().equals(item.getText())) {
                usrs.remove(entry);
                usrs.add(item);
            }
        }
    }

    private void clearFriendListView(){
        friendListViewItem.setItems(null);
    }
}


