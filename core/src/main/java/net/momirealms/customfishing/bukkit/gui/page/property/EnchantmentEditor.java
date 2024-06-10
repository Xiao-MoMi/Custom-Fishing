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

package net.momirealms.customfishing.bukkit.gui.page.property;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import net.momirealms.customfishing.bukkit.adventure.ShadedAdventureComponentWrapper;
import net.momirealms.customfishing.bukkit.gui.SectionPage;
import net.momirealms.customfishing.bukkit.gui.icon.BackGroundItem;
import net.momirealms.customfishing.common.helper.AdventureHelper;
import net.momirealms.customfishing.common.locale.MessageConstants;
import net.momirealms.customfishing.common.locale.TranslationManager;
import org.bukkit.Material;
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
import java.util.Map;

public class EnchantmentEditor {

    private final Player player;
    private final SectionPage parentPage;
    private final ArrayList<String> enchantments;
    private final Section section;
    private int index;
    private final boolean store;

    public EnchantmentEditor(Player player, SectionPage parentPage, boolean store) {
        this.player = player;
        this.parentPage = parentPage;
        this.section = parentPage.getSection();
        this.store = store;
        this.index = 0;
        this.enchantments = new ArrayList<>();
        this.enchantments.add("Select one enchantment");
        Section eSection = section.getSection(store ? "stored-enchantments" : "enchantments");
        if (eSection != null)
            for (Map.Entry<String, Object> entry : eSection.getStringRouteMappedValues(false).entrySet()) {
                this.enchantments.add(entry.getKey() + ":" + entry.getValue());
            }
        reOpen(0);
    }

    public void reOpen(int idx) {
        Item border = new SimpleItem(new ItemBuilder(Material.AIR));
        var confirm  = new ConfirmIcon();
        Gui upperGui = Gui.normal()
                .setStructure("a # b")
                .addIngredient('a', new ItemBuilder(Material.NAME_TAG).setDisplayName(enchantments.get(idx)))
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
                        TranslationManager.render(store ? MessageConstants.GUI_PAGE_STORED_ENCHANTMENT_TITLE.build() : MessageConstants.GUI_PAGE_ENCHANTMENT_TITLE.build())
                ))
                .addRenameHandler(s -> {
                    if (index == 0) return;
                    enchantments.set(index, s);
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
        List<String> subList = enchantments.subList(1, enchantments.size());
        for (String lore : subList) {
            items.add(new EnchantmentElement(lore, i++));
        }
        items.add(new AddEnchantment());
        return items;
    }

    public class AddEnchantment extends AbstractItem {

        @Override
        public ItemProvider getItemProvider() {
            return new ItemBuilder(Material.ANVIL).setDisplayName(new ShadedAdventureComponentWrapper(TranslationManager.render(
                    MessageConstants.GUI_PAGE_ADD_NEW_ENCHANTMENT.build()
            )));
        }

        @Override
        public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
            enchantments.add("namespace:enchantment:level");
            index = enchantments.size() - 1;
            reOpen(index);
        }
    }

    public class EnchantmentElement extends AbstractItem {

        private final String line;
        private final int idx;

        public EnchantmentElement(String line, int idx) {
            this.line = line;
            this.idx = idx;
        }

        @Override
        public ItemProvider getItemProvider() {
            return new ItemBuilder(Material.ENCHANTED_BOOK)
                    .setDisplayName(new ShadedAdventureComponentWrapper(AdventureHelper.miniMessage(line)))
                    .addLoreLines("")
                    .addLoreLines(new ShadedAdventureComponentWrapper(TranslationManager.render(MessageConstants.GUI_LEFT_CLICK_EDIT.build())))
                    .addLoreLines(new ShadedAdventureComponentWrapper(TranslationManager.render(MessageConstants.GUI_RIGHT_CLICK_DELETE.build())));
        }

        @Override
        public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
            if (clickType == ClickType.LEFT) {
                index = idx;
                reOpen(idx);
            } else if (clickType == ClickType.RIGHT) {
                enchantments.remove(idx);
                index = Math.min(index, enchantments.size() - 1);
                reOpen(index);
            }
        }
    }

    public class ConfirmIcon extends AbstractItem {

        @Override
        public ItemProvider getItemProvider() {
            List<String> subList = enchantments.subList(1, enchantments.size());
            if (subList.isEmpty()) {
                return new ItemBuilder(Material.STRUCTURE_VOID).setDisplayName(new ShadedAdventureComponentWrapper(TranslationManager.render(MessageConstants.GUI_DELETE_PROPERTY.build())));
            } else {
                var builder = new ItemBuilder(Material.NAME_TAG).setDisplayName(new ShadedAdventureComponentWrapper(TranslationManager.render(MessageConstants.GUI_CLICK_CONFIRM.build())));
                for (String enchantment : subList) {
                    String[] split = enchantment.split(":");
                    if (split.length != 3) {
                        return new ItemBuilder(Material.BARRIER).setDisplayName(new ShadedAdventureComponentWrapper(TranslationManager.render(MessageConstants.GUI_ILLEGAL_FORMAT.build())));
                    }
                    try {
                        Integer.parseInt(split[2]);
                        builder.addLoreLines(new ShadedAdventureComponentWrapper(AdventureHelper.miniMessage(" <gray>-</gray> " + enchantment)));
                    } catch (NumberFormatException e) {
                        return new ItemBuilder(Material.BARRIER).setDisplayName(new ShadedAdventureComponentWrapper(TranslationManager.render(MessageConstants.GUI_ILLEGAL_FORMAT.build())));
                    }
                }
                return builder;
            }
        }

        @Override
        public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
            List<String> subList = enchantments.subList(1, enchantments.size());
            for (String line : subList) {
                String[] split = line.split(":");
                if (split.length != 3) {
                    return;
                }
                try {
                    Integer.parseInt(split[2]);
                } catch (NumberFormatException e) {
                    return;
                }
            }
            section.set(store ? "stored-enchantments" : "enchantments", null);
            for (String line : subList) {
                String[] split = line.split(":");
                section.set((store ? "stored-enchantments" : "enchantments") + "." + split[0] + ":" + split[1], Integer.parseInt(split[2]));
            }
            parentPage.reOpen();
            parentPage.save();
        }
    }
}
