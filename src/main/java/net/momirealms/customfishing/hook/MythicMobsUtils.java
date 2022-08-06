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
import net.momirealms.customfishing.item.Loot;
import net.momirealms.customfishing.utils.VectorUtil;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.Optional;

public class MythicMobsUtils {

    public static void summonMM(Location pLocation, Location bLocation, Loot loot){
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
