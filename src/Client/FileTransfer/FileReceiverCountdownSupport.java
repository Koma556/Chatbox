package Client.FileTransfer;

import Client.UI.FileReceiverWindow.FileReceiverCountdown;
import javafx.application.Platform;

import static Client.UI.Controller.listOfFileReceiverProcesses;

public class FileReceiverCountdownSupport implements Runnable {
    private final int TIMEOUT_TIMER = 10;
    private int id;

    public FileReceiverCountdownSupport(int id) {
        this.id = id;
    }

    @Override
    public void run() {
        int second = TIMEOUT_TIMER;
        while (listOfFileReceiverProcesses.containsKey(id) && second >= 0 && !listOfFileReceiverProcesses.get(id).isAccepted()) {
            FileReceiverCountdown frcd = new FileReceiverCountdown(id, second);
            Platform.runLater(frcd);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            second--;
        }
    }
}
