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

package net.momirealms.customfishing.mechanic.bag;

import net.momirealms.customfishing.CustomFishingPluginImpl;
import net.momirealms.customfishing.api.CustomFishingPlugin;
import net.momirealms.customfishing.api.data.user.OfflineUser;
import net.momirealms.customfishing.api.manager.BagManager;
import net.momirealms.customfishing.api.mechanic.bag.FishingBagHolder;
import net.momirealms.customfishing.setting.CFConfig;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.UUID;

public class BagManagerImpl implements BagManager, Listener {

    private final CustomFishingPlugin plugin;
    private final HashMap<UUID, OfflineUser> tempEditMap;

    public BagManagerImpl(CustomFishingPluginImpl plugin) {
        this.plugin = plugin;
        this.tempEditMap = new HashMap<>();
    }

    @Override
    public boolean isEnabled() {
        return CFConfig.enableFishingBag;
    }

    public void load() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void unload() {
        HandlerList.unregisterAll(this);
    }

    public void disable() {
        unload();
        plugin.getStorageManager().getDataSource().savePlayersData(tempEditMap.values(), true);
    }

    @Override
    public Inventory getOnlineBagInventory(UUID uuid) {
        var onlinePlayer = plugin.getStorageManager().getOnlineUser(uuid);
        if (onlinePlayer == null) {
            return null;
        }
        return onlinePlayer.getHolder().getInventory();
    }

    @Override
    public void editOfflinePlayerBag(Player admin, OfflineUser userData) {
        this.tempEditMap.put(admin.getUniqueId(), userData);
        admin.openInventory(userData.getHolder().getInventory());
    }

    @EventHandler
    public void onInvClose(InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder() instanceof FishingBagHolder))
            return;
        final Player viewer = (Player) event.getPlayer();
        OfflineUser offlineUser = tempEditMap.remove(viewer.getUniqueId());
        if (offlineUser == null)
            return;
        plugin.getStorageManager().saveUserData(offlineUser, true);
    }

    @EventHandler
    public void onInvClick(InventoryClickEvent event) {
        if (event.isCancelled())
            return;
        if (!(event.getInventory().getHolder() instanceof FishingBagHolder))
            return;
        Inventory clicked = event.getClickedInventory();
        if (clicked != event.getWhoClicked().getInventory())
            return;
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR)
            return;
        if (CFConfig.bagWhiteListItems.contains(clickedItem.getType()))
            return;
        String id = plugin.getItemManager().getAnyItemID(clickedItem);
        if (plugin.getEffectManager().getEffect("rod", id) != null)
            return;
        if (plugin.getEffectManager().getEffect("bait", id) != null)
            return;
        if (plugin.getEffectManager().getEffect("util", id) != null)
            return;
        if (CFConfig.bagStoreLoots && plugin.getLootManager().getLoot(id) != null)
            return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        OfflineUser offlineUser = tempEditMap.remove(event.getPlayer().getUniqueId());
        if (offlineUser == null)
            return;
        plugin.getStorageManager().saveUserData(offlineUser, true);
    }
}
