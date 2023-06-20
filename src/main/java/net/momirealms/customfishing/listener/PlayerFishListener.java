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

package net.momirealms.customfishing.listener;

import net.momirealms.customfishing.fishing.FishingCondition;
import net.momirealms.customfishing.fishing.requirements.RequirementInterface;
import net.momirealms.customfishing.manager.ConfigManager;
import net.momirealms.customfishing.manager.FishingManager;
import net.momirealms.customfishing.util.ConfigUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;

public record PlayerFishListener(FishingManager manager) implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onFishMONITOR(PlayerFishEvent event) {
        if (!ConfigManager.priority.equals("MONITOR")) return;
        selectState(event);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFishHIGHEST(PlayerFishEvent event) {
        if (!ConfigManager.priority.equals("HIGHEST")) return;
        selectState(event);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onFishHIGH(PlayerFishEvent event) {
        if (!ConfigManager.priority.equals("HIGH")) return;
        selectState(event);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onFishNORMAL(PlayerFishEvent event) {
        if (!ConfigManager.priority.equals("NORMAL")) return;
        selectState(event);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onFishLOW(PlayerFishEvent event) {
        if (!ConfigManager.priority.equals("LOW")) return;
        selectState(event);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onFishLOWEST(PlayerFishEvent event) {
        if (!ConfigManager.priority.equals("LOWEST")) return;
        selectState(event);
    }

    public void selectState(PlayerFishEvent event) {
        if (event.isCancelled()) return;
        if (!ConfigManager.getWorldsList().contains(event.getHook().getLocation().getWorld().getName())) return;
        FishingCondition fishingCondition = new FishingCondition(event.getPlayer().getLocation(), event.getPlayer(), null, null);
        if (ConfigManager.mechanicRequirements != null) {
            for (RequirementInterface requirement : ConfigManager.mechanicRequirements) {
                if (!requirement.isConditionMet(fishingCondition)) {
                    return;
                }
            }
        }
        switch (event.getState()) {
            case FISHING -> manager.onFishing(event);
            case REEL_IN -> manager.onReelIn(event);
            case CAUGHT_ENTITY -> manager.onCaughtEntity(event);
            case CAUGHT_FISH -> manager.onCaughtFish(event);
            case BITE -> manager.onBite(event);
        }
    }
}
