package Client.UI.FileReceiverWindow;

import Client.UI.TestUI;

import java.net.Socket;

public class OpenFRWindow implements Runnable {
    private String from, filename, fileSize;
    private Socket sock;

    public OpenFRWindow(String from, String filename, String fileSize, Socket sock){
        this.from = from;
        this.filename = filename;
        this.sock = sock;
        this.fileSize = fileSize;
    }

    @Override
    public void run() {
        TestUI.controller.loadFileReceiverPane(from, filename, fileSize, sock);
    }
}
