package Client.UI.FileReceiverWindow;

import static Client.UI.Controller.listOfFileReceiverProcesses;

public class FileReceiverCountdown implements Runnable {
    private int id, second;

    public FileReceiverCountdown(int id, int second) {
        this.id = id;
        this.second = second;
    }

    @Override
    public void run() {
        if (second > 0 && listOfFileReceiverProcesses.containsKey(id)) {
            listOfFileReceiverProcesses.get(id).getController().countdownTo(second);
        } else if (second == 0 && listOfFileReceiverProcesses.containsKey(id)){
            if (!listOfFileReceiverProcesses.get(id).isAccepted()) {
                listOfFileReceiverProcesses.get(id).getController().notificationTimeout();
            } else {
                listOfFileReceiverProcesses.get(id).getController().notificationAccepted();
            }
        }
    }
}
