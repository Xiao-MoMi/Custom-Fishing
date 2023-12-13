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

package net.momirealms.customfishing.gui.icon.property.item;

import net.momirealms.customfishing.adventure.AdventureManagerImpl;
import net.momirealms.customfishing.adventure.component.ShadedAdventureComponentWrapper;
import net.momirealms.customfishing.gui.SectionPage;
import net.momirealms.customfishing.gui.page.property.EnchantmentEditor;
import net.momirealms.customfishing.setting.CFLocale;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;

import java.util.Map;

public class StoredEnchantmentItem extends AbstractItem {

    private final SectionPage itemPage;

    public StoredEnchantmentItem(SectionPage itemPage) {
        this.itemPage = itemPage;
    }

    @Override
    public ItemProvider getItemProvider() {
        ItemBuilder itemBuilder = new ItemBuilder(Material.ENCHANTED_BOOK)
                .setDisplayName(new ShadedAdventureComponentWrapper(AdventureManagerImpl.getInstance().getComponentFromMiniMessage(
                        CFLocale.GUI_ITEM_STORED_ENCHANTMENT
                )))
                .addEnchantment(Enchantment.ARROW_FIRE,1,true)
                .addItemFlags(ItemFlag.HIDE_ENCHANTS);
        if (itemPage.getSection().contains("stored-enchantments")) {
            itemBuilder.addLoreLines(new ShadedAdventureComponentWrapper(AdventureManagerImpl.getInstance().getComponentFromMiniMessage(
                            CFLocale.GUI_CURRENT_VALUE
                    )));
            for (Map.Entry<String, Object> entry : itemPage.getSection().getConfigurationSection("stored-enchantments").getValues(false).entrySet()) {
                itemBuilder.addLoreLines(new ShadedAdventureComponentWrapper(AdventureManagerImpl.getInstance().getComponentFromMiniMessage(
                        " <gray>- <white>" + entry.getKey() + ":" + entry.getValue()
                )));
            }
            itemBuilder.addLoreLines("").addLoreLines(new ShadedAdventureComponentWrapper(AdventureManagerImpl.getInstance().getComponentFromMiniMessage(
                    CFLocale.GUI_LEFT_CLICK_EDIT
            ))).addLoreLines(new ShadedAdventureComponentWrapper(AdventureManagerImpl.getInstance().getComponentFromMiniMessage(
                    CFLocale.GUI_RIGHT_CLICK_RESET
            )));
        } else {
            itemBuilder.addLoreLines(new ShadedAdventureComponentWrapper(AdventureManagerImpl.getInstance().getComponentFromMiniMessage(
                    CFLocale.GUI_LEFT_CLICK_EDIT
            )));
        }
        return itemBuilder;
    }

    @Override
    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
        if (clickType.isLeftClick()) {
            new EnchantmentEditor(player, itemPage, true);
        } else if (clickType.isRightClick()) {
            itemPage.getSection().set("stored-enchantments", null);
            itemPage.save();
            itemPage.reOpen();
        }
    }
}
