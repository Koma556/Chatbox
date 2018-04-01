package Client.UI;

import Client.ChatWrapper;
import Client.FileTransfer.FileSendInstance;
import Client.FileTransfer.FriendWrapper;
import Client.FriendchatsListener;
import Client.Core;
import Client.UI.FileReceiverWindow.FileReceiverController;
import Client.FileTransfer.FileReceiverWrapper;
import Client.UI.FileSenderWindow.FileSenderController;
import Client.FileTransfer.FileSenderWrapper;
import Client.UI.PopupWindows.Alerts;
import Client.UI.PopupWindows.MulticastGroupListController;
import Client.UI.PopupWindows.SendToController;
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
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
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
    public static ArrayList<String> allActiveChats = new ArrayList<>();
    public static ObservableList<ColoredText> usrs = null;
    public static ConcurrentHashMap<String, ChatWrapper> openChatTabs = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<Integer, FileSenderWrapper> listOfFileSenderProcesses = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<Integer, FileReceiverWrapper> listOfFileReceiverProcesses = new ConcurrentHashMap<>();

    @FXML
    private MenuItem loginMenuItem, registerMenuItem, logoutMenuItem, addFriendMenuItem, removeFriendMenuItem,
            chatWithMenuItem, chatWithTranslatedMenuItem, sendFileToMenuItem, createGroupChatMenuItem, joinGroupChatMenuItem,
            leaveGroupChatMenuItem, deleteGroupChatMenuItem, multicastGroupListMenuItem, searchForUserMenuItem;
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

    public void addChatPane(String username, DatagramSocket sock, int portOut){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("chatPane/additionalChatTabs.fxml"));
            Tab newTabOfPane = (Tab) loader.load();
            newTabOfPane.setText(username);
            ChatTabController thisChatTab = loader.<ChatTabController>getController();
            thisChatTab.setUdpChatSocket(sock, portOut);
            // I will use these hashmaps to find the chat again and modify/delete it
            openChatTabs.get(username).setMode("udp");
            openChatTabs.get(username).setTab(newTabOfPane);
            openChatTabs.get(username).setController(thisChatTab);
            openChatTabs.get(username).setDatagramSocket(sock);
            allActiveChats.add(username);
            newTabOfPane.setOnCloseRequest(e -> openChatTabs.get(username).onClose());
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
            System.out.println("Now from Controller: "+openChatTabs.keySet()+", and username is: "+username);
            if(openChatTabs.containsKey(username)){
                ArrayList<String> tmpArray = new ArrayList();
                tmpArray.add(username);
                clearChatPane(tmpArray);
            }
            // I will use these hashmaps to find the chat again and modify/delete it
            if(openChatTabs.containsKey(username)) {
                openChatTabs.get(username).setMode("tcp");
                openChatTabs.get(username).setTab(newTabOfPane);
                openChatTabs.get(username).setController(thisChatTab);
                openChatTabs.get(username).setSock(sock);
                allActiveChats.add(username);
                newTabOfPane.setOnCloseRequest(e -> openChatTabs.get(username).onClose());
            }else
                System.out.println("Still nothing even though the keyset is "+ openChatTabs.keySet());
            mainTabPane.getTabs().add(newTabOfPane);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void writeToUdpChatTab(String chatID, String content){
        ChatTabController theChat = openChatTabs.get(chatID).getController();
        theChat.addLine(content);
    }

    public void writeToChatTab(String username, String content){
        ChatTabController theChat = openChatTabs.get(username).getController();
        theChat.addLine(username, content);
    }

    // remove the chats with the users found in the arraylist chatsToRemove
    // the delete method of the ChatPane class requires a Tab object
    public void clearChatPane(ArrayList<String> chatsToRemove){
        for (String name: chatsToRemove) {
            mainTabPane.getTabs().remove(openChatTabs.get(name).getTab());
            openChatTabs.remove(name);
            allActiveChats.remove(name);
        }
    }

    public void lockChatTabWrites(String chatToLock){
        ChatTabController theChat;
        if(openChatTabs.containsKey(chatToLock)) {
            theChat = openChatTabs.get(chatToLock).getController();
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
        Stage stage = new Stage();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        File file = fileChooser.showOpenDialog(stage);
        String destination = null;
        if (file != null) {
            // open dialog window to pick a friend
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("PopupWindows/sendToWindow.fxml"));
                Parent root1 = (Parent) fxmlLoader.load();
                SendToController controller = fxmlLoader.<SendToController>getController();
                Stage formStage = new Stage();
                formStage.setScene(new Scene(root1));
                // set it always on top
                formStage.initModality(Modality.APPLICATION_MODAL);
                // wait for it to return
                formStage.showAndWait();
                // get the name of the user I want to send my file to
                destination = controller.getUsername();
            } catch (Exception e) {
                e.printStackTrace();
            }
            // send transfer request to server.
            FriendWrapper target = Core.askSendFileTo(destination);
            // start NIO stream to target
            if (target != null && (listOfFileSenderProcesses != null || !listOfFileSenderProcesses.containsKey(target.getPort()))) {
                // instantiate FileSenderWrapper for the first time
                FileSenderWrapper wrapper = new FileSenderWrapper();
                Thread fileSend = new Thread(new FileSendInstance(target, file));
                fileSend.start();
                wrapper.setWorkerThread(fileSend);
                wrapper.setUsername(destination);
                // Using the current port as unique ID for this operation.
                listOfFileSenderProcesses.put(target.getPort(), wrapper);
            } else{
                Alerts alreadyTransferringAlert = new Alerts(
                        "Error Notice",
                        "Couldn't Start File Transfer.",
                        "You are already transferring a file with that user, please finish that link before trying again.");
                alreadyTransferringAlert.run();
            }
        }
    }

    public void loadFileSenderPane(String text, int controllerID){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("FileSenderWindow/fileSenderWindow.fxml"));
            Parent root1 = (Parent) fxmlLoader.load();

            FileSenderController fileSenderController = fxmlLoader.<FileSenderController>getController();
            fileSenderController.setStatusLabel(text);
            fileSenderController.setId(controllerID);
            listOfFileSenderProcesses.get(controllerID).setController(fileSenderController);

            Stage stage = new Stage();
            stage.setScene(new Scene(root1));
            stage.setOnHidden(e -> fileSenderController.stop());
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateFileSenderStatus(String text, int controllerID) {
        if(listOfFileSenderProcesses.containsKey(controllerID))
            listOfFileSenderProcesses.get(controllerID).getController().setStatusLabel(text);
    }

    public void loadFileReceiverPane(String from, String filename, String fileSize, Socket sock) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("FileReceiverWindow/fileReceiverWindow.fxml"));
            Parent root1 = (Parent) fxmlLoader.load();

            FileReceiverController fileReceiverController = fxmlLoader.<FileReceiverController>getController();
            fileReceiverController.setStatusLabel(from, filename, fileSize);
            fileReceiverController.setSock(sock);
            listOfFileReceiverProcesses.get(sock.getPort()).setController(fileReceiverController);

            Stage stage = new Stage();
            stage.setScene(new Scene(root1));
            stage.setOnHidden(e -> fileReceiverController.stop());
            stage.show();

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    /*
    public void fileReceiverAcceptButtonPress(int id) {
        listOfFileReceiverProcesses.get(id).getController().acceptButtonPressUIModifications();
    }
    */
    public void closeUdpChatThread(String chatID){
        ArrayList<String> tmp = new ArrayList<>();
        tmp.add(chatID);
        clearChatPane(tmp);
        openChatTabs.get(chatID).setActive(false);
    }
/*
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
*/
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
            if (allActiveChats != null){
                clearChatPane(allActiveChats);
            }
            if(listOfFileReceiverProcesses.size() != 0){
                //TODO iterate through all elements, call stop function;
            }
            if(listOfFileSenderProcesses.size() != 0){

            }
            FriendchatsListener.stopServer();

            allActiveChats = new ArrayList<>();
            openChatTabs = new ConcurrentHashMap<>();
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


