package Client.UI;

import Client.UI.TestUI;
import javafx.scene.paint.Color;

import java.util.ArrayList;

public class ModifyFriendlistStatus implements Runnable{

    private boolean status;
    private String name;
    private ArrayList friends;

    public ModifyFriendlistStatus(String name, boolean status){
        this.name = name;
        this.status = status;
    }
    @Override
    public void run() {
        friends = TestUI.myUser.getArrayFriendList();
        friends.remove(name);
        if(status){
            // change name on list to be green
            TestUI.controller.changeColorListViewItem(new ColoredText(name, Color.GREEN));
        }else{
            // change name on list to be red
            TestUI.controller.changeColorListViewItem(new ColoredText(name, Color.RED));
            // lock a possible chat tab associated with it
            TestUI.controller.lockChatTabWrites(name);
        }
    }
}
