/*
 *  Copyright (C) <2022> <XiaoMoMi>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.momirealms.customfishing.gui.page.item;

import de.tr7zw.changeme.nbtapi.NBTItem;
import net.momirealms.customfishing.adventure.AdventureManagerImpl;
import net.momirealms.customfishing.adventure.component.ShadedAdventureComponentWrapper;
import net.momirealms.customfishing.api.CustomFishingPlugin;
import net.momirealms.customfishing.api.util.LogUtils;
import net.momirealms.customfishing.gui.YamlPage;
import net.momirealms.customfishing.gui.icon.BackGroundItem;
import net.momirealms.customfishing.gui.icon.BackToFolderItem;
import net.momirealms.customfishing.gui.icon.NextPageItem;
import net.momirealms.customfishing.gui.icon.PreviousPageItem;
import net.momirealms.customfishing.setting.CFLocale;
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
import java.util.Map;

public class ItemSelector implements YamlPage {

    private final String SEARCH;
    private final Player player;
    private final YamlConfiguration yaml;
    private String prefix;
    private final File file;
    private long coolDown;
    private final String type;

    public ItemSelector(Player player, File file, String type) {
        this.yaml = YamlConfiguration.loadConfiguration(file);
        this.player = player;
        this.file = file;
        this.type = type;
        this.SEARCH = CFLocale.GUI_SEARCH;
        this.prefix = SEARCH;
        this.reOpenWithFilter(SEARCH);
    }

    @Override
    public void reOpen() {
        reOpenWithFilter(prefix);
    }

    public void reOpenWithFilter(String filter) {
        Item border = new SimpleItem(new ItemBuilder(Material.AIR));
        Gui upperGui = Gui.normal()
                .setStructure("a # #")
                .addIngredient('a', new SimpleItem(new ItemBuilder(Material.NAME_TAG).setDisplayName(filter)))
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

        var window = AnvilWindow.split()
                .setViewer(player)
                .setTitle(new ShadedAdventureComponentWrapper(
                        AdventureManagerImpl.getInstance().getComponentFromMiniMessage(
                                CFLocale.GUI_SELECT_ITEM
                        )))
                .addRenameHandler(s -> {
                    long current = System.currentTimeMillis();
                    if (current - coolDown < 100) return;
                    if (s.equals(filter)) return;
                    prefix = s;
                    coolDown = current;
                    reOpenWithFilter(s);
                })
                .setUpperGui(upperGui)
                .setLowerGui(gui)
                .build();

        window.open();
    }

    public void reOpenWithNewKey() {
        String tempKey = CFLocale.GUI_TEMP_NEW_KEY;
        prefix = tempKey;
        var confirmIcon = new ConfirmIcon();
        Item border = new SimpleItem(new ItemBuilder(Material.AIR));
        Gui upperGui = Gui.normal()
                .setStructure("a # b")
                .addIngredient('a', new SimpleItem(new ItemBuilder(Material.NAME_TAG).setDisplayName(tempKey)))
                .addIngredient('b', confirmIcon)
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
                .build();

        var window = AnvilWindow.split()
                .setViewer(player)
                .setTitle(new ShadedAdventureComponentWrapper(
                        AdventureManagerImpl.getInstance().getComponentFromMiniMessage(CFLocale.GUI_SET_NEW_KEY)
                ))
                .addRenameHandler(s -> {
                    long current = System.currentTimeMillis();
                    if (current - coolDown < 100) return;
                    if (s.equals(tempKey)) return;
                    prefix = s;
                    coolDown = current;
                    confirmIcon.notifyWindows();
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
                    ItemStack build = CustomFishingPlugin.get().getItemManager().getItemBuilder(section, type, key).build(player);
                    NBTItem nbtItem = new NBTItem(build);
                    nbtItem.removeKey("display");
                    ItemBuilder itemBuilder = new ItemBuilder(nbtItem.getItem());
                    itemList.add(new ItemInList(key, itemBuilder, this));
                    continue;
                }
            }
            itemList.add(new ItemInList(key, new ItemBuilder(Material.STRUCTURE_VOID), this));
        }
        itemList.add(new AddKey());
        return itemList;
    }

    public void removeKey(String key) {
        yaml.set(key, null);
    }

    public void openEditor(String key) {
        switch (type) {
            case "item" -> new SectionEditor(player, key, this, yaml.getConfigurationSection(key));
            case "rod" -> new RodEditor(player, key, this, yaml.getConfigurationSection(key));
            case "bait" -> new BaitEditor(player, key, this, yaml.getConfigurationSection(key));
            case "hook" -> new HookEditor(player, key, this, yaml.getConfigurationSection(key));
        }
    }

    @Override
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
        private final ItemSelector itemSelector;

        public ItemInList(String key, ItemBuilder itemBuilder, ItemSelector itemSelector) {
            this.key = key;
            this.itemBuilder = itemBuilder.setDisplayName(new ShadedAdventureComponentWrapper(AdventureManagerImpl.getInstance().getComponentFromMiniMessage(
                            key
                    ))).addLoreLines("")
                    .addLoreLines(new ShadedAdventureComponentWrapper(AdventureManagerImpl.getInstance().getComponentFromMiniMessage(
                            CFLocale.GUI_LEFT_CLICK_EDIT
                    ))).addLoreLines(new ShadedAdventureComponentWrapper(AdventureManagerImpl.getInstance().getComponentFromMiniMessage(
                            CFLocale.GUI_RIGHT_CLICK_DELETE
                    )));
            this.itemSelector = itemSelector;
        }

        @Override
        public ItemProvider getItemProvider() {
            return itemBuilder;
        }

        @Override
        public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
            if (clickType.isLeftClick()) {
                this.itemSelector.openEditor(key);
            } else if (clickType.isRightClick()) {
                this.itemSelector.removeKey(key);
                this.itemSelector.save();
                this.itemSelector.reOpenWithFilter(itemSelector.prefix);
            }
        }
    }

    public class AddKey extends AbstractItem {

        @Override
        public ItemProvider getItemProvider() {
            return new ItemBuilder(Material.ANVIL).setDisplayName(new ShadedAdventureComponentWrapper(AdventureManagerImpl.getInstance().getComponentFromMiniMessage(
                    CFLocale.GUI_ADD_NEW_KEY
            )));
        }

        @Override
        public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
            reOpenWithNewKey();
        }
    }

    public class ConfirmIcon extends AbstractItem {

        @Override
        public ItemProvider getItemProvider() {
            if (prefix != null && !yaml.contains(prefix) && prefix.matches("^[a-zA-Z0-9_]+$")) {
                var builder = new ItemBuilder(Material.NAME_TAG)
                        .setDisplayName(new ShadedAdventureComponentWrapper(AdventureManagerImpl.getInstance().getComponentFromMiniMessage(
                                prefix
                        )));
                builder.addLoreLines(new ShadedAdventureComponentWrapper(AdventureManagerImpl.getInstance().getComponentFromMiniMessage(
                        CFLocale.GUI_CLICK_CONFIRM
                ))).addLoreLines(new ShadedAdventureComponentWrapper(AdventureManagerImpl.getInstance().getComponentFromMiniMessage(
                        CFLocale.GUI_RIGHT_CLICK_CANCEL
                )));
                return builder;
            } else {
                return new ItemBuilder(Material.BARRIER)
                        .setDisplayName(new ShadedAdventureComponentWrapper(AdventureManagerImpl.getInstance().getComponentFromMiniMessage(
                            CFLocale.GUI_DUPE_INVALID_KEY
                        )));
            }
        }

        @Override
        public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
            if (clickType.isLeftClick()) {
                if (prefix != null && !yaml.contains(prefix) && prefix.matches("^[a-zA-Z0-9_]+$")) {
                    yaml.createSection(prefix);
                    save();
                } else {
                    return;
                }
            }
            prefix = SEARCH;
            reOpenWithFilter(SEARCH);
        }
    }
}
