package net.momirealms.customfishing.gui.page.property;

import net.momirealms.customfishing.adventure.AdventureManagerImpl;
import net.momirealms.customfishing.adventure.component.ShadedAdventureComponentWrapper;
import net.momirealms.customfishing.gui.YamlPage;
import net.momirealms.customfishing.gui.icon.BackGroundItem;
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

import java.util.ArrayList;
import java.util.List;

public class LoreEditor {

    private final Player player;
    private final YamlPage parentPage;
    private final ArrayList<String> lore;
    private final ConfigurationSection section;
    private int index;

    public LoreEditor(Player player, YamlPage parentPage, ConfigurationSection section) {
        this.player = player;
        this.parentPage = parentPage;
        this.section = section;
        this.index = 0;
        this.lore = new ArrayList<>(section.getStringList("display.lore"));
        this.lore.add(0, "Select one line");
        reOpen(0);
    }

    public void reOpen(int idx) {
        Item border = new SimpleItem(new ItemBuilder(Material.AIR));
        var confirm  = new ConfirmIcon();
        Gui upperGui = Gui.normal()
                .setStructure(
                        "a # b"
                )
                .addIngredient('a', new ItemBuilder(Material.NAME_TAG).setDisplayName(lore.get(idx)))
                .addIngredient('#', border)
                .addIngredient('b', confirm)
                .build();

        var gui = PagedGui.items()
                .setStructure(
                        "x x x x x x x x x",
                        "x x x x x x x x x",
                        "x x x x x x x x x",
                        "# # # # c # # # #"
                )
                .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
                .addIngredient('c', parentPage.getBackItem())
                .addIngredient('#', new BackGroundItem())
                .setContent(getContents())
                .build();

        var window = AnvilWindow.split()
                .setViewer(player)
                .setTitle(new ShadedAdventureComponentWrapper(
                        AdventureManagerImpl.getInstance().getComponentFromMiniMessage("Edit Lore")
                ))
                .addRenameHandler(s -> {
                    if (index == 0) return;
                    lore.set(index, s);
                    confirm.notifyWindows();
                })
                .setUpperGui(upperGui)
                .setLowerGui(gui)
                .build();

        window.open();
    }

    public List<Item> getContents() {
        ArrayList<Item> items = new ArrayList<>();
        int i = 1;
        List<String> subList = lore.subList(1, lore.size());
        for (String lore : subList) {
            items.add(new LoreElement(lore, i++));
        }
        items.add(new AddLore());
        return items;
    }

    public class AddLore extends AbstractItem {

        @Override
        public ItemProvider getItemProvider() {
            return new ItemBuilder(Material.ANVIL).setDisplayName(new ShadedAdventureComponentWrapper(AdventureManagerImpl.getInstance().getComponentFromMiniMessage(
                    "<green>[+] <gray>Add a new line"
            )));
        }

        @Override
        public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
            lore.add("Text");
            index = lore.size() - 1;
            reOpen(index);
        }
    }

    public class LoreElement extends AbstractItem {

        private final String line;
        private final int idx;

        public LoreElement(String line, int idx) {
            this.line = line;
            this.idx = idx;
        }

        @Override
        public ItemProvider getItemProvider() {
            return new ItemBuilder(Material.PAPER).setDisplayName(new ShadedAdventureComponentWrapper(AdventureManagerImpl.getInstance().getComponentFromMiniMessage(
                    line
            ))).addLoreLines("")
                    .addLoreLines(new ShadedAdventureComponentWrapper(AdventureManagerImpl.getInstance().getComponentFromMiniMessage(
                            "<#00FF7F> -> Left click to edit"
                    ))).addLoreLines(new ShadedAdventureComponentWrapper(AdventureManagerImpl.getInstance().getComponentFromMiniMessage(
                            "<#FF6347> -> Right click to delete"
                    )));
        }

        @Override
        public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
            if (clickType == ClickType.LEFT) {
                index = idx;
                reOpen(idx);
            } else if (clickType == ClickType.RIGHT) {
                lore.remove(idx);
                index = Math.min(index, lore.size() - 1);
                reOpen(index);
            }
        }
    }

    public class ConfirmIcon extends AbstractItem {

        @Override
        public ItemProvider getItemProvider() {
            List<String> subList = lore.subList(1, lore.size());
            if (subList.isEmpty()) {
                return new ItemBuilder(Material.STRUCTURE_VOID).setDisplayName(new ShadedAdventureComponentWrapper(AdventureManagerImpl.getInstance().getComponentFromMiniMessage(
                        "<#00CED1>● Delete property"
                )));
            } else {
                var builder = new ItemBuilder(Material.NAME_TAG)
                        .setDisplayName(new ShadedAdventureComponentWrapper(AdventureManagerImpl.getInstance().getComponentFromMiniMessage(
                                "<#00FF7F> -> Click to confirm"
                        )));
                for (String lore : subList) {
                    builder.addLoreLines(new ShadedAdventureComponentWrapper(AdventureManagerImpl.getInstance().getComponentFromMiniMessage(
                            " <gray>-</gray> " + lore
                    )));
                }
                return builder;
            }
        }

        @Override
        public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
            List<String> subList = lore.subList(1, lore.size());
            if (lore.isEmpty()) {
                section.set("display.lore", null);
            } else {
                section.set("display.lore", subList);
            }
            parentPage.reOpen();
            parentPage.save();
        }
    }
}
