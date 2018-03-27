package Client.FileTransfer;

import Client.UI.FileSenderWindow.FileSenderController;

public class FileSenderWrapper {
    private FileSenderController controller;
    private String username;
    private boolean done = false;
    private Thread workerThread;

    public FileSenderController getController() {
        return controller;
    }

    public void setController(FileSenderController controller) {
        this.controller = controller;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public Thread getWorkerThread() {
        return workerThread;
    }

    public void setWorkerThread(Thread workerThread) {
        this.workerThread = workerThread;
    }

}
