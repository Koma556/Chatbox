package Client.UI.FileSenderWindow;

import Client.UI.TestUI;
import javafx.beans.value.ObservableValue;

public class OpenFSWindow implements Runnable {
    private String text;

    public OpenFSWindow(String text){
        this.text = text;
    }

    @Override
    public void run() {
        TestUI.controller.loadFileSenderPane(text);
    }
}
