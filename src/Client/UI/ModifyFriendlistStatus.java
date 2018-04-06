package Client.UI;

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
        if(status){
            // change name on list to be green
            CoreUI.controller.changeColorListViewItem(new ColoredText(name, Color.GREEN));
        }else{
            // change name on list to be red
            CoreUI.controller.changeColorListViewItem(new ColoredText(name, Color.RED));
            // lock a possible chat tab associated with it
            CoreUI.controller.lockChatTabWrites(name);
        }
    }
}
