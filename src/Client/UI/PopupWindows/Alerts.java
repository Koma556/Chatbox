package Client.UI.PopupWindows;

import Client.UI.CoreUI;

public class Alerts implements Runnable{
    private String title, header, content;

    public Alerts(String title, String header, String content) {
        this.title = title;
        this.header = header;
        this.content = content;
    }

    @Override
    public void run() {
        CoreUI.controller.alertItem(title, header, content);
    }
}
