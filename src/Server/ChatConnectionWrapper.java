package Server;

public class ChatConnectionWrapper {
    private boolean active;

    public ChatConnectionWrapper(){
        this.active = true;
    }

    public synchronized boolean isActive() {
        return active;
    }

    public synchronized void closeConnection() {
        this.active = false;
    }
}
