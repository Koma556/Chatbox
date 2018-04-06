package Client.UI;

public class FriendListUpdate implements Runnable {
    @Override
    public void run() {
        CoreUI.controller.populateListView();
    }
}
