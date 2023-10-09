package net.momirealms.customfishing.gui.icon;

import net.momirealms.customfishing.adventure.AdventureManagerImpl;
import net.momirealms.customfishing.adventure.component.ShadedAdventureComponentWrapper;
import net.momirealms.customfishing.gui.ParentPage;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;

public class BackToPageItem extends AbstractItem {

    private final ParentPage parentPage;

    public BackToPageItem(ParentPage parentPage) {
        this.parentPage = parentPage;
    }

    @Override
    public ItemProvider getItemProvider() {
        return new ItemBuilder(Material.ORANGE_STAINED_GLASS_PANE)
                .setDisplayName(new ShadedAdventureComponentWrapper(AdventureManagerImpl.getInstance().getComponentFromMiniMessage(
                        "<#FF8C00>Back to parent page"
                )));
    }

    @Override
    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
        parentPage.reOpen();
    }
}
