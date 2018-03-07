package Client.UI;

import Client.FriendchatsListener;
import Client.Core;
import Client.UI.chatPane.ChatTabController;
import Client.User;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListCell;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class Controller {

    // create new user instance I will then pass onto the logincontroller. If needed this could be created at an even higher level
    // all user-related data is saved here
    // currently using the same class the server uses. This could be modified easily
    private HashMap<String, Tab> openChats = new HashMap<>();
    private HashMap<String, ChatTabController> openChatControllers = new HashMap<>();
    private ArrayList<String> allActiveChats = new ArrayList<>();
    public static ObservableList<ColoredText> usrs = null;

    @FXML
    private MenuItem loginMenuItem, registerMenuItem, logoutMenuItem, addFriendMenuItem, removeFriendMenuItem, chatWithMenuItem, sendFileToMenuItem, createGroupChatMenuItem, joinGroupChatMenuItem, leaveGroupChatMenuItem, deleteGroupChatMenuItem;
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

    public void enableControls(){
        logoutMenuItem.setDisable(false);
        addFriendMenuItem.setDisable(false);
        removeFriendMenuItem.setDisable(false);
        chatWithMenuItem.setDisable(false);
        sendFileToMenuItem.setDisable(false);
        createGroupChatMenuItem.setDisable(false);
        joinGroupChatMenuItem.setDisable(false);
        leaveGroupChatMenuItem.setDisable(false);
        deleteGroupChatMenuItem.setDisable(false);
        loginMenuItem.setDisable(true);
        registerMenuItem.setDisable(true);
    }

    public void disableControls(){
        logoutMenuItem.setDisable(true);
        addFriendMenuItem.setDisable(true);
        removeFriendMenuItem.setDisable(true);
        chatWithMenuItem.setDisable(true);
        sendFileToMenuItem.setDisable(true);
        createGroupChatMenuItem.setDisable(true);
        joinGroupChatMenuItem.setDisable(true);
        leaveGroupChatMenuItem.setDisable(true);
        deleteGroupChatMenuItem.setDisable(true);
        loginMenuItem.setDisable(false);
        registerMenuItem.setDisable(false);
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
            mainTabPane.getTabs().add(newTabOfPane);
        } catch(Exception e) {
            e.printStackTrace();
        }
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

    public void registerMenuItem() {
        // opens a login window for the user
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
            FriendchatsListener.stopServer();
            allActiveChats = new ArrayList<>();
            TestUI.myUser.unlockRegistry();
            TestUI.myUser.stopHeartMonitor();
            TestUI.myUser = new User();

            // Debug; user is informed he has logged out via the interface itself
            System.out.println("Logged out.");
        }
        else
            System.out.println("Nothing to log out.");
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
            for (String friend: TestUI.myUser.getTmpFriendList()){
                ColoredText usr = new ColoredText(friend, Color.RED);
                usrs.add(usr);
            }
            friendListViewItem.setItems(usrs);
        }
    }

    // create new observable list to replace with old one
    public void changeColorListViewItem(ColoredText item){
        ObservableList<ColoredText> tmpusrs = FXCollections.observableArrayList();
        for (ColoredText entry: usrs) {
            if(entry.getText().equals(item.getText())) {
                tmpusrs.add(item);
            }else{
                tmpusrs.add(entry);
            }
        }
        usrs = tmpusrs;
        friendListViewItem.setItems(tmpusrs);
    }



    private void clearFriendListView(){
        friendListViewItem.setItems(null);
    }

}


