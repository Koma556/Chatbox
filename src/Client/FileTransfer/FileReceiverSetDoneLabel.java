package Client.FileTransfer;

import static Client.UI.Controller.listOfFileReceiverProcesses;

public class FileReceiverSetDoneLabel implements Runnable {
    private int id;

    public FileReceiverSetDoneLabel(int id) {
        this.id = id;
    }

    @Override
    public void run() {
        if(listOfFileReceiverProcesses.containsKey(id)){
            listOfFileReceiverProcesses.get(id).getController().notificationComplete();
        }
    }
}
