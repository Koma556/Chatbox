package Client.UI.FileSenderWindow;

import Client.UI.TestUI;
import javafx.beans.value.ObservableValue;

public class OpenFSWindow implements Runnable {
    private String text;
    private int id;

    public OpenFSWindow(String text, int id){
        this.text = text;
        this.id = id;
    }

    @Override
    public void run() {
        TestUI.controller.loadFileSenderPane(text, id);
    }
}
