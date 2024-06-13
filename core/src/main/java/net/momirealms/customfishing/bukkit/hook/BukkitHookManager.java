package net.momirealms.customfishing.bukkit.hook;

import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.api.mechanic.hook.HookConfig;
import net.momirealms.customfishing.api.mechanic.hook.HookManager;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
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
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() != Material.FISHING_ROD)
            return;
        if (player.getGameMode() != GameMode.SURVIVAL)
            return;


    }
}
