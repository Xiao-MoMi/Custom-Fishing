package net.momirealms.customfishing.gui;

import net.momirealms.customfishing.adventure.AdventureManagerImpl;
import net.momirealms.customfishing.adventure.component.ShadedAdventureComponentWrapper;
import net.momirealms.customfishing.api.CustomFishingPlugin;
import net.momirealms.customfishing.api.util.LogUtils;
import net.momirealms.customfishing.gui.icon.BackGroundItem;
import net.momirealms.customfishing.gui.icon.BackToFolderItem;
import net.momirealms.customfishing.gui.icon.NextPageItem;
import net.momirealms.customfishing.gui.icon.PreviousPageItem;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ItemEditor {

    private static final String SEARCH = "Search";
    private final Player player;
    private final YamlConfiguration yaml;
    private String prefix;
    private final File file;

    public ItemEditor(Player player, File file) {
        this.yaml = YamlConfiguration.loadConfiguration(file);
        this.player = player;
        this.file = file;
        this.prefix = SEARCH;
        this.reOpenWithFilter();
    }

    public void reOpenWithFilter() {
        Item border = new SimpleItem(new ItemBuilder(Material.AIR));
        Gui upperGui = Gui.normal()
                .setStructure(
                        "a # #"
                )
                .addIngredient('a', new SimpleItem(new ItemBuilder(Material.NAME_TAG).setDisplayName(prefix)))
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
                .addIngredient('c', new BackToFolderItem(file.getParentFile()))
                .setContent(getItemList())
                .build();

        var temp = prefix;
        var window = AnvilWindow.split()
                .setViewer(player)
                .setTitle(new ShadedAdventureComponentWrapper(
                        AdventureManagerImpl.getInstance().getComponentFromMiniMessage("Select item to edit")
                ))
                .addRenameHandler(s -> {
                    if (s.equals(temp)) return;
                    prefix = s;
                    reOpenWithFilter();
                })
                .setUpperGui(upperGui)
                .setLowerGui(gui)
                .build();

        window.open();
    }

    public List<Item> getItemList() {
        List<Item> itemList = new ArrayList<>();
        for (Map.Entry<String, Object> entry : this.yaml.getValues(false).entrySet()) {
            String key = entry.getKey();
            if (entry.getValue() instanceof ConfigurationSection section) {
                if (!prefix.equals(SEARCH) && !entry.getKey().startsWith(prefix)) continue;
                String material = section.getString("material");
                if (material != null) {
                    if (material.contains(":")) {
                        ItemStack itemStack = CustomFishingPlugin.get().getItemManager().buildAnyPluginItemByID(player, material);
                        if (itemStack != null) {
                            ItemBuilder itemBuilder = new ItemBuilder(itemStack.getType());
                            itemBuilder.setCustomModelData(itemStack.getItemMeta().getCustomModelData());
                            itemBuilder.setCustomModelData(section.getInt("custom-model-data"));
                            itemList.add(new ItemInList(key, itemBuilder, this));
                            continue;
                        }
                    } else {
                        ItemBuilder itemBuilder = new ItemBuilder(Material.valueOf(material.toUpperCase(Locale.ENGLISH)));
                        itemBuilder.setCustomModelData(section.getInt("custom-model-data"));
                        itemList.add(new ItemInList(key, itemBuilder, this));
                        continue;
                    }
                }
            }
            itemList.add(new ItemInList(key, new ItemBuilder(Material.STRUCTURE_VOID), this));
        }
        return itemList;
    }

    public void removeKey(String key) {
        yaml.set(key, null);
    }

    public void save() {
        try {
            yaml.save(file);
        } catch (IOException e) {
            LogUtils.warn("Failed to save file", e);
        }
    }

    public static class ItemInList extends AbstractItem {

        private final String key;
        private final ItemBuilder itemBuilder;
        private final ItemEditor itemEditor;

        public ItemInList(String key, ItemBuilder itemBuilder, ItemEditor itemEditor) {
            this.key = key;
            this.itemBuilder = itemBuilder;
            this.itemEditor = itemEditor;
        }

        @Override
        public ItemProvider getItemProvider() {
            return itemBuilder.setDisplayName(new ShadedAdventureComponentWrapper(AdventureManagerImpl.getInstance().getComponentFromMiniMessage(
                    key
            ))).addLoreLines(new ShadedAdventureComponentWrapper(AdventureManagerImpl.getInstance().getComponentFromMiniMessage(
                    "<green>Left click to edit"
            ))).addLoreLines(new ShadedAdventureComponentWrapper(AdventureManagerImpl.getInstance().getComponentFromMiniMessage(
                    "<red>Right click to delete"
            )));
        }

        @Override
        public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
            if (clickType.isLeftClick()) {

            } else if (clickType.isRightClick()) {
                this.itemEditor.removeKey(key);
                this.itemEditor.save();
                this.itemEditor.reOpenWithFilter();
            }
        }
    }
}
