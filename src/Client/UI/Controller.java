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
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class Controller {

    // create new user instance I will then pass onto the logincontroller. If needed this could be created at an even higher level
    // all user-related data is saved here
    // currently using the same class the server uses. This could be modified easily
    private HashMap<String, Tab> openChats = new HashMap<>();
    public static HashMap<String, ChatTabController> openChatControllers = new HashMap<>();
    public static ConcurrentHashMap<String, Boolean> openGroupChats = new ConcurrentHashMap<>();
    private ArrayList<String> allActiveChats = new ArrayList<>();
    public static ObservableList<ColoredText> usrs = null;

    @FXML
    private MenuItem loginMenuItem, registerMenuItem, logoutMenuItem, addFriendMenuItem, removeFriendMenuItem,
            chatWithMenuItem, chatWithTranslatedMenuItem, sendFileToMenuItem, createGroupChatMenuItem, joinGroupChatMenuItem,
            leaveGroupChatMenuItem, deleteGroupChatMenuItem, multicastGroupListMenuItem, searchForUserMenuItem, aboutMenuItem;
    @FXML
    private javafx.scene.control.ListView<ColoredText> friendListViewItem;
    @FXML
    private TabPane mainTabPane;

    public void chatWithMenuItem(){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("PopupWindows/chatWithMenuItem.fxml"));
            Parent root1 = (Parent) fxmlLoader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root1));
            stage.show();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void searchForUserMenuItem(){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("PopupWindows/userLookupWindow.fxml"));
            Parent root1 = (Parent) fxmlLoader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root1));
            stage.show();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void chatWithTranslatedMenuItem(){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("PopupWindows/chatWithTranslatedMenuItem.fxml"));
            Parent root1 = (Parent) fxmlLoader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root1));
            stage.show();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

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

    public void addChatPane(String username, DatagramSocket sock, int portIn){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("chatPane/additionalChatTabs.fxml"));
            Tab newTabOfPane = (Tab) loader.load();
            newTabOfPane.setText(username);
            ChatTabController thisChatTab = loader.<ChatTabController>getController();
            thisChatTab.setUdpChatSocket(sock, portIn);
            // clear old chats with same user
            if(openChats.containsKey(username)){
                ArrayList<String> tmpArray = new ArrayList();
                tmpArray.add(username);
                clearChatPane(tmpArray);
            }
            // I will use these hashmaps to find the chat again and modify/delete it
            System.out.println("Created new tab with username: "+ username);
            openChats.put(username, newTabOfPane);
            openChatControllers.put(username, thisChatTab);
            allActiveChats.add(username);
            newTabOfPane.setOnCloseRequest(e -> thisChatTab.onClose(username));
            mainTabPane.getTabs().add(newTabOfPane);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void addChatPane(String username, Socket sock){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("chatPane/additionalChatTabs.fxml"));
            Tab newTabOfPane = (Tab) loader.load();
            newTabOfPane.setText(username);
            ChatTabController thisChatTab = loader.<ChatTabController>getController();
            thisChatTab.setChatSocket(sock);
            // clear old chats with same user
            if(openChats.containsKey(username)){
                ArrayList<String> tmpArray = new ArrayList();
                tmpArray.add(username);
                clearChatPane(tmpArray);
            }
            // I will use these hashmaps to find the chat again and modify/delete it
            openChats.put(username, newTabOfPane);
            openChatControllers.put(username, thisChatTab);
            allActiveChats.add(username);
            newTabOfPane.setOnCloseRequest(e -> thisChatTab.onClose(username));
            mainTabPane.getTabs().add(newTabOfPane);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void writeToUdpChatTab(String chatID, String content){
        ChatTabController theChat = openChatControllers.get(chatID);
        theChat.addLine(content);
    }

    public void writeToChatTab(String username, String content){
        ChatTabController theChat = openChatControllers.get(username);
        theChat.addLine(username, content);
    }

    // remove the chats with the users found in the arraylist chatsToRemove
    // the delete method of the ChatPane class requires a Tab object
    public void clearChatPane(ArrayList<String> chatsToRemove){
        for (String name: chatsToRemove) {
            mainTabPane.getTabs().remove(openChats.get(name));
        }
    }

    public void lockChatTabWrites(String chatToLock){
        ChatTabController theChat;
        if(openChatControllers.containsKey(chatToLock)) {
            theChat = openChatControllers.get(chatToLock);
            theChat.lockWrite();
        }
    }

    public void alertItem(String title, String header, String content){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        alert.showAndWait();
    }

    public void loginMenuItem() {
        // opens a login window for the user
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("PopupWindows/loginWindow.fxml"));
            Parent root1 = (Parent) fxmlLoader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root1));
            // acquiring a reference to the newly instantiated controller so that I can call its methods
            // no longer required
            // LoginController userAcquisitionController = fxmlLoader.<LoginController>getController();
            stage.show();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void createGroupChatMenuItem(){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("PopupWindows/createGroupWindow.fxml"));
            Parent root1 = (Parent) fxmlLoader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root1));
            stage.show();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void joinGroupChatMenuItem(){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("PopupWindows/joinGroupWindow.fxml"));
            Parent root1 = (Parent) fxmlLoader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root1));
            stage.show();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void leaveGroupChatMenuItem(){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("PopupWindows/leaveGroupWindow.fxml"));
            Parent root1 = (Parent) fxmlLoader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root1));
            stage.show();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteGroupChatMenuItem(){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("PopupWindows/deleteGroupWindow.fxml"));
            Parent root1 = (Parent) fxmlLoader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root1));
            stage.show();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void registerMenuItem() {
        // opens a register window for the user
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("PopupWindows/registerWindow.fxml"));
            Parent root1 = (Parent) fxmlLoader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root1));
            stage.show();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void multicastGroupListMenuItem(){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("PopupWindows/multicastGroupListWindow.fxml"));
            Parent root1 = (Parent) fxmlLoader.load();

            MulticastGroupListController controller = fxmlLoader.<MulticastGroupListController>getController();
            controller.populateView();

            Stage stage = new Stage();
            stage.setScene(new Scene(root1));
            stage.show();

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void sendFileToMenuItem(){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("NIOui/sendFileToWindow.fxml"));
            Parent root1 = (Parent) fxmlLoader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root1));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void closeUdpChatThread(String chatID){
        ArrayList<String> tmp = new ArrayList<>();
        tmp.add(chatID);
        clearChatPane(tmp);
        openGroupChats.replace(chatID, false);
        System.out.println("*******************1");
    }

    private void closeAllUdpChatThread(){
        String[] allUdpChats = openGroupChats.keySet().toArray(new String[openGroupChats.size()]);
        ArrayList<String> chatPaneRemovalIndex = new ArrayList<>();
        for (String chat: allUdpChats
             ) {
            openGroupChats.replace(chat, false);
            System.out.println("*******************2");
            chatPaneRemovalIndex.add(chat);
        }
        clearChatPane(chatPaneRemovalIndex);
    }

    public void logoutMenuItem(){
        if(TestUI.myUser != null && TestUI.myUser.getMySocket() != null && !TestUI.myUser.getMySocket().isClosed()) {
            Core.Logout(TestUI.myUser.getName(), TestUI.myUser.getMySocket());
            try {
                TestUI.myUser.getMySocket().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            clearFriendListView();
            disableControls();
            if(allActiveChats != null)
                clearChatPane(allActiveChats);
            if(openGroupChats != null) {
                closeAllUdpChatThread();
            }
            TestUI.myUser.stopNIO();
            FriendchatsListener.stopServer();

            allActiveChats = new ArrayList<>();
            Controller.openGroupChats = new ConcurrentHashMap<>();
            TestUI.myUser.unlockRegistry();
            TestUI.myUser.stopHeartMonitor();
            TestUI.myUser = new User();

            // Debug; user is informed he has logged out via the interface itself
            //System.out.println("Logged out.");
        }
    }

    public void addFriendMenuItem(){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("PopupWindows/addFriendWindow.fxml"));
            Parent root1 = (Parent) fxmlLoader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root1));

            // acquiring a reference to the newly instantiated controller so that I can call its methods
            // AddFriendController userAcquisitionController = fxmlLoader.<AddFriendController>getController();
            // setting my User object to the new controller which will then save username and socket on it
            // userAcquisitionController.setUser(TestUI.myUser);

            stage.show();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void removeFriendMenuItem(){
        try{
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("PopupWindows/removeFriendWindow.fxml"));
            Parent root1 = (Parent) fxmlLoader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root1));
            stage.show();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void closeMenuItem(){
        logoutMenuItem();
        Platform.exit();
    }

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
        Core.askRetrieveFriendList();
        if(TestUI.myUser.getTmpFriendList() != null) {
            usrs = FXCollections.observableArrayList();
            for (String friend : TestUI.myUser.getTmpFriendList()) {
                ColoredText usr = new ColoredText(friend, Color.RED);
                usrs.add(usr);
            }
            friendListViewItem.setItems(usrs);
        }
    }

    // remove old listing of friend from the friendListView
    // replacing it with a new ColoredText containing its correct online status
    public void changeColorListViewItem(ColoredText item){
        // this raises a ConcurrentModificationException BUT works all the same.
        // a possible fix would be restructiring it like in the MulticastGroupListController.populateView() method
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

    public void aboutMenuItem(){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Chatbox Info");
        alert.setHeaderText("A.A. 2017/2018 RCL final.");
        alert.setContentText("Code credited to Crea Giuseppe (the bad bits), stackoverflow (the okay bits) and class slides (the better bits).");

        alert.showAndWait();
    }
}


