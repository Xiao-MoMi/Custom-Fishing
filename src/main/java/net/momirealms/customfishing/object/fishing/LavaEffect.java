package net.momirealms.customfishing.object.fishing;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.scheduler.BukkitRunnable;


public class LavaEffect extends BukkitRunnable {

    private final Location startLoc;
    private final Location endLoc;
    private final Location controlLoc;
    private int timer;

    public LavaEffect(Location loc) {
        this.startLoc = loc.clone().add(0,0.3,0);
        this.endLoc = this.startLoc.clone().add((Math.random() * 16 - 8), startLoc.getY(), (Math.random() * 16 - 8));
        this.controlLoc = new Location(startLoc.getWorld(), (startLoc.getX() + endLoc.getX())/2 + Math.random() * 12 - 6, startLoc.getY(), (startLoc.getZ() + endLoc.getZ())/2 + Math.random() * 12 - 6);
    }

    @Override
    public void run() {
        timer++;
        if (timer > 60) {
            cancel();
        }
        else {
            double t = (double) timer / 60;
            Location particleLoc = endLoc.clone().multiply(Math.pow((1 - t), 2)).add(controlLoc.clone().multiply(2 * t * (1 - t))).add(startLoc.clone().multiply(Math.pow(t, 2)));
            particleLoc.setY(startLoc.getY());
            startLoc.getWorld().spawnParticle(Particle.FLAME, particleLoc,1,0,0,0,0);
        }
    }
}
