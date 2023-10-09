package net.momirealms.customfishing.gui.icon.property;

import net.momirealms.customfishing.adventure.AdventureManagerImpl;
import net.momirealms.customfishing.adventure.component.ShadedAdventureComponentWrapper;
import net.momirealms.customfishing.gui.ItemPage;
import net.momirealms.customfishing.gui.page.property.CustomModelDataEditor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;

public class CMDItem extends AbstractItem {

    private final ItemPage itemPage;

    public CMDItem(ItemPage itemPage) {
        this.itemPage = itemPage;
    }

    @Override
    public ItemProvider getItemProvider() {
        ItemBuilder itemBuilder = new ItemBuilder(Material.GLOW_INK_SAC)
                .setDisplayName(new ShadedAdventureComponentWrapper(AdventureManagerImpl.getInstance().getComponentFromMiniMessage(
                        "<#FFC0CB>● Custom Model Data"
                )));

        if (itemPage.getSection().contains("custom-model-data")) {
            itemBuilder.addLoreLines(new ShadedAdventureComponentWrapper(AdventureManagerImpl.getInstance().getComponentFromMiniMessage(
                            "<gray>Current value: <white>" + itemPage.getSection().getInt("custom-model-data")
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
            new CustomModelDataEditor(player, itemPage, itemPage.getSection().getString("material", ""), itemPage.getSection());
        } else if (clickType.isRightClick()) {
            itemPage.getSection().set("custom-model-data", null);
            itemPage.save();
            itemPage.reOpen();
        }
    }
}
