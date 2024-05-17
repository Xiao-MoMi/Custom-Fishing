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

package net.momirealms.customfishing.mechanic.totem.particle;

import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.api.common.Pair;
import net.momirealms.customfishing.api.mechanic.totem.TotemParticle;
import net.momirealms.customfishing.api.scheduler.CancellableTask;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class ParticleSetting implements TotemParticle {

    protected final Expression expressionHorizontal;
    protected final Expression expressionVertical;
    protected final double interval;
    protected int delay;
    protected int period;
    protected final Particle particle;
    List<Pair<Double, Double>> ranges;

    public ParticleSetting(
            String formulaHorizontal,
            String formulaVertical,
            Particle particle,
            double interval,
            List<Pair<Double, Double>> ranges,
            int delayTicks,
            int periodTicks
    ) {
        this.interval = interval * Math.PI / 180;
        this.particle = particle;
        this.delay = delayTicks;
        this.period = periodTicks;
        this.ranges = ranges;
        this.expressionHorizontal = new ExpressionBuilder(formulaHorizontal)
                .variables("theta", "radius")
                .build();
        this.expressionVertical = new ExpressionBuilder(formulaVertical)
                .variables("theta", "radius")
                .build();
    }

    @SuppressWarnings("DuplicatedCode")
    public CancellableTask start(Location location, double radius) {
        World world = location.getWorld();
        return BukkitCustomFishingPlugin.get().getScheduler().runTaskAsyncTimer(() -> {
            for (Pair<Double, Double> range : ranges) {
                for (double theta = range.left(); theta <= range.right(); theta += interval) {
                    double r = expressionHorizontal.setVariable("theta", theta).setVariable("radius", radius).evaluate();
                    double x = r * Math.cos(theta) + 0.5;
                    double z = r * Math.sin(theta) + 0.5;
                    double y = expressionVertical.setVariable("theta", theta).setVariable("radius", radius).evaluate();
                    world.spawnParticle(particle, location.clone().add(x, y, z), 1,0,0,0);
                }
            }
        }, delay * 50L, period * 50L, TimeUnit.MILLISECONDS);
    }
}
