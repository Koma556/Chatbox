package Client.UI.FileSenderWindow;

import Client.UI.TestUI;

public class FileSenderStatusUpdate implements Runnable {
    private String text;
    private int id;

    public FileSenderStatusUpdate(String text, int id){
        this.text = text;
        this.id = id;
    }

    @Override
    public void run() {
        TestUI.controller.updateFileSenderStatus(text, id);
    }
}
