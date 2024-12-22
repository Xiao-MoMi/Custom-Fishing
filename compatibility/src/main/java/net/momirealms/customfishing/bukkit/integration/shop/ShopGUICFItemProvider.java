/*
 *  Copyright (C) <2024> <XiaoMoMi>
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

package net.momirealms.customfishing.bukkit.integration.shop;

import net.brcdev.shopgui.ShopGuiPlusApi;
import net.brcdev.shopgui.event.ShopGUIPlusPostEnableEvent;
import net.brcdev.shopgui.provider.item.ItemProvider;
import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.api.mechanic.context.Context;
import net.momirealms.customfishing.api.mechanic.context.ContextKeys;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class ShopGUICFItemProvider extends ItemProvider implements Listener {

    private final BukkitCustomFishingPlugin plugin;

    public ShopGUICFItemProvider(BukkitCustomFishingPlugin plugin) {
        super("CustomFishing");
        this.plugin = plugin;
    }

    @Override
    public boolean isValidItem(ItemStack itemStack) {
        return plugin.getItemManager().getCustomFishingItemID(itemStack) != null;
    }

    @Override
    public ItemStack loadItem(ConfigurationSection configurationSection) {
        String id = configurationSection.getString("customFishing");
        if (id == null) return null;
        return plugin.getItemManager().buildInternal(Context.player(null).arg(ContextKeys.ID, id), id);
    }

    @Override
    public boolean compare(ItemStack i1, ItemStack i2) {
        return Objects.equals(plugin.getItemManager().getCustomFishingItemID(i1), plugin.getItemManager().getCustomFishingItemID(i2));
    }

    @EventHandler
    public void onShopGUIPlusPostEnable(ShopGUIPlusPostEnableEvent event) {
        ShopGuiPlusApi.registerItemProvider(this);
    }
}
