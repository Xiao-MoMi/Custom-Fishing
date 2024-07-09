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

package net.momirealms.customfishing.bukkit.hook;

import com.saicone.rtag.item.ItemTagStream;
import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.api.mechanic.context.Context;
import net.momirealms.customfishing.api.mechanic.hook.HookConfig;
import net.momirealms.customfishing.api.mechanic.hook.HookManager;
import net.momirealms.customfishing.common.item.Item;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Optional;

public class BukkitHookManager implements HookManager, Listener {

    private final BukkitCustomFishingPlugin plugin;
    private final HashMap<String, HookConfig> hooks = new HashMap<>();

    public BukkitHookManager(BukkitCustomFishingPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void unload() {
        HandlerList.unregisterAll(this);
    }

    @Override
    public void load() {
        Bukkit.getPluginManager().registerEvents(this, plugin.getBoostrap());
        plugin.debug("Loaded " + hooks.size() + " hooks");
    }

    @Override
    public boolean registerHook(HookConfig hook) {
        if (hooks.containsKey(hook.id())) return false;
        hooks.put(hook.id(), hook);
        return true;
    }

    @NotNull
    @Override
    public Optional<HookConfig> getHook(String id) {
        return Optional.ofNullable(hooks.get(id));
    }

    @EventHandler (ignoreCancelled = true)
    public void onDragDrop(InventoryClickEvent event) {
        final Player player = (Player) event.getWhoClicked();
        if (event.getClickedInventory() != player.getInventory())
            return;
        if (player.getGameMode() != GameMode.SURVIVAL)
            return;
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() != Material.FISHING_ROD)
            return;
        if (plugin.getFishingManager().getFishHook(player).isPresent())
            return;
        ItemStack cursor = event.getCursor();
        if (cursor.getType() == Material.AIR) {
            if (event.getClick() != ClickType.RIGHT) {
                return;
            }
            Item<ItemStack> wrapped = plugin.getItemManager().wrap(clicked);
            if (!wrapped.hasTag("CustomFishing", "hook_id")) {
                return;
            }
            event.setCancelled(true);
            String id = (String) wrapped.getTag("CustomFishing", "hook_id").orElseThrow();
            byte[] hookItemBase64 = (byte[]) wrapped.getTag("CustomFishing", "hook_id").orElse(null);
            int damage = (int) wrapped.getTag("CustomFishing", "hook_dur").orElse(0);
            ItemStack itemStack;
            if (hookItemBase64 != null) {
                itemStack = ItemTagStream.INSTANCE.fromBytes(hookItemBase64);
            } else {
                itemStack = plugin.getItemManager().buildInternal(Context.player(player), id);
            }
            plugin.getItemManager().setDurability(player, itemStack, damage);

            wrapped.removeTag("hook_id");
            wrapped.removeTag("hook_item");
            wrapped.removeTag("hook_dur");

            event.setCursor(itemStack);

            wrapped.load();
            return;
        }

        String hookID = plugin.getItemManager().getItemID(cursor);
        Optional<HookConfig> setting = getHook(hookID);
        if (setting.isEmpty()) {
            return;
        }


    }
}
