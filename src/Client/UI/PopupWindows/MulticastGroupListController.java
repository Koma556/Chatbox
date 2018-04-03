package Client.UI.PopupWindows;

import Client.Core;
import Client.UI.ColoredText;
import Client.UI.TestUI;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.ArrayList;

public class MulticastGroupListController {


    private ObservableList<ColoredText> groups;
    @FXML
    private javafx.scene.control.ListView<ColoredText> allGroupsListView;
    @FXML
    private javafx.scene.control.Button closeButton, reloadButton;

    public void populateView() {
        ArrayList<String> groupList = Core.getListOfMulticastGroups();
        //System.out.println("Received list of udp chatrooms");
        allGroupsListView.setCellFactory(lv -> new ListCell<ColoredText>() {
            @Override
            protected void updateItem(ColoredText item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null) {
                    setText(null);
                    setTextFill(null);
                } else {
                    setText(item.getText());
                    setTextFill(item.getColor());
                }
            }
        });
        groups = FXCollections.observableArrayList();
            int i = 0;
            for (String group : groupList) {
                //System.out.println("Grouplist["+i+"]: "+group);
                ColoredText grp;
                i++;
                if(TestUI.controller.openChatTabs.containsKey(group))
                    grp = new ColoredText(group, Color.GREEN);
                else
                    grp = new ColoredText(group, Color.RED);
                groups.add(grp);
            }
        allGroupsListView.setItems(groups);
    }

    @FXML
    public void closeButtonPress(){
        // get the stage to which cancelButton belongs
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}
