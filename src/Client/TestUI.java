package Client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class TestUI extends Application{

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("clientGUI.fxml"));
        primaryStage.setTitle("Social Gossip Client");
        primaryStage.setScene((new Scene(root, 800, 500)));
        primaryStage.show();
    }

}
