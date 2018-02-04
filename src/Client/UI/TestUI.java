package Client.UI;

import Communication.User;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class TestUI extends Application{

    public static Controller controller;
    public static Stage pStage;
    public static User myUser = new User();

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("clientGUI.fxml"));
        Parent root = (Parent)loader.load();
        controller = (Controller)loader.getController();
        primaryStage = new Stage();
        primaryStage.setTitle("Social Gossip Client");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
        setPrimaryStage(primaryStage);
    }

    public static Stage getPrimaryStage() {
        return pStage;
    }

    private void setPrimaryStage(Stage pStage) {
        TestUI.pStage = pStage;
    }

}
