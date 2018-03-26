package Client.UI.FileSenderWindow;

import Client.UI.TestUI;

public class FileSenderStatusUpdate implements Runnable {
    private String text;

    public FileSenderStatusUpdate(String text){
        this.text = text;
    }

    @Override
    public void run() {
        TestUI.controller.updateFileSenderStatus(text);
    }
}
