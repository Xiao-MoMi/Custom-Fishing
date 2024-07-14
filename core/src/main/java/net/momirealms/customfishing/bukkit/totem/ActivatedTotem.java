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
import net.momirealms.customfishing.api.mechanic.MechanicType;
import net.momirealms.customfishing.api.mechanic.action.ActionTrigger;
import net.momirealms.customfishing.api.mechanic.context.Context;
import net.momirealms.customfishing.api.mechanic.context.ContextKeys;
import net.momirealms.customfishing.api.mechanic.totem.TotemConfig;
import net.momirealms.customfishing.api.mechanic.totem.TotemParticle;
import net.momirealms.customfishing.common.plugin.scheduler.SchedulerTask;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ActivatedTotem {

    private final List<SchedulerTask> subTasks;
    private final Location coreLocation;
    private final TotemConfig totemConfig;
    private final long expireTime;
    private final Context<Player> context;
    private final double radius;

    public ActivatedTotem(Player activator, Location coreLocation, TotemConfig config) {
        this.context = Context.player(activator, true)
                .arg(ContextKeys.LOCATION, coreLocation)
                .arg(ContextKeys.X, coreLocation.getBlockX())
                .arg(ContextKeys.Y, coreLocation.getBlockY())
                .arg(ContextKeys.Z, coreLocation.getBlockZ())
                .arg(ContextKeys.ID, config.id());
        this.subTasks = new ArrayList<>();
        this.expireTime = (long) (System.currentTimeMillis() + config.duration().evaluate(context) * 1000L);
        this.coreLocation = coreLocation.clone().add(0.5,0,0.5);
        this.totemConfig = config;
        this.radius = config.radius().evaluate(context);
        for (TotemParticle particleSetting : config.particleSettings()) {
            this.subTasks.add(particleSetting.start(coreLocation, radius));
        }
    }

    public TotemConfig getTotemConfig() {
        return totemConfig;
    }

    public Location getCoreLocation() {
        return coreLocation;
    }

    public void cancel() {
        for (SchedulerTask task : this.subTasks) {
            task.cancel();
        }
        this.subTasks.clear();
    }

    public long getExpireTime() {
        return this.expireTime;
    }

    public double getRadius() {
        return radius;
    }

    public void doTimerAction() {
        this.context.arg(ContextKeys.TIME_LEFT, String.valueOf((expireTime - System.currentTimeMillis())/1000));
        BukkitCustomFishingPlugin.getInstance().getEventManager().getEventCarrier(totemConfig.id(), MechanicType.TOTEM)
                .ifPresent(carrier -> carrier.trigger(context, ActionTrigger.TIMER));
    }
}
