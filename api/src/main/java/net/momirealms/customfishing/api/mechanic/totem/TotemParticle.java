package net.momirealms.customfishing.api.mechanic.totem;

import net.momirealms.customfishing.api.scheduler.CancellableTask;
import org.bukkit.Location;

public interface TotemParticle {

    /**
     * Start the particle task at specified location
     *
     * @param location location
     * @param radius totem radius
     * @return cancellable task
     */
    CancellableTask start(Location location, double radius);
}
