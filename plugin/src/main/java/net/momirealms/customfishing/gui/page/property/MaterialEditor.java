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

package net.momirealms.customfishing.gui.page.property;

import net.momirealms.customfishing.adventure.AdventureManagerImpl;
import net.momirealms.customfishing.adventure.component.ShadedAdventureComponentWrapper;
import net.momirealms.customfishing.api.CustomFishingPlugin;
import net.momirealms.customfishing.gui.SectionPage;
import net.momirealms.customfishing.gui.icon.BackGroundItem;
import net.momirealms.customfishing.mechanic.item.ItemManagerImpl;
import net.momirealms.customfishing.setting.CFLocale;
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

public class MaterialEditor {

    private final Player player;
    private final SectionPage parentPage;
    private String material;
    private final ConfigurationSection section;

    public MaterialEditor(Player player, SectionPage parentPage) {
        this.player = player;
        this.parentPage = parentPage;
        this.section = parentPage.getSection();
        this.material = section.getString("material");

        Item border = new SimpleItem(new ItemBuilder(Material.AIR));
        var confirm = new ConfirmIcon();
        var itemBuilder = new ItemBuilder(CustomFishingPlugin.get()
                .getItemManager()
                .getItemStackAppearance(player, material)
        )
        .setDisplayName(section.getString("material", ""));

        if (section.contains("custom-model-data"))
            itemBuilder.setCustomModelData(section.getInt("custom-model-data", 0));

        Gui upperGui = Gui.normal()
                .setStructure("a # b")
                .addIngredient('a', itemBuilder)
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
                .setTitle(new ShadedAdventureComponentWrapper(
                        AdventureManagerImpl.getInstance().getComponentFromMiniMessage(CFLocale.GUI_TITLE_MATERIAL)
                ))
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
        for (String lib : ((ItemManagerImpl) CustomFishingPlugin.get().getItemManager()).getItemLibraries()) {
            switch (lib) {
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
                return new ItemBuilder(Material.STRUCTURE_VOID).setDisplayName(new ShadedAdventureComponentWrapper(AdventureManagerImpl.getInstance().getComponentFromMiniMessage(
                        CFLocale.GUI_DELETE_PROPERTY
                )));
            } else {
                var builder = new ItemBuilder(
                        CustomFishingPlugin.get()
                                .getItemManager()
                                .getItemStackAppearance(player, material)
                ).setDisplayName(CFLocale.GUI_NEW_VALUE + material)
                        .addLoreLines(new ShadedAdventureComponentWrapper(AdventureManagerImpl.getInstance().getComponentFromMiniMessage(
                                CFLocale.GUI_CLICK_CONFIRM
                        )));
                if (section.contains("custom-model-data"))
                    builder.setCustomModelData(section.getInt("custom-model-data"));
                return builder;
            }
        }

        @Override
        public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
            if (material == null || material.isEmpty()) {
                section.set("material", null);
            } else if (CustomFishingPlugin.get().getItemManager().getItemStackAppearance(player, material).getType() == Material.BARRIER) {
                return;
            } else {
                section.set("material", material);
            }
            parentPage.reOpen();
            parentPage.save();
        }
    }
}
