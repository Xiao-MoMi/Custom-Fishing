package net.momirealms.customfishing.bukkit.fishing;

import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.api.event.RodCastEvent;
import net.momirealms.customfishing.api.mechanic.config.ConfigManager;
import net.momirealms.customfishing.api.mechanic.context.Context;
import net.momirealms.customfishing.api.mechanic.fishing.FishingGears;
import net.momirealms.customfishing.api.mechanic.fishing.FishingHookTimerTask;
import net.momirealms.customfishing.api.mechanic.fishing.FishingManager;
import net.momirealms.customfishing.api.mechanic.requirement.RequirementManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BukkitFishingManager implements FishingManager, Listener {

    private BukkitCustomFishingPlugin plugin;
    private final ConcurrentHashMap<UUID, FishHook> castHooks = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, FishingGears> gears = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, FishingHookTimerTask> tasks = new ConcurrentHashMap<>();

    public BukkitFishingManager(BukkitCustomFishingPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public Optional<FishHook> getFishHook(Player player) {
        return Optional.ofNullable(castHooks.get(player.getUniqueId()));
    }

    @Override
    public Optional<FishHook> getFishHook(UUID player) {
        return Optional.ofNullable(castHooks.get(player));
    }

    @Override
    public Optional<Player> getOwner(FishHook hook) {
        if (hook.getOwnerUniqueId() != null) {
            return Optional.ofNullable(Bukkit.getPlayer(hook.getOwnerUniqueId()));
        }
        return Optional.empty();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFishMONITOR(PlayerFishEvent event) {
        if (ConfigManager.eventPriority() != EventPriority.MONITOR) return;
        this.selectState(event);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFishHIGHEST(PlayerFishEvent event) {
        if (ConfigManager.eventPriority() != EventPriority.HIGHEST) return;
        this.selectState(event);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onFishHIGH(PlayerFishEvent event) {
        if (ConfigManager.eventPriority() != EventPriority.HIGH) return;
        this.selectState(event);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onFishNORMAL(PlayerFishEvent event) {
        if (ConfigManager.eventPriority() != EventPriority.NORMAL) return;
        this.selectState(event);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onFishLOW(PlayerFishEvent event) {
        if (ConfigManager.eventPriority() != EventPriority.LOW) return;
        this.selectState(event);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onFishLOWEST(PlayerFishEvent event) {
        if (ConfigManager.eventPriority() != EventPriority.LOWEST) return;
        this.selectState(event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemHeldChange(PlayerItemHeldEvent event) {
        if (getFishHook(event.getPlayer()).isPresent()) {
            this.destroy(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onSwapItem(PlayerSwapHandItemsEvent event) {
        if (getFishHook(event.getPlayer()).isPresent()) {
            this.destroy(event.getPlayer().getUniqueId());
        }
    }

    private void selectState(PlayerFishEvent event) {
        switch (event.getState()) {
            case FISHING -> onCastRod(event);
            case REEL_IN -> onReelIn(event);
            case CAUGHT_ENTITY -> onCaughtEntity(event);
            case CAUGHT_FISH -> onCaughtFish(event);
            case BITE -> onBite(event);
            case IN_GROUND -> onInGround(event);
        }
    }

    private void onCastRod(PlayerFishEvent event) {
        FishHook hook = event.getHook();
        Player player = event.getPlayer();
        Context<Player> context = Context.player(player);
        FishingGears gears = new FishingGears(context);
        this.gears.put(player.getUniqueId(), gears);

        if (!RequirementManager.isSatisfied(context, ConfigManager.mechanicRequirements())) {
            this.destroy(player.getUniqueId());
            return;
        }

        RodCastEvent rodCastEvent = new RodCastEvent(event, gears);
        Bukkit.getPluginManager().callEvent(rodCastEvent);
        if (rodCastEvent.isCancelled()) {
            return;
        }

        gears.cast();
        this.castHooks.put(player.getUniqueId(), hook);
    }

    @Override
    public void destroy(UUID uuid) {
        this.getFishHook(uuid).ifPresent(hook -> {
            hook.remove();
            this.castHooks.remove(uuid);
        });
        this.gears.remove(uuid);
        FishingHookTimerTask task = this.tasks.remove(uuid);
        if (task != null) {
            task.cancel();
        }
    }
}
