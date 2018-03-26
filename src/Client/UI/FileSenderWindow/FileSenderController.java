package Client.UI.FileSenderWindow;

import Client.UI.TestUI;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class FileSenderController{
    @FXML
    private javafx.scene.control.Button cancelButton;
    @FXML
    private Label statusLabel;

    public void setStatusLabel(String text){
        statusLabel.textProperty().setValue(text);
    }

    public void pressCancel(){
        TestUI.controller.fileSend.stop();
        TestUI.controller.fileSenderController = null;
        TestUI.controller.fileSend = null;
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

}
