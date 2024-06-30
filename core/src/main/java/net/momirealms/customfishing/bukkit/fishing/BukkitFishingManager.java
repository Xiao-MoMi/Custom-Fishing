package net.momirealms.customfishing.bukkit.fishing;

import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.api.event.RodCastEvent;
import net.momirealms.customfishing.api.mechanic.config.ConfigManager;
import net.momirealms.customfishing.api.mechanic.context.Context;
import net.momirealms.customfishing.api.mechanic.fishing.CustomFishingHook;
import net.momirealms.customfishing.api.mechanic.fishing.FishingGears;
import net.momirealms.customfishing.api.mechanic.fishing.FishingManager;
import net.momirealms.customfishing.api.mechanic.fishing.hook.VanillaMechanic;
import net.momirealms.customfishing.api.mechanic.requirement.RequirementManager;
import net.momirealms.customfishing.bukkit.util.EventUtils;
import net.momirealms.customfishing.common.helper.VersionHelper;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BukkitFishingManager implements FishingManager, Listener {

    private final BukkitCustomFishingPlugin plugin;
    private final ConcurrentHashMap<UUID, CustomFishingHook> castHooks = new ConcurrentHashMap<>();

    public BukkitFishingManager(BukkitCustomFishingPlugin plugin) {
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
    public Optional<CustomFishingHook> getFishHook(Player player) {
        return getFishHook(player.getUniqueId());
    }

    @Override
    public Optional<CustomFishingHook> getFishHook(UUID player) {
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

    private void onCaughtEntity(PlayerFishEvent event) {
        final Player player = event.getPlayer();
        getFishHook(player).ifPresent(hook -> {
            Entity entity = event.getCaught();
            if (entity != null && entity.getPersistentDataContainer().get(
                    Objects.requireNonNull(NamespacedKey.fromString("temp-entity", plugin.getBoostrap())),
                    PersistentDataType.STRING
            ) != null) {

            }
        });
    }

    private void onReelIn(PlayerFishEvent event) {
        Player player = event.getPlayer();
        getFishHook(player).ifPresent(hook -> {
            hook.onReelIn();
        });
    }

    private void onBite(PlayerFishEvent event) {
        Player player = event.getPlayer();
        getFishHook(player).ifPresent(hook -> {
            hook.onBite();
        });
    }

    private void onCaughtFish(PlayerFishEvent event) {
        Player player = event.getPlayer();
        getFishHook(player).ifPresent(hook -> {
            hook.onReelIn();
        });
    }

    private void onCastRod(PlayerFishEvent event) {
        FishHook hook = event.getHook();
        Player player = event.getPlayer();
        Context<Player> context = Context.player(player);
        FishingGears gears = new FishingGears(context);

        if (!RequirementManager.isSatisfied(context, ConfigManager.mechanicRequirements())) {
            this.destroy(player.getUniqueId());
            return;
        }

        if (!gears.canFish()) {
            event.setCancelled(true);
            return;
        }

        if (EventUtils.fireAndCheckCancel(new RodCastEvent(event, gears))) {
            return;
        }

        plugin.debug(context.toString());
        gears.cast();
        CustomFishingHook customHook = new CustomFishingHook(hook, gears, context);
        this.castHooks.put(player.getUniqueId(), customHook);
    }

    private void onInGround(PlayerFishEvent event) {
        if (VersionHelper.isVersionNewerThan1_20_5()) return;
        final Player player = event.getPlayer();
        if (player.getGameMode() != GameMode.CREATIVE) {
            ItemStack itemStack = player.getInventory().getItemInMainHand();
            if (itemStack.getType() != Material.FISHING_ROD) itemStack = player.getInventory().getItemInOffHand();
            if (itemStack.getType() == Material.FISHING_ROD) {
                plugin.getItemManager().decreaseDurability(itemStack, 5, true);
            }
        }
    }

    @Override
    public void destroy(UUID uuid) {
        this.getFishHook(uuid).ifPresent(hook -> {
            hook.destroy();
            this.castHooks.remove(uuid);
        });
    }
}
