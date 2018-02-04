package Client.UI;

import Client.Core;
import Communication.User;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Controller {

    // create new user instance I will then pass onto the logincontroller. If needed this could be created at an even higher level
    // all user-related data is saved here
    // currently using the same class the server uses. This could be modified easily
    private HashMap<String, Tab> openChats = new HashMap<>();
    private ArrayList<String> allActiveChats = new ArrayList<>();

    @FXML
    private MenuItem logoutMenuItem, addFriendMenuItem, removeFriendMenuItem, chatWithMenuItem, sendFileToMenuItem, createGroupChatMenuItem, joinGroupChatMenuItem, leaveGroupChatMenuItem, deleteGroupChatMenuItem;
    @FXML
    private javafx.scene.control.ListView friendListViewItem;
    @FXML
    private TabPane mainTabPane;

    public void chatWithMenuItem(){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("chatWithMenuItem.fxml"));
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
    }

    public void addChatPane(String username){
        try {
            Tab newTabOfPane = (Tab)FXMLLoader.load(this.getClass().getResource("additionalChatTabs.fxml"));
            newTabOfPane.setText(username);
            openChats.put(username,newTabOfPane);
            allActiveChats.add(username);
            mainTabPane.getTabs().add(newTabOfPane);

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    // remove the chats with the users found in the arraylist chatsToRemove
    public void clearChatPane(ArrayList<String> chatsToRemove){
        List<Tab> tabs = chatsToRemove.stream().map(openChats::get).collect(Collectors.toList());
        mainTabPane.getTabs().removeAll(tabs);
    }

    public void loginMenuItem() {
        // opens a login window for the user
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("loginWindow.fxml"));
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
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("registerWindow.fxml"));
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
            if(Core.Logout(TestUI.myUser.getName(), TestUI.myUser.getMySocket()))
                System.out.println("Server acknowledged.");
            try {
                TestUI.myUser.getMySocket().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            TestUI.myUser = new User();
            clearFriendListView();
            disableControls();
            clearChatPane(allActiveChats);

            // TODO: convert this to a popup window
            System.out.println("Logged out.");
        }
        else
            System.out.println("Nothing to log out.");
    }

    public void addFriendMenuItem(){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("addFriendWindow.fxml"));
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
        Core.askRetrieveFriendList();
        if(TestUI.myUser.getTmpFriendList() != null) {
            ObservableList<String> names = FXCollections.observableArrayList();
            names.addAll(TestUI.myUser.getTmpFriendList());
            friendListViewItem.setItems(names);
        }
    }

    private void clearFriendListView(){
        friendListViewItem.setItems(null);
    }

}


