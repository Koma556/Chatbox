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
import javafx.stage.Stage;

import java.io.IOException;

public class Controller {

    // create new user instance I will then pass onto the logincontroller. If needed this could be created at an even higher level
    // all user-related data is saved here
    // currently using the same class the server uses. This could be modified easily

    @FXML
    private javafx.scene.control.ListView friendListViewItem;

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


