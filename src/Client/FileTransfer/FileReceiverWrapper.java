package Client.FileTransfer;

import Client.UI.FileReceiverWindow.FileReceiverController;

public class FileReceiverWrapper {
    private FileReceiverController controller;
    private String username;
    private boolean done = false;
    private Runnable workerThread;

    public FileReceiverController getController() {
        return controller;
    }

    public void setController(FileReceiverController controller) {
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

    public Runnable getWorkerThread() {
        return workerThread;
    }

    public void setWorkerThread(Runnable workerThread) {
        this.workerThread = workerThread;
    }
}
