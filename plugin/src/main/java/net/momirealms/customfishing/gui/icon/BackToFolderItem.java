package net.momirealms.customfishing.gui.icon;

import net.momirealms.customfishing.adventure.AdventureManagerImpl;
import net.momirealms.customfishing.adventure.component.ShadedAdventureComponentWrapper;
import net.momirealms.customfishing.gui.Icon;
import net.momirealms.customfishing.gui.page.file.FileSelector;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;

import java.io.File;
import java.util.List;

public class BackToFolderItem extends AbstractItem implements Icon {

    private final File file;

    public BackToFolderItem(File file) {
        this.file = file;
    }

    @Override
    public ItemProvider getItemProvider() {
        if (file != null && file.getPath().startsWith("plugins\\CustomFishing\\contents")) {
            return new ItemBuilder(Material.ORANGE_STAINED_GLASS_PANE)
                    .setDisplayName(new ShadedAdventureComponentWrapper(AdventureManagerImpl.getInstance().getComponentFromMiniMessage(
                            "<#FF8C00>Back to parent folder"
                    )))
                    .setLore(List.of(new ShadedAdventureComponentWrapper(AdventureManagerImpl.getInstance().getComponentFromMiniMessage(
                            "<#FFA500>-> " + file.getName()
                    ))));
        } else {
            return new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE);
        }
    }

    @Override
    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
        if (file != null && file.getPath().startsWith("plugins\\CustomFishing\\contents"))
            new FileSelector(player, file);
    }
}
