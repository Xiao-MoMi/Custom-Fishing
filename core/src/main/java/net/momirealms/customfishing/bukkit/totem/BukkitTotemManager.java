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

package net.momirealms.customfishing.bukkit.totem;

import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.api.event.TotemActivateEvent;
import net.momirealms.customfishing.api.mechanic.MechanicType;
import net.momirealms.customfishing.api.mechanic.action.ActionTrigger;
import net.momirealms.customfishing.api.mechanic.config.ConfigManager;
import net.momirealms.customfishing.api.mechanic.context.Context;
import net.momirealms.customfishing.api.mechanic.context.ContextKeys;
import net.momirealms.customfishing.api.mechanic.effect.EffectModifier;
import net.momirealms.customfishing.api.mechanic.requirement.RequirementManager;
import net.momirealms.customfishing.api.mechanic.totem.TotemConfig;
import net.momirealms.customfishing.api.mechanic.totem.TotemManager;
import net.momirealms.customfishing.api.mechanic.totem.block.TotemBlock;
import net.momirealms.customfishing.api.util.EventUtils;
import net.momirealms.customfishing.api.util.SimpleLocation;
import net.momirealms.customfishing.bukkit.util.LocationUtils;
import net.momirealms.customfishing.common.plugin.scheduler.SchedulerTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class BukkitTotemManager implements TotemManager, Listener {

    private final BukkitCustomFishingPlugin plugin;
    private final HashMap<String, List<TotemConfig>> block2Totem = new HashMap<>();
    private final HashMap<String, TotemConfig> id2Totem = new HashMap<>();
    private final List<String> allMaterials = Arrays.stream(Material.values()).map(Enum::name).toList();
    private final ConcurrentHashMap<SimpleLocation, ActivatedTotem> activatedTotems = new ConcurrentHashMap<>();
    private SchedulerTask timerCheckTask;

    public BukkitTotemManager(BukkitCustomFishingPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void load() {
        Bukkit.getPluginManager().registerEvents(this, plugin.getBoostrap());
        this.timerCheckTask = plugin.getScheduler().asyncRepeating(() -> {
            long time = System.currentTimeMillis();
            ArrayList<SimpleLocation> removed = new ArrayList<>();
            for (Map.Entry<SimpleLocation, ActivatedTotem> entry : activatedTotems.entrySet()) {
                if (time > entry.getValue().getExpireTime()) {
                    removed.add(entry.getKey());
                    entry.getValue().cancel();
                } else {
                    entry.getValue().doTimerAction();
                }
            }
            for (SimpleLocation simpleLocation : removed) {
                activatedTotems.remove(simpleLocation);
            }
        }, 1, 1, TimeUnit.SECONDS);
        plugin.debug("Loaded " + id2Totem.size() + " totems");
    }

    @Override
    public void unload() {
        HandlerList.unregisterAll(this);
        for (ActivatedTotem activatedTotem : this.activatedTotems.values())
            activatedTotem.cancel();
        this.activatedTotems.clear();
        if (this.timerCheckTask != null)
            this.timerCheckTask.cancel();
        this.block2Totem.clear();
        this.id2Totem.clear();
    }

    @Override
    public Collection<String> getActivatedTotems(Location location) {
        Collection<String> activated = new ArrayList<>();
        double nearest = Double.MAX_VALUE;
        String nearestTotemID = null;
        for (ActivatedTotem activatedTotem : activatedTotems.values()) {
            double distance = LocationUtils.getDistance(activatedTotem.getCoreLocation(), location);
            if (distance < activatedTotem.getRadius()) {
               activated.add(activatedTotem.getTotemConfig().id());
               if (nearest > distance) {
                   nearest = distance;
                   nearestTotemID = activatedTotem.getTotemConfig().id();
               }
            }
        }
        if (nearestTotemID == null) return List.of();
        if (!ConfigManager.allowMultipleTotemType()) {
            if (ConfigManager.allowSameTotemType()) {
                String finalNearestTotemID = nearestTotemID;
                activated.removeIf(element -> !element.equals(finalNearestTotemID));
                return activated;
            } else {
                return List.of(nearestTotemID);
            }
        } else {
            if (ConfigManager.allowSameTotemType()) {
                return activated;
            } else {
                return new HashSet<>(activated);
            }
        }
    }

    @EventHandler
    public void onBreakTotemCore(BlockBreakEvent event) {
        if (event.isCancelled())
            return;
        Location location = event.getBlock().getLocation();
        SimpleLocation simpleLocation = SimpleLocation.of(location);
        ActivatedTotem activatedTotem = activatedTotems.remove(simpleLocation);
        if (activatedTotem != null)
            activatedTotem.cancel();
    }

    @EventHandler (ignoreCancelled = true)
    public void onInteractBlock(PlayerInteractEvent event) {
        if (
            event.isBlockInHand() ||
            event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK ||
            event.getHand() != EquipmentSlot.HAND
        )
            return;

        Block block = event.getClickedBlock();
        assert block != null;
        String id = plugin.getBlockManager().getBlockID(block);
        List<TotemConfig> configs = block2Totem.get(id);
        if (configs == null)
            return;
        TotemConfig config = null;
        for (TotemConfig temp : configs) {
            if (temp.isRightPattern(block.getLocation())) {
                config = temp;
                break;
            }
        }
        if (config == null)
            return;

        Location location = block.getLocation();
        SimpleLocation simpleLocation = SimpleLocation.of(location);
        ActivatedTotem previous = this.activatedTotems.get(simpleLocation);
        if (previous != null) {
            return;
        }

        String totemID = config.id();
        final Player player = event.getPlayer();;
        Context<Player> context = Context.player(player);
        context.arg(ContextKeys.SLOT, event.getHand());
        Optional<EffectModifier> optionalEffectModifier = plugin.getEffectManager().getEffectModifier(totemID, MechanicType.TOTEM);
        if (optionalEffectModifier.isPresent()) {
            if (!RequirementManager.isSatisfied(context, optionalEffectModifier.get().requirements())) {
                return;
            }
        }

        if (EventUtils.fireAndCheckCancel(new TotemActivateEvent(player, block.getLocation(), config))) {
            return;
        }

        plugin.getEventManager().trigger(context, totemID, MechanicType.TOTEM, ActionTrigger.ACTIVATE);
        ActivatedTotem activatedTotem = new ActivatedTotem(player, location, config);
        this.activatedTotems.put(simpleLocation, activatedTotem);
    }

    @Override
    public boolean registerTotem(TotemConfig totem) {
        if (id2Totem.containsKey(totem.id())) {
            return false;
        }
        HashSet<String> coreMaterials = new HashSet<>();
        for (TotemBlock totemBlock : totem.totemCore()) {
            String text = totemBlock.getTypeCondition().getRawText();
            if (text.startsWith("*")) {
                String sub = text.substring(1);
                coreMaterials.addAll(allMaterials.stream().filter(it -> it.endsWith(sub)).toList());
            } else if (text.endsWith("*")) {
                String sub = text.substring(0, text.length() - 1);
                coreMaterials.addAll(allMaterials.stream().filter(it -> it.startsWith(sub)).toList());
            } else {
                coreMaterials.add(text);
            }
        }
        for (String material : coreMaterials) {
            List<TotemConfig> configs = this.block2Totem.getOrDefault(material, new ArrayList<>());
            configs.add(totem);
            this.block2Totem.put(material, configs);
        }
        id2Totem.put(totem.id(), totem);
        return true;
    }

    @NotNull
    @Override
    public Optional<TotemConfig> getTotem(String id) {
        return Optional.ofNullable(id2Totem.get(id));
    }
}
