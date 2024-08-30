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

package net.momirealms.customfishing.api.mechanic.misc.hologram;

import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.common.helper.VersionHelper;
import net.momirealms.customfishing.common.plugin.feature.Reloadable;
import net.momirealms.customfishing.common.plugin.scheduler.SchedulerTask;
import net.momirealms.sparrow.heart.SparrowHeart;
import net.momirealms.sparrow.heart.feature.entity.FakeNamedEntity;
import net.momirealms.sparrow.heart.feature.entity.armorstand.FakeArmorStand;
import net.momirealms.sparrow.heart.feature.entity.display.FakeTextDisplay;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class HologramManager implements Reloadable {

    private final BukkitCustomFishingPlugin plugin;
    private final ConcurrentHashMap<Location, Hologram> holograms = new ConcurrentHashMap<>();
    private SchedulerTask task;

    public HologramManager(BukkitCustomFishingPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void load() {
        this.task = plugin.getScheduler().sync().runRepeating(() -> {
            ArrayList<Location> toRemove = new ArrayList<>();
            for (Map.Entry<Location, Hologram> entry : holograms.entrySet()) {
                if (entry.getValue().reduceTicks()) {
                    toRemove.add(entry.getKey());
                    entry.getValue().destroy();
                }
            }
            for (Location location : toRemove) {
                holograms.remove(location);
            }
        }, 1,1, null);
    }

    @Override
    public void unload() {
        if (this.task != null) {
            this.task.cancel();
        }
        for (Hologram hologram : holograms.values()) {
            hologram.destroy();
        }
        holograms.clear();
    }

    public void createHologram(Location location, String json, int ticks, boolean displayEntity, int[] rgba, Set<Player> viewers) {
        Hologram hologram = holograms.get(location);
        if (hologram == null) {
            FakeNamedEntity fakeNamedEntity;
            if (displayEntity && VersionHelper.isVersionNewerThan1_19_4()) {
                FakeTextDisplay textDisplay = SparrowHeart.getInstance().createFakeTextDisplay(location);
                textDisplay.rgba(rgba[0], rgba[1], rgba[2], rgba[3]);
                fakeNamedEntity = textDisplay;
            } else {
                FakeArmorStand armorStand = SparrowHeart.getInstance().createFakeArmorStand(location);
                armorStand.small(true);
                armorStand.invisible(true);
                fakeNamedEntity = armorStand;
            }
            hologram = new Hologram(fakeNamedEntity);
            hologram.name(json);
            hologram.updateNearbyPlayers(viewers);
            hologram.setTicksRemaining(ticks);
            holograms.put(location, hologram);
        } else {
            hologram.name(json);
            hologram.updateNearbyPlayers(viewers);
            hologram.setTicksRemaining(ticks);
        }
    }


}
