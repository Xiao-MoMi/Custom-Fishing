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
import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.bukkit.adventure.ShadedAdventureComponentWrapper;
import net.momirealms.customfishing.bukkit.gui.SectionPage;
import net.momirealms.customfishing.bukkit.gui.icon.BackGroundItem;
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

public class MaterialEditor {

    private final Player player;
    private final SectionPage parentPage;
    private String material;
    private final Section section;

    public MaterialEditor(Player player, SectionPage parentPage) {
        this.player = player;
        this.parentPage = parentPage;
        this.section = parentPage.getSection();
        this.material = section.getString("material");

        Item border = new SimpleItem(new ItemBuilder(Material.AIR));
        var confirm = new ConfirmIcon();
//        var itemBuilder = new ItemBuilder(BukkitCustomFishingPlugin.get()
//                .getItemManager()
//                .getItemStackAppearance(player, material)
//        )
//        .setDisplayName(section.getString("material", ""));
//
//        if (section.contains("custom-model-data"))
//            itemBuilder.setCustomModelData(section.getInt("custom-model-data", 0));

        Gui upperGui = Gui.normal()
                .setStructure("a # b")
//                .addIngredient('a', itemBuilder)
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
                .setContent(getCompatibilityItemList())
                .build();

        var window = AnvilWindow.split()
                .setViewer(player)
                .setTitle(new ShadedAdventureComponentWrapper(TranslationManager.render(MessageConstants.GUI_PAGE_MATERIAL_TITLE.build())))
                .addRenameHandler(s -> {
                    material = s;
                    confirm.notifyWindows();
                })
                .setUpperGui(upperGui)
                .setLowerGui(gui)
                .build();

        window.open();
    }

    public List<Item> getCompatibilityItemList() {
        ArrayList<Item> items = new ArrayList<>();
        for (net.momirealms.customfishing.api.integration.ItemProvider lib : BukkitCustomFishingPlugin.getInstance().getItemManager().getItemProviders()) {
            switch (lib.identifier()) {
                case "MMOItems" -> items.add(new SimpleItem(new ItemBuilder(Material.BELL).setDisplayName(lib + ":TYPE:ID")));
                case "ItemsAdder" -> items.add(new SimpleItem(new ItemBuilder(Material.BELL).setDisplayName(lib + ":namespace:id")));
                case "vanilla", "CustomFishing" -> {}
                default -> items.add(new SimpleItem(new ItemBuilder(Material.BELL).setDisplayName(lib + ":ID")));
            }
        }
        return items;
    }

    public class ConfirmIcon extends AbstractItem {

        @Override
        public ItemProvider getItemProvider() {
            if (material == null || material.isEmpty()) {
                return new ItemBuilder(Material.STRUCTURE_VOID).setDisplayName(new ShadedAdventureComponentWrapper(TranslationManager.render(MessageConstants.GUI_DELETE_PROPERTY.build())));
            } else {
//                var builder = new ItemBuilder(
//                        BukkitCustomFishingPlugin.get()
//                                .getItemManager()
//                                .getItemStackAppearance(player, material)
//                ).setDisplayName(CFLocale.GUI_NEW_VALUE + material)
//                        .addLoreLines(new ShadedAdventureComponentWrapper(AdventureHelper.getInstance().getComponentFromMiniMessage(
//                                CFLocale.GUI_CLICK_CONFIRM
//                        )));
//                if (section.contains("custom-model-data"))
//                    builder.setCustomModelData(section.getInt("custom-model-data"));
//                return builder;
                return null;
            }
        }

        @Override
        public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
//            if (material == null || material.isEmpty()) {
//                section.set("material", null);
//            } else if (BukkitCustomFishingPlugin.get().getItemManager().getItemStackAppearance(player, material).getType() == Material.BARRIER) {
//                return;
//            } else {
//                section.set("material", material);
//            }
            parentPage.reOpen();
            parentPage.save();
        }
    }
}
