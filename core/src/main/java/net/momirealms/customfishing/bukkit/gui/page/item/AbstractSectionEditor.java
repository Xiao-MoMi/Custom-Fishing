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

import dev.dejvokep.boostedyaml.block.implementation.Section;
import net.kyori.adventure.text.Component;
import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.bukkit.adventure.ShadedAdventureComponentWrapper;
import net.momirealms.customfishing.bukkit.gui.SectionPage;
import net.momirealms.customfishing.bukkit.gui.icon.BackGroundItem;
import net.momirealms.customfishing.bukkit.gui.icon.BackToPageItem;
import net.momirealms.customfishing.bukkit.gui.icon.NextPageItem;
import net.momirealms.customfishing.bukkit.gui.icon.PreviousPageItem;
import net.momirealms.customfishing.common.locale.MessageConstants;
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

import java.util.List;

public abstract class AbstractSectionEditor implements SectionPage {

    protected final Player player;
    protected final ItemSelector itemSelector;
    protected final Section section;
    protected final String key;

    public AbstractSectionEditor(Player player, ItemSelector itemSelector, Section section, String key) {
        this.player = player;
        this.itemSelector = itemSelector;
        this.section = section;
        this.key = key;
        this.reOpen();
    }

    @Override
    public Section getSection() {
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
                .setTitle(new ShadedAdventureComponentWrapper(MessageConstants.GUI_EDIT_KEY.arguments(Component.text(key)).build()))
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
            return null;
            //return new ItemBuilder(BukkitCustomFishingPlugin.getInstance().getItemManager().getItemBuilder(section, "bait", key).build(player));
        }

        @Override
        public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
            notifyWindows();
        }
    }

    public abstract List<Item> getItemList();
}
