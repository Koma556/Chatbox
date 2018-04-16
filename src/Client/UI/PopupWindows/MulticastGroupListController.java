package Client.UI.PopupWindows;

import Client.Core;
import Client.UDP.UDPClient;
import Client.UI.ColoredText;
import Client.UI.CoreUI;
import Communication.Message;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.ArrayList;

import static Client.UI.CoreUI.myUser;

public class MulticastGroupListController {


    private ObservableList<ColoredText> groups;
    @FXML
    private javafx.scene.control.ListView<ColoredText> allGroupsListView;
    @FXML
    private javafx.scene.control.Button closeButton, reloadButton;

    public void clickOnRoomName(){
        String chatID = allGroupsListView.getSelectionModel().getSelectedItem().getText();
        // only ask to join if I'm not already in the group, no need to let the server bother with that
        if(allGroupsListView.getSelectionModel().getSelectedItem().getColor().equals(Color.RED)) {
            Message msg = new Message("OP_JON_GRP", chatID);
            try {
                msg.send(myUser.getMySocket());
            } catch (java.io.IOException e) {
                e.printStackTrace();
            }
            // actually open the tab
            Message reply = new Message();
            if (Core.waitOkAnswer(reply, myUser.getMySocket())) {
                String[] ports = reply.getData().split(":");
                int portIn = Integer.parseInt(ports[0]);
                int portOut = Integer.parseInt(ports[1]);
                UDPClient newChat = new UDPClient(portIn, portOut, chatID);
                Thread newChatThread = new Thread(newChat);
                newChatThread.start();
            }
        }
    }

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
                if(CoreUI.controller.openGroupChats.containsKey(group))
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
