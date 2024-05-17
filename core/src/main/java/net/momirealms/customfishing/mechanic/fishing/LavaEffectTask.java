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

package net.momirealms.customfishing.mechanic.fishing;

import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.api.scheduler.CancellableTask;
import org.bukkit.Location;
import org.bukkit.Particle;

import java.util.concurrent.TimeUnit;

/**
 * A task responsible for creating a lava effect animation between two points.
 */
public class LavaEffectTask implements Runnable {

    private final Location startLoc;
    private final Location endLoc;
    private final Location controlLoc;
    private int timer;
    private final CancellableTask lavaTask;
    private final HookCheckTimerTask hookCheckTimerTask;

    /**
     * Constructs a new LavaEffectTask.
     *
     * @param hookCheckTimerTask The HookCheckTimerTask instance.
     * @param location                The starting location for the lava effect.
     * @param delay              The delay before starting the task.
     */
    public LavaEffectTask(HookCheckTimerTask hookCheckTimerTask, Location location, int delay) {
        this.hookCheckTimerTask = hookCheckTimerTask;
        this.startLoc = location.clone().add(0,0.3,0);
        this.endLoc = this.startLoc.clone().add((Math.random() * 16 - 8), startLoc.getY(), (Math.random() * 16 - 8));
        this.controlLoc = new Location(
                startLoc.getWorld(),
                (startLoc.getX() + endLoc.getX())/2 + Math.random() * 12 - 6,
                startLoc.getY(),
                (startLoc.getZ() + endLoc.getZ())/2 + Math.random() * 12 - 6
        );
        this.lavaTask = BukkitCustomFishingPlugin.get().getScheduler().runTaskAsyncTimer(this, delay * 50L, 50, TimeUnit.MILLISECONDS);
    }

    @Override
    public void run() {
        timer++;
        if (timer > 60) {
            lavaTask.cancel();
            BukkitCustomFishingPlugin.get().getScheduler().runTaskSync(hookCheckTimerTask::getHooked, startLoc);
        } else {
            double t = (double) timer / 60;
            Location particleLoc = endLoc.clone().multiply(Math.pow((1 - t), 2)).add(controlLoc.clone().multiply(2 * t * (1 - t))).add(startLoc.clone().multiply(Math.pow(t, 2)));
            particleLoc.setY(startLoc.getY());
            startLoc.getWorld().spawnParticle(Particle.FLAME, particleLoc,1,0,0,0,0);
        }
    }

    /**
     * Cancels the lava effect task.
     */
    public void cancel() {
        if (lavaTask != null && !lavaTask.isCancelled())
            lavaTask.cancel();
    }

    /**
     * Checks if the lava effect task is cancelled.
     *
     * @return True if the task is cancelled, false otherwise.
     */
    public boolean isCancelled() {
        return lavaTask.isCancelled();
    }
}
