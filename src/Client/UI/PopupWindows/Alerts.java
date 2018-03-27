package Client.UI.PopupWindows;

import Client.UI.TestUI;

public class Alerts implements Runnable{
    private String title, header, content;

    public Alerts(String title, String header, String content) {
        this.title = title;
        this.header = header;
        this.content = content;
    }

    @Override
    public void run() {
        TestUI.controller.alertItem(title, header, content);
    }
}
