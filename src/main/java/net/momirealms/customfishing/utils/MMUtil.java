package net.momirealms.customfishing.utils;

import io.lumine.mythic.api.adapters.AbstractLocation;
import io.lumine.mythic.api.adapters.AbstractVector;
import io.lumine.mythic.api.mobs.MobManager;
import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.bukkit.utils.serialize.Position;
import io.lumine.mythic.core.mobs.ActiveMob;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.Optional;

public class MMUtil {

    public static void summonMM(Location pLocation, Location bLocation, LootInstance loot){
        MobManager mobManager = MythicBukkit.inst().getMobManager();
        Optional<MythicMob> mythicMob = mobManager.getMythicMob(loot.getMm());
        if (mythicMob.isPresent()) {
            //获取MM怪实例
            MythicMob theMob = mythicMob.get();
            //生成MM怪
            Position position = Position.of(bLocation);
            AbstractLocation abstractLocation = new AbstractLocation(position);
            ActiveMob activeMob = theMob.spawn(abstractLocation, loot.getMmLevel());

            VectorUtil vectorUtil = loot.getVectorUtil();
            Vector vector = pLocation.subtract(bLocation).toVector().multiply((vectorUtil.getHorizontal())-1);
            vector = vector.setY((vector.getY()+0.2)*vectorUtil.getVertical());
            activeMob.getEntity().setVelocity(new AbstractVector(vector.getX(),vector.getY(),vector.getZ()));
        }
    }
}
