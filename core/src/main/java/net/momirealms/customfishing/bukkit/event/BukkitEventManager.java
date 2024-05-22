package net.momirealms.customfishing.bukkit.event;

import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.api.mechanic.action.ActionTrigger;
import net.momirealms.customfishing.api.mechanic.context.Context;
import net.momirealms.customfishing.api.mechanic.event.EventCarrier;
import net.momirealms.customfishing.api.mechanic.event.EventManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
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
        Bukkit.getPluginManager().registerEvents(this, plugin.getBoostrap());
    }

    @Override
    public Optional<EventCarrier> getEventCarrier(String id) {
        return Optional.ofNullable(carriers.get(id));
    }

    @Override
    public boolean registerEventCarrier(EventCarrier carrier) {
        if (carriers.containsKey(carrier.id())) return false;
        carriers.put(carrier.id(), carrier);
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
        String id = plugin.getItemManager().getItemID(itemStack);
        Optional.ofNullable(carriers.get(id)).ifPresent(carrier -> {
            carrier.trigger(Context.player(event.getPlayer()), ActionTrigger.INTERACT);
        });
    }

    @EventHandler (ignoreCancelled = true)
    public void onConsumeItem(PlayerItemConsumeEvent event) {
        Optional.ofNullable(carriers.get(plugin.getItemManager().getItemID(event.getItem())))
                .ifPresent(carrier -> carrier.trigger(Context.player(event.getPlayer()), ActionTrigger.CONSUME));
    }
}
