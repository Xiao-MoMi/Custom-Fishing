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

package net.momirealms.customfishing.integration.mob;

import io.lumine.mythic.api.adapters.AbstractLocation;
import io.lumine.mythic.api.adapters.AbstractVector;
import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.bukkit.utils.serialize.Position;
import io.lumine.mythic.core.mobs.ActiveMob;
import net.momirealms.customfishing.fishing.loot.Mob;
import net.momirealms.customfishing.fishing.loot.MobVector;
import net.momirealms.customfishing.integration.MobInterface;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.Optional;

public class MythicMobsMobImpl implements MobInterface {

    private MythicBukkit mythicBukkit;

    public MythicMobsMobImpl() {
        this.mythicBukkit = MythicBukkit.inst();
    }

    @Override
    public void summon(Location playerLoc, Location summonLoc, Mob mob){
        if (this.mythicBukkit == null || mythicBukkit.isClosed()) {
            this.mythicBukkit = MythicBukkit.inst();
        }
        Optional<MythicMob> mythicMob = mythicBukkit.getMobManager().getMythicMob(mob.getMobID());
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
