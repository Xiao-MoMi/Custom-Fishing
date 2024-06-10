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

package net.momirealms.customfishing.bukkit.gui.icon.property.loot;

import net.kyori.adventure.text.Component;
import net.momirealms.customfishing.bukkit.adventure.ShadedAdventureComponentWrapper;
import net.momirealms.customfishing.bukkit.gui.SectionPage;
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

public class DisableGameItem extends AbstractItem {

    private final SectionPage itemPage;

    public DisableGameItem(SectionPage itemPage) {
        this.itemPage = itemPage;
    }

    @Override
    public ItemProvider getItemProvider() {
        return new ItemBuilder(Material.LEAD)
                .setDisplayName(new ShadedAdventureComponentWrapper(TranslationManager.render(MessageConstants.GUI_LOOT_DISABLE_GAME.build())))
                .addLoreLines(new ShadedAdventureComponentWrapper(TranslationManager.render(
                        MessageConstants.GUI_CURRENT_VALUE.arguments(
                                Component.text(itemPage.getSection().getBoolean("disable-game", false))
                        ).build()
                )))
                .addLoreLines("")
                .addLoreLines(new ShadedAdventureComponentWrapper(TranslationManager.render(MessageConstants.GUI_CLICK_TO_TOGGLE.build())));
    }

    @Override
    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
        itemPage.getSection().set("disable-game", !itemPage.getSection().getBoolean("disable-game", false));
        itemPage.save();
        itemPage.reOpen();
    }
}
