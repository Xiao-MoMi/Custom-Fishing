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

package net.momirealms.customfishing.bukkit.totem.particle;

import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.common.plugin.scheduler.SchedulerTask;
import net.momirealms.customfishing.common.util.Pair;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class DustParticleSetting extends ParticleSetting {

    private final Particle.DustOptions dustOptions;

    public DustParticleSetting(
            String formulaHorizontal,
            String formulaVertical,
            Particle particle,
            double interval,
            List<Pair<Double, Double>> ranges,
            int delayTicks,
            int periodTicks,
            Particle.DustOptions dustOptions
    ) {
        super(formulaHorizontal, formulaVertical, particle, interval, ranges, delayTicks, periodTicks);
        this.dustOptions = dustOptions;
    }

    @SuppressWarnings("DuplicatedCode")
    public SchedulerTask start(Location location, double radius) {
        World world = location.getWorld();
        return BukkitCustomFishingPlugin.getInstance().getScheduler().asyncRepeating(() -> {
            for (Pair<Double, Double> range : ranges) {
                for (double theta = range.left(); theta <= range.right(); theta += interval) {
                    double r = expressionHorizontal.setVariable("theta", theta).setVariable("radius", radius).evaluate();
                    double x = r * Math.cos(theta) + 0.5;
                    double z = r * Math.sin(theta) + 0.5;
                    double y = expressionVertical.setVariable("theta", theta).setVariable("radius", radius).evaluate();
                    world.spawnParticle(particle, location.clone().add(x, y, z), 1,0,0,0, 0, dustOptions);
                }
            }
        }, delay * 50L, period * 50L, TimeUnit.MILLISECONDS);
    }
}
