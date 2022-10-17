package net.momirealms.customfishing.integration.mob;

import io.lumine.mythic.api.adapters.AbstractLocation;
import io.lumine.mythic.api.adapters.AbstractVector;
import io.lumine.mythic.api.mobs.MobManager;
import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.bukkit.utils.serialize.Position;
import io.lumine.mythic.core.mobs.ActiveMob;
import net.momirealms.customfishing.integration.MobInterface;
import net.momirealms.customfishing.object.loot.Mob;
import net.momirealms.customfishing.object.loot.MobVector;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.Optional;

public class MythicMobsMobImpl implements MobInterface {

    private final MobManager mobManager;

    public MythicMobsMobImpl() {
        this.mobManager = MythicBukkit.inst().getMobManager();
    }

    @Override
    public void summon(Location playerLoc, Location summonLoc, Mob mob){
        Optional<MythicMob> mythicMob = mobManager.getMythicMob(mob.getMobID());
        if (mythicMob.isPresent()) {
            MythicMob theMob = mythicMob.get();
            Position position = Position.of(summonLoc);
            AbstractLocation abstractLocation = new AbstractLocation(position);
            ActiveMob activeMob = theMob.spawn(abstractLocation, mob.getMobLevel());
            MobVector mobVector = mob.getMobVector();
            Vector vector = playerLoc.subtract(summonLoc).toVector().multiply((mobVector.horizontal()) - 1);
            vector = vector.setY((vector.getY() + 0.2) * mobVector.vertical());
            activeMob.getEntity().setVelocity(new AbstractVector(vector.getX(), vector.getY(), vector.getZ()));
        }
    }
}
