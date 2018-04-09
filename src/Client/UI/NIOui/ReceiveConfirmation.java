package Client.UI.NIOui;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.Optional;
import java.util.concurrent.Callable;

public class ReceiveConfirmation implements Callable{
    private String filename, username;

    public ReceiveConfirmation(String filename, String uName){
        this.filename = filename;
        this.username = uName;
    }

    @Override
    public Object call() throws Exception {
        File selectedDirectory = null;
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Incoming File");
        alert.setHeaderText(filename);
        alert.setContentText("Do you want to receive a file from user " + username + "?");

        String retVal = null;

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK){
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Pick a save location.");
            Stage stage = new Stage();
            selectedDirectory = directoryChooser.showDialog(stage);
            retVal = selectedDirectory.getPath();
        }
        return retVal;
    }
}
