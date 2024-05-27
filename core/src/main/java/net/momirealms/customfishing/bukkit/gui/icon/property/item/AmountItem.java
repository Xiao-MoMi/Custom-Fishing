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
import net.momirealms.customfishing.bukkit.adventure.ShadedAdventureComponentWrapper;
import net.momirealms.customfishing.bukkit.gui.SectionPage;
import net.momirealms.customfishing.bukkit.gui.page.property.AmountEditor;
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

public class AmountItem extends AbstractItem {

    private final SectionPage itemPage;

    public AmountItem(SectionPage itemPage) {
        this.itemPage = itemPage;
    }

    @Override
    public ItemProvider getItemProvider() {
        ItemBuilder itemBuilder = new ItemBuilder(Material.IRON_NUGGET)
                .setDisplayName(new ShadedAdventureComponentWrapper(TranslationManager.render(MessageConstants.GUI_ITEM_AMOUNT.build())))
                .setAmount(itemPage.getSection().getInt("amount", 1));
        if (itemPage.getSection().contains("amount")) {
            itemBuilder.addLoreLines(new ShadedAdventureComponentWrapper(TranslationManager.render(
                            MessageConstants.GUI_CURRENT_VALUE.arguments(
                                    Component.text(itemPage.getSection().getInt("amount"))
                            ).build()
                    )))
                    .addLoreLines("");
            itemBuilder.addLoreLines(new ShadedAdventureComponentWrapper(TranslationManager.render(MessageConstants.GUI_LEFT_CLICK_EDIT.build())))
                    .addLoreLines(new ShadedAdventureComponentWrapper(TranslationManager.render(MessageConstants.GUI_RIGHT_CLICK_RESET.build())));
        } else {
            itemBuilder.addLoreLines(new ShadedAdventureComponentWrapper(TranslationManager.render(MessageConstants.GUI_LEFT_CLICK_EDIT.build())));
        }
        return itemBuilder;
    }

    @Override
    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
        if (clickType.isLeftClick()) {
            new AmountEditor(player, itemPage);
        } else if (clickType.isRightClick()) {
            itemPage.getSection().set("amount", null);
            itemPage.save();
            itemPage.reOpen();
        }
    }
}
