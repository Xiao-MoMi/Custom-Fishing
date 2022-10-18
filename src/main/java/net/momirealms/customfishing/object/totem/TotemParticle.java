package net.momirealms.customfishing.object.totem;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

public class TotemParticle extends BukkitRunnable {

    private final Location bottomLoc;
    private final int radius;
    private final double angle_1;
    private final double angle_2;
    private int timer;
    private final Particle particle;
    private final World world;

    public TotemParticle(Location bottomLoc, int radius, Particle particle) {
        this.bottomLoc = bottomLoc.clone().add(0.5,0,0.5);
        this.radius = radius;
        this.particle = particle;
        this.angle_1 = 360 / (double) radius;
        this.angle_2 = 72 / (double) radius;
        this.world = bottomLoc.getWorld();
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
}
