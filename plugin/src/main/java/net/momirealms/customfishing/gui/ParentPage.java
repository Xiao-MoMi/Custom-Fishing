package net.momirealms.customfishing.gui;

import net.momirealms.customfishing.gui.icon.BackToPageItem;
import xyz.xenondevs.invui.item.Item;

public interface ParentPage {

    void reOpen();

    default Item getBackItem() {
        return new BackToPageItem(this);
    }
}
