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

package net.momirealms.customfishing.bukkit.bag;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.api.mechanic.action.Action;
import net.momirealms.customfishing.api.mechanic.bag.BagManager;
import net.momirealms.customfishing.api.mechanic.bag.FishingBagHolder;
import net.momirealms.customfishing.api.mechanic.item.MechanicType;
import net.momirealms.customfishing.api.mechanic.requirement.Requirement;
import net.momirealms.customfishing.api.storage.user.UserData;
import net.momirealms.customfishing.bukkit.config.BukkitConfigManager;
import net.momirealms.customfishing.common.helper.AdventureHelper;
import net.momirealms.sparrow.heart.SparrowHeart;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class BukkitBagManager implements BagManager, Listener {

    private final BukkitCustomFishingPlugin plugin;
    private final HashMap<UUID, UserData> tempEditMap;
    private Action<Player>[] collectLootActions;
    private Action<Player>[] bagFullActions;
    private boolean bagStoreLoots;
    private String bagTitle;
    private List<Material> bagWhiteListItems = new ArrayList<>();
    private Requirement<Player>[] collectRequirements;

    public BukkitBagManager(BukkitCustomFishingPlugin plugin) {
        this.plugin = plugin;
        this.tempEditMap = new HashMap<>();
    }

    @Override
    public void load() {
        this.loadConfig();
        Bukkit.getPluginManager().registerEvents(this, plugin.getBoostrap());
    }

    @Override
    public void unload() {
        HandlerList.unregisterAll(this);
    }

    @Override
    public void disable() {
        unload();
        this.plugin.getStorageManager().getDataSource().updateManyPlayersData(tempEditMap.values(), true);
    }

    private void loadConfig() {
        Section config = BukkitConfigManager.getMainConfig().getSection("mechanics.fishing-bag");

        bagTitle = config.getString("bag-title", "");
        bagStoreLoots = config.getBoolean("can-store-loot", false);
        bagWhiteListItems = config.getStringList("whitelist-items").stream().map(it -> Material.valueOf(it.toUpperCase(Locale.ENGLISH))).toList();
        collectLootActions = plugin.getActionManager().parseActions(config.getSection("collect-actions"));
        bagFullActions = plugin.getActionManager().parseActions(config.getSection("full-actions"));
        collectRequirements = plugin.getRequirementManager().parseRequirements(config.getSection("collect-requirements"), false);
    }

    @Override
    public CompletableFuture<Boolean> openBag(Player viewer, UUID owner) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        Optional<UserData> onlineUser = plugin.getStorageManager().getOnlineUser(owner);
        onlineUser.ifPresentOrElse(data -> {
            viewer.openInventory(data.holder().getInventory());
            SparrowHeart.getInstance().updateInventoryTitle(viewer, AdventureHelper.componentToJson(AdventureHelper.miniMessage(plugin.getPlaceholderManager().parse(Bukkit.getOfflinePlayer(owner), bagTitle, Map.of("{uuid}", owner.toString(), "{player}", data.name())))));
            future.complete(true);
        }, () -> plugin.getStorageManager().getOfflineUserData(owner, true).thenAccept(result -> result.ifPresentOrElse(data -> {
            if (data.isLocked()) {
                future.completeExceptionally(new RuntimeException("Data is locked"));
                return;
            }
            this.tempEditMap.put(viewer.getUniqueId(), data);
            viewer.openInventory(data.holder().getInventory());
            SparrowHeart.getInstance().updateInventoryTitle(viewer, AdventureHelper.componentToJson(AdventureHelper.miniMessage(plugin.getPlaceholderManager().parse(Bukkit.getOfflinePlayer(owner), bagTitle, Map.of("{uuid}", owner.toString(), "{player}", data.name())))));
            future.complete(true);
        }, () -> future.complete(false))));
        return future;
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
        UserData userData = tempEditMap.remove(viewer.getUniqueId());
        if (userData == null)
            return;
        this.plugin.getStorageManager().saveUserData(userData, true);
    }

    /**
     * Handles InventoryClickEvent to prevent certain actions on the Fishing Bag inventory.
     * This method cancels the event if specific conditions are met to restrict certain item interactions.
     *
     * @param event The InventoryClickEvent triggered when an item is clicked in an inventory.
     */
    @EventHandler (ignoreCancelled = true)
    public void onInvClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof FishingBagHolder))
            return;

        ItemStack movedItem = event.getCurrentItem();
        Inventory clicked = event.getClickedInventory();

        if (clicked != event.getWhoClicked().getInventory()) {
            if (event.getAction() != InventoryAction.HOTBAR_SWAP && event.getAction() != InventoryAction.HOTBAR_MOVE_AND_READD) {
                return;
            }
            movedItem = event.getWhoClicked().getInventory().getItem(event.getHotbarButton());
        }

        if (movedItem == null || movedItem.getType() == Material.AIR || bagWhiteListItems.contains(movedItem.getType()))
            return;

        String id = plugin.getItemManager().getItemID(movedItem);
        MechanicType type = MechanicType.getTypeByID(id);
        if (type == null) {
            event.setCancelled(true);
            return;
        }

        if (type == MechanicType.LOOT && bagStoreLoots)
            return;

        if (type == MechanicType.BAIT || type == MechanicType.ROD || type == MechanicType.UTIL || type == MechanicType.HOOK)
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
        UserData userData = tempEditMap.remove(event.getPlayer().getUniqueId());
        if (userData == null)
            return;
        plugin.getStorageManager().saveUserData(userData, true);
    }
}
