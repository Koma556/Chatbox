package Client.UI.PopupWindows;

import javafx.scene.control.Alert;

public class Warning implements Runnable {
    private String title, header, content;

    public Warning(String title, String header, String content) {
        this.title = title;
        this.header = header;
        this.content = content;
    }

    @Override
    public void run() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        alert.showAndWait();
    }
}
