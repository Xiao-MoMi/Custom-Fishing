package net.momirealms.customfishing.gui.page.item;

import net.momirealms.customfishing.adventure.AdventureManagerImpl;
import net.momirealms.customfishing.adventure.component.ShadedAdventureComponentWrapper;
import net.momirealms.customfishing.api.CustomFishingPlugin;
import net.momirealms.customfishing.gui.SectionPage;
import net.momirealms.customfishing.gui.icon.BackGroundItem;
import net.momirealms.customfishing.gui.icon.BackToPageItem;
import net.momirealms.customfishing.gui.icon.NextPageItem;
import net.momirealms.customfishing.gui.icon.PreviousPageItem;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.gui.PagedGui;
import xyz.xenondevs.invui.gui.structure.Markers;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;
import xyz.xenondevs.invui.item.impl.SimpleItem;
import xyz.xenondevs.invui.window.AnvilWindow;

import java.util.List;

public abstract class AbstractSectionEditor implements SectionPage {

    protected final Player player;
    protected final ItemSelector itemSelector;
    protected final ConfigurationSection section;
    protected final String key;

    public AbstractSectionEditor(Player player, ItemSelector itemSelector, ConfigurationSection section, String key) {
        this.player = player;
        this.itemSelector = itemSelector;
        this.section = section;
        this.key = key;
        this.reOpen();
    }

    @Override
    public ConfigurationSection getSection() {
        return section;
    }

    @Override
    public void reOpen() {
        Item border = new SimpleItem(new ItemBuilder(Material.AIR));
        Gui upperGui = Gui.normal()
                .setStructure(
                        "# a #"
                )
                .addIngredient('a', new RefreshExample())
                .addIngredient('#', border)
                .build();

        var gui = PagedGui.items()
                .setStructure(
                        "x x x x x x x x x",
                        "x x x x x x x x x",
                        "x x x x x x x x x",
                        "# # a # c # b # #"
                )
                .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
                .addIngredient('#', new BackGroundItem())
                .addIngredient('a', new PreviousPageItem())
                .addIngredient('b', new NextPageItem())
                .addIngredient('c', new BackToPageItem(itemSelector))
                .setContent(getItemList())
                .build();

        var window = AnvilWindow.split()
                .setViewer(player)
                .setTitle(new ShadedAdventureComponentWrapper(
                        AdventureManagerImpl.getInstance().getComponentFromMiniMessage("Edit " + key)
                ))
                .setUpperGui(upperGui)
                .setLowerGui(gui)
                .build();

        window.open();
    }

    @Override
    public void save() {
        itemSelector.save();
    }

    public class RefreshExample extends AbstractItem {

        @Override
        public ItemProvider getItemProvider() {
            return new ItemBuilder(CustomFishingPlugin.get().getItemManager().getItemBuilder(section, "bait", key).build(player));
        }

        @Override
        public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
            notifyWindows();
        }
    }

    public abstract List<Item> getItemList();
}
