package Client.UI;

import Client.Core;
import Communication.Message;
import Communication.User;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Controller {

    // create new user instance I will then pass onto the logincontroller. If needed this could be created at an even higher level
    // all user-related data is saved here
    // currently using the same class the server uses. This could be modified easily
    private User myUser = new User();

    public void loginMenuItem() {
        // opens a login window for the user
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("loginWindow.fxml"));
            Parent root1 = (Parent) fxmlLoader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root1));
            // acquiring a reference to the newly instantiated controller so that I can call its methods
            LoginController userAcquisitionController = fxmlLoader.<LoginController>getController();
            // setting my User object to the new controller which will then save username and socket on it
            userAcquisitionController.setUser(myUser);
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
            // acquiring a reference to the newly instantiated controller so that I can call its methods
            RegisterController userAcquisitionController = fxmlLoader.<RegisterController>getController();
            // setting my User object to the new controller which will then save username and socket on it
            userAcquisitionController.setUser(myUser);
            stage.show();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void logoutMenuItem(){
        if(myUser != null && myUser.getMySocket() != null && !myUser.getMySocket().isClosed()) {
            if(Core.Logout(myUser.getName(), myUser.getMySocket()))
                System.out.println("Server acknowledged.");
            try {
                myUser.getMySocket().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            myUser = new User();

            // TODO: convert this to a popup window
            System.out.println("Logged out.");
        }
        else
            System.out.println("Nothing to log out.");
    }

    public void closeMenuItem(){
        logoutMenuItem();
        Platform.exit();
    }

    public void sendButtonClicked(){
        // debug, to showcase that indeed the User has been saved
        System.out.println(myUser.getName());
    }

}


