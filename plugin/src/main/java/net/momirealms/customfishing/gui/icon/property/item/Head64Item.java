package net.momirealms.customfishing.gui.icon.property.item;

import net.momirealms.customfishing.CustomFishingPluginImpl;
import net.momirealms.customfishing.adventure.AdventureManagerImpl;
import net.momirealms.customfishing.adventure.component.ShadedAdventureComponentWrapper;
import net.momirealms.customfishing.api.CustomFishingPlugin;
import net.momirealms.customfishing.gui.SectionPage;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;

import java.util.ArrayList;

public class Head64Item extends AbstractItem {

    private final SectionPage itemPage;

    public Head64Item(SectionPage itemPage) {
        this.itemPage = itemPage;
    }

    @Override
    public ItemProvider getItemProvider() {
        ItemBuilder itemBuilder = new ItemBuilder(Material.PLAYER_HEAD)
                .setDisplayName(new ShadedAdventureComponentWrapper(AdventureManagerImpl.getInstance().getComponentFromMiniMessage(
                        "<#2E8B57>● Head64"
                )));

        if (itemPage.getSection().contains("head64")) {
            itemBuilder.addLoreLines(new ShadedAdventureComponentWrapper(AdventureManagerImpl.getInstance().getComponentFromMiniMessage(
                            "<gray>Current value: <white>"
                    )));
            String head64 = itemPage.getSection().getString("head64", "");

            ArrayList<String> list = new ArrayList<>();
            for (int i = 0; i < head64.length(); i += 16) {
                if (i + 16 > head64.length()) {
                    list.add(head64.substring(i));
                } else {
                    list.add(head64.substring(i, i + 16));
                }
            }
            for (String line : list) {
                itemBuilder.addLoreLines(new ShadedAdventureComponentWrapper(AdventureManagerImpl.getInstance().getComponentFromMiniMessage(
                        "<white>"+ line
                )));
            }

            itemBuilder.addLoreLines("").addLoreLines(new ShadedAdventureComponentWrapper(AdventureManagerImpl.getInstance().getComponentFromMiniMessage(
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
            player.closeInventory();
            AdventureManagerImpl.getInstance().sendMessageWithPrefix(player, "Input the head64 value in chat");
            ((CustomFishingPluginImpl) CustomFishingPlugin.get()).getChatCatcherManager().catchMessage(player, "head64", itemPage);
        } else if (clickType.isRightClick()) {
            itemPage.getSection().set("head64", null);
            itemPage.save();
            itemPage.reOpen();
        }
    }
}
