package Client.UI.FileReceiverWindow;

import Client.UI.TestUI;

public class FileReceiverAcceptPress implements Runnable {
    @Override
    public void run() {
        TestUI.controller.fileReceiverAcceptButtonPress();
    }
}
