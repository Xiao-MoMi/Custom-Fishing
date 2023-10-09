package net.momirealms.customfishing.gui.icon.property;

import net.momirealms.customfishing.adventure.AdventureManagerImpl;
import net.momirealms.customfishing.adventure.component.ShadedAdventureComponentWrapper;
import net.momirealms.customfishing.gui.ItemPage;
import net.momirealms.customfishing.gui.page.property.DisplayNameEditor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;

public class DisplayNameItem extends AbstractItem {

    private final ItemPage itemPage;

    public DisplayNameItem(ItemPage itemPage) {
        this.itemPage = itemPage;
    }

    @Override
    public ItemProvider getItemProvider() {
        ItemBuilder itemBuilder = new ItemBuilder(Material.NAME_TAG)
                .setDisplayName(new ShadedAdventureComponentWrapper(AdventureManagerImpl.getInstance().getComponentFromMiniMessage(
                        "<#FAFAD2>● Display Name"
                )));

        if (itemPage.getSection().contains("display.name")) {
            itemBuilder.addLoreLines(new ShadedAdventureComponentWrapper(AdventureManagerImpl.getInstance().getComponentFromMiniMessage(
                            "<gray>Current value: </gray>" + itemPage.getSection().getString("display.name")
                    )))
                    .addLoreLines("");
            itemBuilder.addLoreLines(new ShadedAdventureComponentWrapper(AdventureManagerImpl.getInstance().getComponentFromMiniMessage(
                    "<#00FF7F> -> Left click to edit"
            ))).addLoreLines(new ShadedAdventureComponentWrapper(AdventureManagerImpl.getInstance().getComponentFromMiniMessage(
                    "<#FF6347> -> Right click to reset"
            )));
        } else {
            itemBuilder.addLoreLines(new ShadedAdventureComponentWrapper(AdventureManagerImpl.getInstance().getComponentFromMiniMessage(
                    "<#00FF7F> -> Left click to set"
            )));
        }

        return itemBuilder;
    }

    @Override
    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
        if (clickType.isLeftClick()) {
            new DisplayNameEditor(player, itemPage, itemPage.getSection());
        } else if (clickType.isRightClick()) {
            itemPage.getSection().set("display.name", null);
            itemPage.save();
            itemPage.reOpen();
        }
    }
}
