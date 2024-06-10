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
import net.momirealms.customfishing.common.helper.AdventureHelper;
import net.momirealms.customfishing.common.locale.MessageConstants;
import net.momirealms.customfishing.common.locale.TranslationManager;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
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

public class ItemFlagEditor {

    private final Player player;
    private final SectionPage parentPage;
    private final List<String> flags;
    private final Section section;

    public ItemFlagEditor(Player player, SectionPage parentPage) {
        this.player = player;
        this.parentPage = parentPage;
        this.section = parentPage.getSection();
        this.flags = section.getStringList("item-flags");
        reOpen();
    }

    public void reOpen() {
        Gui upperGui = Gui.normal()
                .setStructure(
                        "# a #"
                )
                //.addIngredient('a', new ItemBuilder(BukkitCustomFishingPlugin.getInstance().getItemManager().getItemBuilder(section, "item", "id").build(player)))
                .addIngredient('#', new SimpleItem(new ItemBuilder(Material.AIR)))
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
                        TranslationManager.render(MessageConstants.GUI_PAGE_ITEM_FLAG_TITLE.build())
                ))
                .setUpperGui(upperGui)
                .setLowerGui(gui)
                .build();

        window.open();
    }

    public List<Item> getContents() {
        ArrayList<Item> items = new ArrayList<>();
        for (ItemFlag itemFlag : ItemFlag.values()) {
            items.add(new ItemFlagToggleItem(itemFlag.name()));
        }
        return items;
    }

    public class ItemFlagToggleItem extends AbstractItem {

        private final String flag;

        public ItemFlagToggleItem(String flag) {
            this.flag = flag;
        }

        @Override
        public ItemProvider getItemProvider() {
            if (flags.contains(flag)) {
                return new ItemBuilder(Material.GREEN_BANNER).setDisplayName(new ShadedAdventureComponentWrapper(AdventureHelper.miniMessage("<green>" + flag)));
            } else {
                return new ItemBuilder(Material.RED_BANNER).setDisplayName(new ShadedAdventureComponentWrapper(AdventureHelper.miniMessage("<red>" + flag)));
            }
        }

        @Override
        public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
            if (flags.contains(flag)) {
                flags.remove(flag);
            } else {
                flags.add(flag);
            }
            if (!flags.isEmpty()) {
                section.set("item-flags", flags);
            } else {
                section.set("item-flags", null);
            }
            parentPage.save();
            reOpen();
        }
    }
}
