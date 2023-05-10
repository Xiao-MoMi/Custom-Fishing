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

package net.momirealms.customfishing.fishing.totem;

import net.momirealms.customfishing.CustomFishing;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class TotemParticle implements Runnable {

    private final Location bottomLoc;
    private final int radius;
    private final double angle_1;
    private final double angle_2;
    private int timer;
    private final Particle particle;
    private final World world;
    private final ScheduledFuture<?> task;

    public TotemParticle(Location bottomLoc, int radius, Particle particle) {
        this.bottomLoc = bottomLoc.clone().add(0.5,0,0.5);
        this.radius = radius;
        this.particle = particle;
        this.angle_1 = 360 / (double) radius;
        this.angle_2 = 72 / (double) radius;
        this.world = bottomLoc.getWorld();
        this.task = CustomFishing.getInstance().getScheduler().runTaskTimerAsync(this, 200, 200, TimeUnit.MILLISECONDS);
    }

    @Override
    public void run() {
        timer++;
        if (timer > 4) {
            timer = 0;
        }
        for (int i = 0; i < radius; i++) {
            double temp_angle = angle_1 * i + angle_2 * timer;
            double angle = temp_angle * Math.PI / 180;
            world.spawnParticle(particle, bottomLoc.clone().add(Math.cos(angle) * radius, 0.5, Math.sin(angle) * radius), 1 ,0, 0,0, 0);
        }
    }

    public void cancel() {
        this.task.cancel(false);
    }

    public boolean isCancelled() {
        return this.task.isCancelled();
    }
}
