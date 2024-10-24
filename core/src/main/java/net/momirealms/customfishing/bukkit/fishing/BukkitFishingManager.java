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

package net.momirealms.customfishing.bukkit.fishing;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.api.event.FishingHookStateEvent;
import net.momirealms.customfishing.api.event.RodCastEvent;
import net.momirealms.customfishing.api.mechanic.config.ConfigManager;
import net.momirealms.customfishing.api.mechanic.context.Context;
import net.momirealms.customfishing.api.mechanic.fishing.CustomFishingHook;
import net.momirealms.customfishing.api.mechanic.fishing.FishingGears;
import net.momirealms.customfishing.api.mechanic.fishing.FishingManager;
import net.momirealms.customfishing.api.mechanic.fishing.hook.VanillaMechanic;
import net.momirealms.customfishing.api.mechanic.game.AbstractGamingPlayer;
import net.momirealms.customfishing.api.mechanic.game.GamingPlayer;
import net.momirealms.customfishing.api.mechanic.requirement.RequirementManager;
import net.momirealms.customfishing.api.util.EventUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
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
        Bukkit.getPluginManager().registerEvents(this, plugin.getBootstrap());
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
            this.destroyHook(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onSwapItem(PlayerSwapHandItemsEvent event) {
        getFishHook(event.getPlayer()).ifPresent(hook -> {
            Optional<GamingPlayer> optionalGamingPlayer = hook.getGamingPlayer();
            if (optionalGamingPlayer.isPresent()) {
                optionalGamingPlayer.get().handleSwapHand();
                event.setCancelled(true);
            } else {
                this.destroyHook(event.getPlayer().getUniqueId());
            }
        });
    }

    @EventHandler(ignoreCancelled = true)
    public void onJump(PlayerJumpEvent event) {
        final Player player = event.getPlayer();
        getFishHook(player).flatMap(CustomFishingHook::getGamingPlayer).ifPresent(gamingPlayer -> {
            if (gamingPlayer.handleJump()) {
                event.setCancelled(true);
            }
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        this.destroyHook(event.getPlayer().getUniqueId());
    }

    @EventHandler (ignoreCancelled = false)
    public void onLeftClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.LEFT_CLICK_AIR)
            return;
        if (event.getMaterial() != Material.FISHING_ROD)
            return;
        if (event.getHand() != EquipmentSlot.HAND)
            return;
        getFishHook(event.getPlayer()).ifPresent(hook -> {
            Optional<GamingPlayer> optionalGamingPlayer = hook.getGamingPlayer();
            if (optionalGamingPlayer.isPresent()) {
                if (((AbstractGamingPlayer) optionalGamingPlayer.get()).internalLeftClick()) {
                    event.setCancelled(true);
                }
            }
        });
    }

    @EventHandler (ignoreCancelled = true)
    public void onSneak(PlayerToggleSneakEvent event) {
        if (!event.isSneaking()) return;
        getFishHook(event.getPlayer()).ifPresent(hook -> {
            Optional<GamingPlayer> optionalGamingPlayer = hook.getGamingPlayer();
            if (optionalGamingPlayer.isPresent()) {
                if (optionalGamingPlayer.get().handleSneak()) {
                    event.setCancelled(true);
                }
            }
        });
    }

    // It's not necessary to get component from the event
    @SuppressWarnings("deprecation")
    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        if (event.isCancelled()) return;
        getFishHook(event.getPlayer()).ifPresent(hook -> {
            Optional<GamingPlayer> optionalGamingPlayer = hook.getGamingPlayer();
            if (optionalGamingPlayer.isPresent()) {
                if (optionalGamingPlayer.get().handleChat(event.getMessage())) {
                    event.setCancelled(true);
                }
            }
        });
    }

    private void selectState(PlayerFishEvent event) {
        switch (event.getState()) {
            case FISHING -> onCastRod(event);
            case REEL_IN, CAUGHT_FISH -> onReelIn(event);
            case CAUGHT_ENTITY -> onCaughtEntity(event);
            //case CAUGHT_FISH -> onCaughtFish(event);
            case BITE -> onBite(event);
            case IN_GROUND -> onInGround(event);
            case FAILED_ATTEMPT -> onFailedAttempt(event);
            // case LURED 1.20.5+
        }
    }

    // for vanilla mechanics
    private void onFailedAttempt(PlayerFishEvent event) {
        Player player = event.getPlayer();
        getFishHook(player).ifPresent(hook -> {
            if (hook.getCurrentHookMechanic() instanceof VanillaMechanic vanillaMechanic) {
                vanillaMechanic.onFailedAttempt();
            }
        });
    }

    // for vanilla mechanics
    private void onBite(PlayerFishEvent event) {
        Player player = event.getPlayer();
        getFishHook(player).ifPresent(hook -> {
            if (hook.getCurrentHookMechanic() instanceof VanillaMechanic vanillaMechanic) {
                vanillaMechanic.onBite();
            }
        });
    }

    private void onCaughtEntity(PlayerFishEvent event) {
        final Player player = event.getPlayer();
        Optional<CustomFishingHook> hook = getFishHook(player);
        if (hook.isPresent()) {
            Entity entity = event.getCaught();
            if (entity != null && entity.getPersistentDataContainer().get(
                    Objects.requireNonNull(NamespacedKey.fromString("temp-entity", plugin.getBootstrap())),
                    PersistentDataType.STRING
            ) != null) {
                event.setCancelled(true);
                Optional<GamingPlayer> gamingPlayer = hook.get().getGamingPlayer();
                if (gamingPlayer.isPresent()) {
                    ((AbstractGamingPlayer) gamingPlayer.get()).internalRightClick();
                    return;
                }
                hook.get().onReelIn();
                return;
            }

            if (player.getGameMode() != GameMode.CREATIVE) {
                ItemStack itemStack = player.getInventory().getItemInMainHand();
                if (itemStack.getType() != Material.FISHING_ROD) itemStack = player.getInventory().getItemInOffHand();
                if (plugin.getItemManager().hasCustomMaxDamage(itemStack)) {
                    event.setCancelled(true);
                    event.getHook().pullHookedEntity();
                    hook.get().destroy();
                    plugin.getItemManager().increaseDamage(player, itemStack, event.getCaught() instanceof Item ? 3 : 5, true);
                }
            }
        }
    }

    private void onReelIn(PlayerFishEvent event) {
        Player player = event.getPlayer();
        getFishHook(player).ifPresent(hook -> {
            event.setCancelled(true);
            Optional<GamingPlayer> gamingPlayer = hook.getGamingPlayer();
            if (gamingPlayer.isPresent()) {
                ((AbstractGamingPlayer) gamingPlayer.get()).internalRightClick();
                return;
            }
            hook.onReelIn();
        });
    }

//    private void onCaughtFish(PlayerFishEvent event) {
//        Player player = event.getPlayer();
//        getFishHook(player).ifPresent(hook -> {
//            Optional<GamingPlayer> gamingPlayer = hook.getGamingPlayer();
//            if (gamingPlayer.isPresent()) {
//                if (gamingPlayer.get().handleRightClick()) {
//                    event.setCancelled(true);
//                }
//                return;
//            }
//            event.setCancelled(true);
//            hook.onReelIn();
//        });
//    }

    private void onCastRod(PlayerFishEvent event) {
        FishHook hook = event.getHook();
        Player player = event.getPlayer();
        Context<Player> context = Context.player(player);
        FishingGears gears = new FishingGears(context);
        if (!RequirementManager.isSatisfied(context, ConfigManager.mechanicRequirements())) {
            this.destroyHook(player.getUniqueId());
            return;
        }
        if (!gears.canFish()) {
            event.setCancelled(true);
            return;
        }
        if (EventUtils.fireAndCheckCancel(new RodCastEvent(event, gears))) {
            return;
        }
        plugin.debug(context::toString);
        CustomFishingHook customHook = new CustomFishingHook(plugin, hook, gears, context);
        CustomFishingHook previous = this.castHooks.put(player.getUniqueId(), customHook);
        if (previous != null) {
            plugin.debug("Previous hook is still in cache, which is not an expected behavior");
            previous.stop();
        }
    }

    private void onInGround(PlayerFishEvent event) {
        final Player player = event.getPlayer();
        if (player.getGameMode() != GameMode.CREATIVE) {
            ItemStack itemStack = player.getInventory().getItemInMainHand();
            if (itemStack.getType() != Material.FISHING_ROD) itemStack = player.getInventory().getItemInOffHand();
            if (itemStack.getType() == Material.FISHING_ROD) {
                if (plugin.getItemManager().hasCustomMaxDamage(itemStack)) {
                    event.setCancelled(true);
                    event.getHook().remove();
                    plugin.getItemManager().increaseDamage(player, itemStack, 2, true);
                }
            }
        }
    }

    @EventHandler
    public void onHookStateChange(FishingHookStateEvent event) {
        Player player = event.getPlayer();
        getFishHook(player).ifPresent(hook -> {
            switch (event.getState()) {
                case BITE -> hook.onBite();
                case LAND -> hook.onLand();
                case ESCAPE -> hook.onEscape();
                case LURE -> hook.onLure();
            }
        });
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onInteractEntity(PlayerInteractAtEntityEvent event) {
        Entity entity = event.getRightClicked();
        if (entity.getType() != EntityType.ARMOR_STAND) return;
        if (entity.getPersistentDataContainer().has(Objects.requireNonNull(NamespacedKey.fromString("temp-entity", BukkitCustomFishingPlugin.getInstance().getBootstrap())))) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        Entity entity = event.getRightClicked();
        if (entity.getType() != EntityType.ARMOR_STAND) return;
        if (entity.getPersistentDataContainer().has(Objects.requireNonNull(NamespacedKey.fromString("temp-entity", BukkitCustomFishingPlugin.getInstance().getBootstrap())))) {
            event.setCancelled(true);
        }
    }

    @Override
    public void destroyHook(UUID uuid) {
        CustomFishingHook hook = this.castHooks.remove(uuid);
        if (hook != null) {
            hook.stop();
        }
    }
}
