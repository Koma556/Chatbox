package Client.UI;

import Client.RMI.UserCallback;
import Client.RMI.UserCallbackImplementation;
import Client.User;
import Server.RMI.CallbackInterface;
import Server.RMI.LoginCallback;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ThreadLocalRandom;

public class TestUI extends Application{

    public static Controller controller;
    public static Stage pStage;
    public static User myUser = new User();
    public static int sessionClientPort;

    @Override
    public void start(Stage primaryStage) throws Exception{
        sessionClientPort = ThreadLocalRandom.current().nextInt(49152, 65535 + 1);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("clientGUI.fxml"));
        Parent root = (Parent)loader.load();
        controller = (Controller)loader.getController();
        primaryStage = new Stage();
        primaryStage.setTitle("Social Gossip Client");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
        setPrimaryStage(primaryStage);
    }

    public void stop(){
        controller.logoutMenuItem();
    }

    public static Stage getPrimaryStage() {
        return pStage;
    }

    private void setPrimaryStage(Stage pStage) {
        TestUI.pStage = pStage;
    }

}
