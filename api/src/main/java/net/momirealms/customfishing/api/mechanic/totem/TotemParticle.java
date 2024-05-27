package net.momirealms.customfishing.api.mechanic.totem;

import net.momirealms.customfishing.common.plugin.scheduler.SchedulerTask;
import org.bukkit.Location;

public interface TotemParticle {

    /**
     * Start the particle task at specified location
     *
     * @param location location
     * @param radius totem radius
     * @return cancellable task
     */
    SchedulerTask start(Location location, double radius);
}
