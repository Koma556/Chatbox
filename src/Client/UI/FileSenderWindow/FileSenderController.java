package Client.UI.FileSenderWindow;

import Client.UI.Controller;
import Client.UI.TestUI;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import static Client.UI.Controller.listOfFileSenderProcesses;

public class FileSenderController{
    @FXML
    private javafx.scene.control.Button cancelButton;
    @FXML
    private Label statusLabel;
    private int id;

    public void setId(int id){
        this.id = id;
    }

    public void setStatusLabel(String text){
        statusLabel.textProperty().setValue(text);
    }

    public void pressCancel(){
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    public void stop(){
        if(listOfFileSenderProcesses.containsKey(id))
            listOfFileSenderProcesses.get(id).setDone(true);
    }
}
