package Client.UI.FileReceiverWindow;

import Client.UI.TestUI;

import java.net.Socket;

public class OpenFRWindow implements Runnable {
    private String from, filename;
    private Socket sock;

    public OpenFRWindow(String from, String filename, Socket sock){
        this.from = from;
        this.filename = filename;
        this.sock = sock;
    }

    @Override
    public void run() {
        TestUI.controller.loadFileReceiverPane(from, filename, sock);
    }
}
