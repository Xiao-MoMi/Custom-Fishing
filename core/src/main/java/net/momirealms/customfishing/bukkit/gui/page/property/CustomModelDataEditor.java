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
import net.kyori.adventure.text.Component;
import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.bukkit.adventure.ShadedAdventureComponentWrapper;
import net.momirealms.customfishing.bukkit.gui.SectionPage;
import net.momirealms.customfishing.bukkit.gui.icon.BackGroundItem;
import net.momirealms.customfishing.common.locale.MessageConstants;
import net.momirealms.customfishing.common.locale.TranslationManager;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.gui.PagedGui;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;
import xyz.xenondevs.invui.item.impl.SimpleItem;
import xyz.xenondevs.invui.window.AnvilWindow;

public class CustomModelDataEditor {

    private final Player player;
    private final SectionPage parentPage;
    private String cmd;
    private final Section section;
    private final String material;

    public CustomModelDataEditor(Player player, SectionPage parentPage) {
        this.player = player;
        this.parentPage = parentPage;
        this.section = parentPage.getSection();
        this.material = section.getString("material");

        Item border = new SimpleItem(new ItemBuilder(Material.AIR));
        var confirm = new ConfirmIcon();
        Gui upperGui = Gui.normal()
                .setStructure(
                        "a # b"
                )
                .addIngredient('#', border)
                .addIngredient('b', confirm)
//                .addIngredient('a', new ItemBuilder(BukkitCustomFishingPlugin.getInstance()
//                        .getItemManager()
//                        .getItemStackAppearance(player, material))
//                            .setCustomModelData(section.getInt("custom-model-data", 0))
//                            .setDisplayName(String.valueOf(section.getInt("custom-model-data", 0)))
//                )
                .build();

        var gui = PagedGui.items()
                .setStructure(
                        "x x x x x x x x x",
                        "x x x x x x x x x",
                        "x x x x x x x x x",
                        "# # # # c # # # #"
                )
                .addIngredient('x', new ItemStack(Material.AIR))
                .addIngredient('c', parentPage.getBackItem())
                .addIngredient('#', new BackGroundItem())
                .build();

        var window = AnvilWindow.split()
                .setViewer(player)
                .setTitle(new ShadedAdventureComponentWrapper(TranslationManager.render(MessageConstants.GUI_PAGE_MODEL_DATA_TITLE.build())))
                .addRenameHandler(s -> {
                    cmd = s;
                    confirm.notifyWindows();
                })
                .setUpperGui(upperGui)
                .setLowerGui(gui)
                .build();

        window.open();
    }

    public class ConfirmIcon extends AbstractItem {

        @Override
        public ItemProvider getItemProvider() {
            if (cmd == null || cmd.isEmpty()) {
                return new ItemBuilder(Material.STRUCTURE_VOID).setDisplayName(new ShadedAdventureComponentWrapper(TranslationManager.render(
                        MessageConstants.GUI_DELETE_PROPERTY.build()
                )));
            } else {
                try {
                    int value = Integer.parseInt(cmd);
                    if (value >= 0) {
                        return null;
//                        return new ItemBuilder(
//                                BukkitCustomFishingPlugin.getInstance()
//                                        .getItemManager()
//                                        .getItemStackAppearance(player, material)
//                                )
//                                .setCustomModelData(value)
//                                .setDisplayName(new ShadedAdventureComponentWrapper(TranslationManager.render(MessageConstants.GUI_NEW_VALUE.arguments(Component.text(value)).build())))
//                                .addLoreLines(new ShadedAdventureComponentWrapper(TranslationManager.render(MessageConstants.GUI_CLICK_CONFIRM.build())));
                    } else {
                        return new ItemBuilder(Material.BARRIER).setDisplayName(new ShadedAdventureComponentWrapper(TranslationManager.render(MessageConstants.GUI_INVALID_NUMBER.build())));
                    }
                } catch (NumberFormatException e) {
                    return new ItemBuilder(Material.BARRIER).setDisplayName(new ShadedAdventureComponentWrapper(TranslationManager.render(MessageConstants.GUI_INVALID_NUMBER.build())));
                }
            }
        }

        @Override
        public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
            if (cmd == null || cmd.isEmpty()) {
                section.set("custom-model-data", null);
            } else {
                try {
                    int value = Integer.parseInt(cmd);
                    if (value >= 0) {
                        section.set("custom-model-data", value);
                    } else {
                        return;
                    }
                } catch (NumberFormatException e) {
                    return;
                }
            }
            parentPage.reOpen();
            parentPage.save();
        }
    }
}
