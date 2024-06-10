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

package net.momirealms.customfishing.bukkit.gui.page.item;

import com.saicone.rtag.RtagItem;
import dev.dejvokep.boostedyaml.YamlDocument;
import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.bukkit.adventure.ShadedAdventureComponentWrapper;
import net.momirealms.customfishing.bukkit.gui.YamlPage;
import net.momirealms.customfishing.bukkit.gui.icon.BackGroundItem;
import net.momirealms.customfishing.bukkit.gui.icon.BackToFolderItem;
import net.momirealms.customfishing.bukkit.gui.icon.NextPageItem;
import net.momirealms.customfishing.bukkit.gui.icon.PreviousPageItem;
import net.momirealms.customfishing.common.helper.AdventureHelper;
import net.momirealms.customfishing.common.helper.VersionHelper;
import net.momirealms.customfishing.common.locale.MessageConstants;
import net.momirealms.customfishing.common.locale.TranslationManager;
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

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ItemSelector implements YamlPage {

    private final String SEARCH;
    private final Player player;
    private final YamlDocument yaml;
    private String prefix;
    private final File file;
    private long coolDown;
    private final String type;

    public ItemSelector(Player player, File file, String type) {
        this.yaml = BukkitCustomFishingPlugin.getInstance().getConfigManager().loadData(file);
        this.player = player;
        this.file = file;
        this.type = type;
        this.SEARCH = "Search";
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
                        TranslationManager.render(
                                MessageConstants.GUI_SELECT_ITEM.build()
                        )
                ))
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
        String tempKey = "ID";
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
                        TranslationManager.render(
                                MessageConstants.GUI_SET_NEW_KEY.build()
                        )
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
        for (Map.Entry<String, Object> entry : this.yaml.getStringRouteMappedValues(false).entrySet()) {
            String key = entry.getKey();
            if (entry.getValue() instanceof ConfigurationSection section) {
                if (!prefix.equals(SEARCH) && !entry.getKey().startsWith(prefix)) continue;
                String material = section.getString("material");
                if (material != null) {

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
            case "item" -> new SectionEditor(player, key, this, yaml.getSection(key));
            case "rod" -> new RodEditor(player, key, this, yaml.getSection(key));
            case "bait" -> new BaitEditor(player, key, this, yaml.getSection(key));
            case "hook" -> new HookEditor(player, key, this, yaml.getSection(key));
        }
    }

    @Override
    public void save() {
        try {
            yaml.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static class ItemInList extends AbstractItem {

        private final String key;
        private final ItemBuilder itemBuilder;
        private final ItemSelector itemSelector;

        public ItemInList(String key, ItemBuilder itemBuilder, ItemSelector itemSelector) {
            this.key = key;
            this.itemBuilder = itemBuilder
                    .setDisplayName(new ShadedAdventureComponentWrapper(AdventureHelper.miniMessage(key)))
                    .addLoreLines("")
                    .addLoreLines(new ShadedAdventureComponentWrapper(TranslationManager.render(MessageConstants.GUI_LEFT_CLICK_EDIT.build())))
                    .addLoreLines(new ShadedAdventureComponentWrapper(TranslationManager.render(MessageConstants.GUI_RIGHT_CLICK_DELETE.build())));
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
            return new ItemBuilder(Material.ANVIL).setDisplayName(new ShadedAdventureComponentWrapper(TranslationManager.render(
                    MessageConstants.GUI_PAGE_ADD_NEW_KEY.build()
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
                return new ItemBuilder(Material.NAME_TAG)
                        .setDisplayName(new ShadedAdventureComponentWrapper(AdventureHelper.miniMessage(prefix)))
                        .addLoreLines(new ShadedAdventureComponentWrapper(TranslationManager.render(MessageConstants.GUI_CLICK_CONFIRM.build())))
                        .addLoreLines(new ShadedAdventureComponentWrapper(TranslationManager.render(MessageConstants.GUI_RIGHT_CLICK_CANCEL.build())));
            } else {
                return new ItemBuilder(Material.BARRIER)
                        .setDisplayName(new ShadedAdventureComponentWrapper(TranslationManager.render(
                            MessageConstants.GUI_INVALID_KEY.build()
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
