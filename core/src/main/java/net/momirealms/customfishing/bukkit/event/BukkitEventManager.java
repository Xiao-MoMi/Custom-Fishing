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

package net.momirealms.customfishing.bukkit.event;

import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.api.mechanic.MechanicType;
import net.momirealms.customfishing.api.mechanic.action.ActionTrigger;
import net.momirealms.customfishing.api.mechanic.context.Context;
import net.momirealms.customfishing.api.mechanic.context.ContextKeys;
import net.momirealms.customfishing.api.mechanic.event.EventCarrier;
import net.momirealms.customfishing.api.mechanic.event.EventManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class BukkitEventManager implements EventManager, Listener {

    private final HashMap<String, EventCarrier> carriers = new HashMap<>();
    private final BukkitCustomFishingPlugin plugin;

    public BukkitEventManager(BukkitCustomFishingPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void unload() {
        this.carriers.clear();
        HandlerList.unregisterAll(this);
    }

    @Override
    public void load() {
        Bukkit.getPluginManager().registerEvents(this, this.plugin.getBoostrap());
    }

    @Override
    public Optional<EventCarrier> getEventCarrier(String id, MechanicType type) {
        return Optional.ofNullable(this.carriers.get(type.getType() + ":" + id));
    }

    @Override
    public boolean registerEventCarrier(EventCarrier carrier) {
        if (this.carriers.containsKey(carrier.id())) return false;
        this.carriers.put(carrier.type().getType() + ":" + carrier.id(), carrier);
        return true;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND)
            return;
        if (event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_AIR && event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK)
            return;
        ItemStack itemStack = event.getPlayer().getInventory().getItemInMainHand();
        if (itemStack.getType() == Material.AIR || itemStack.getAmount() == 0)
            return;
        String id = this.plugin.getItemManager().getItemID(itemStack);
        Context<Player> context = Context.player(event.getPlayer());
        Block clicked = event.getClickedBlock();
        context.arg(ContextKeys.OTHER_LOCATION, clicked == null ? event.getPlayer().getLocation() : clicked.getLocation());
        List<MechanicType> mechanics = MechanicType.getTypeByID(id);
        if (mechanics != null) {
            for (MechanicType type : mechanics) {
                if (type == MechanicType.ROD) continue;
                trigger(context, id, type, ActionTrigger.INTERACT);
            }
        }
    }

    @EventHandler (ignoreCancelled = true)
    public void onConsumeItem(PlayerItemConsumeEvent event) {
        Context<Player> context = Context.player(event.getPlayer());
        trigger(context, plugin.getItemManager().getItemID(event.getItem()), MechanicType.LOOT, ActionTrigger.CONSUME);
    }
}
