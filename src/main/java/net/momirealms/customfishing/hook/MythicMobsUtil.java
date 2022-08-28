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

package net.momirealms.customfishing.hook;

import io.lumine.mythic.api.adapters.AbstractLocation;
import io.lumine.mythic.api.adapters.AbstractVector;
import io.lumine.mythic.api.mobs.MobManager;
import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.bukkit.utils.serialize.Position;
import io.lumine.mythic.core.mobs.ActiveMob;
import net.momirealms.customfishing.object.MobVector;
import net.momirealms.customfishing.object.loot.Mob;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.Optional;

public class MythicMobsUtil {

    public static void summonMM(Location pLocation, Location bLocation, Mob mob){
        MobManager mobManager = MythicBukkit.inst().getMobManager();
        Optional<MythicMob> mythicMob = mobManager.getMythicMob(mob.getMmID());
        if (mythicMob.isPresent()) {
            MythicMob theMob = mythicMob.get();
            Position position = Position.of(bLocation);
            AbstractLocation abstractLocation = new AbstractLocation(position);
            ActiveMob activeMob = theMob.spawn(abstractLocation, mob.getMmLevel());
            MobVector mobVector = mob.getMobVector();
            Vector vector = pLocation.subtract(bLocation).toVector().multiply((mobVector.getHorizontal())-1);
            vector = vector.setY((vector.getY()+0.2)*mobVector.getVertical());
            activeMob.getEntity().setVelocity(new AbstractVector(vector.getX(),vector.getY(),vector.getZ()));
        }
    }
}
