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
import net.momirealms.customfishing.adventure.AdventureManagerImpl;
import net.momirealms.customfishing.api.CustomFishingPlugin;
import net.momirealms.customfishing.api.data.user.OfflineUser;
import net.momirealms.customfishing.api.manager.BagManager;
import net.momirealms.customfishing.api.manager.EffectManager;
import net.momirealms.customfishing.api.mechanic.bag.FishingBagHolder;
import net.momirealms.customfishing.api.util.InventoryUtils;
import net.momirealms.customfishing.compatibility.papi.PlaceholderManagerImpl;
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
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
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
        plugin.getStorageManager().getDataSource().updateManyPlayersData(tempEditMap.values(), true);
    }

    /**
     * Retrieves the online bag inventory associated with a player's UUID.
     *
     * @param uuid The UUID of the player for whom the bag inventory is retrieved.
     * @return The online bag inventory if the player is online, or null if not found.
     */
    @Nullable
    @Override
    public Inventory getOnlineBagInventory(UUID uuid) {
        var onlinePlayer = plugin.getStorageManager().getOnlineUser(uuid);
        if (onlinePlayer == null) {
            return null;
        }
        Player player = onlinePlayer.getPlayer();
        int rows = getBagInventoryRows(player);
        Inventory bag = onlinePlayer.getHolder().getInventory();
        if (bag.getSize() != rows * 9) {
            Inventory newBag = InventoryUtils.createInventory(onlinePlayer.getHolder(), rows * 9,
                    AdventureManagerImpl.getInstance().getComponentFromMiniMessage(
                            PlaceholderManagerImpl.getInstance().parse(
                                    player, CFConfig.bagTitle, Map.of("{player}", player.getName())
                            )
                    ));
            onlinePlayer.getHolder().setInventory(newBag);
            assert newBag != null;
            ItemStack[] newContents = new ItemStack[rows * 9];
            ItemStack[] oldContents = bag.getContents();
            for (int i = 0; i < rows * 9 && i < oldContents.length; i++) {
                newContents[i] = oldContents[i];
            }
            newBag.setContents(newContents);
        }
        return onlinePlayer.getHolder().getInventory();
    }

    @Override
    public int getBagInventoryRows(Player player) {
        int size = 1;
        for (int i = 6; i > 1; i--) {
            if (player.hasPermission("fishingbag.rows." + i)) {
                size = i;
                break;
            }
        }
        return size;
    }

    /**
     * Initiates the process of editing the bag inventory of an offline player by an admin.
     *
     * @param admin    The admin player performing the edit.
     * @param userData The OfflineUser data of the player whose bag is being edited.
     */
    @Override
    public void editOfflinePlayerBag(Player admin, OfflineUser userData) {
        this.tempEditMap.put(admin.getUniqueId(), userData);
        admin.openInventory(userData.getHolder().getInventory());
    }

    /**
     * Handles the InventoryCloseEvent to save changes made to an offline player's bag inventory when it's closed.
     *
     * @param event The InventoryCloseEvent triggered when the inventory is closed.
     */
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

    /**
     * Handles InventoryClickEvent to prevent certain actions on the Fishing Bag inventory.
     * This method cancels the event if specific conditions are met to restrict certain item interactions.
     *
     * @param event The InventoryClickEvent triggered when an item is clicked in an inventory.
     */
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
        String id = plugin.getItemManager().getAnyPluginItemID(clickedItem);
        EffectManager effectManager = plugin.getEffectManager();
        if (effectManager.hasEffectCarrier("rod", id)
                || effectManager.hasEffectCarrier("bait", id)
                || effectManager.hasEffectCarrier("util", id)
                || effectManager.hasEffectCarrier("hook", id)
        ) {
            return;
        }
        if (CFConfig.bagStoreLoots && plugin.getLootManager().getLoot(id) != null)
            return;
        event.setCancelled(true);
    }

    /**
     * Event handler for the PlayerQuitEvent.
     * This method is triggered when a player quits the server.
     * It checks if the player was in the process of editing an offline player's bag inventory,
     * and if so, saves the offline player's data if necessary.
     *
     * @param event The PlayerQuitEvent triggered when a player quits.
     */
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        OfflineUser offlineUser = tempEditMap.remove(event.getPlayer().getUniqueId());
        if (offlineUser == null)
            return;
        plugin.getStorageManager().saveUserData(offlineUser, true);
    }
}
