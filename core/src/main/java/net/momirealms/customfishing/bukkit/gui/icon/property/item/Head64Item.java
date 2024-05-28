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

package net.momirealms.customfishing.bukkit.gui.icon.property.item;

import net.kyori.adventure.text.Component;
import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.bukkit.BukkitCustomFishingPluginImpl;
import net.momirealms.customfishing.bukkit.adventure.ShadedAdventureComponentWrapper;
import net.momirealms.customfishing.bukkit.gui.SectionPage;
import net.momirealms.customfishing.common.helper.AdventureHelper;
import net.momirealms.customfishing.common.locale.MessageConstants;
import net.momirealms.customfishing.common.locale.TranslationManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;

import java.util.ArrayList;

public class Head64Item extends AbstractItem {

    private final SectionPage itemPage;

    public Head64Item(SectionPage itemPage) {
        this.itemPage = itemPage;
    }

    @Override
    public ItemProvider getItemProvider() {
        ItemBuilder itemBuilder = new ItemBuilder(Material.PLAYER_HEAD)
                .setDisplayName(new ShadedAdventureComponentWrapper(TranslationManager.render(MessageConstants.GUI_ITEM_HEAD64.build())));
        if (itemPage.getSection().contains("head64")) {
            itemBuilder.addLoreLines(new ShadedAdventureComponentWrapper(TranslationManager.render(MessageConstants.GUI_CURRENT_VALUE.build())));
            String head64 = itemPage.getSection().getString("head64", "");
            ArrayList<String> list = new ArrayList<>();
            for (int i = 0; i < head64.length(); i += 16) {
                if (i + 16 > head64.length()) {
                    list.add(head64.substring(i));
                } else {
                    list.add(head64.substring(i, i + 16));
                }
            }
            for (String line : list) {
                itemBuilder.addLoreLines(new ShadedAdventureComponentWrapper(AdventureHelper.miniMessage(
                        "<white>" + line
                )));
            }
            itemBuilder
                    .addLoreLines("")
                    .addLoreLines(new ShadedAdventureComponentWrapper(TranslationManager.render(MessageConstants.GUI_LEFT_CLICK_EDIT.build())))
                    .addLoreLines(new ShadedAdventureComponentWrapper(TranslationManager.render(MessageConstants.GUI_RIGHT_CLICK_RESET.build())));
        } else {
            itemBuilder
                    .addLoreLines(new ShadedAdventureComponentWrapper(TranslationManager.render(MessageConstants.GUI_LEFT_CLICK_EDIT.build())));
        }
        return itemBuilder;
    }

    @Override
    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
        if (clickType.isLeftClick()) {
            player.closeInventory();
            BukkitCustomFishingPlugin.getInstance().getSenderFactory().wrap(player)
                            .sendMessage(Component.text("Input the head64 value in chat"));
            ((BukkitCustomFishingPluginImpl) BukkitCustomFishingPlugin.getInstance()).getChatCatcherManager().catchMessage(player, "head64", itemPage);
        } else if (clickType.isRightClick()) {
            itemPage.getSection().set("head64", null);
            itemPage.save();
            itemPage.reOpen();
        }
    }
}
