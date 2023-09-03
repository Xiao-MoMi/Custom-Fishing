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
 *
 */

package net.momirealms.customfishing.compatibility.mob;

import io.lumine.mythic.api.adapters.AbstractLocation;
import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.bukkit.utils.serialize.Position;
import io.lumine.mythic.core.mobs.ActiveMob;
import net.momirealms.customfishing.api.mechanic.mob.MobLibrary;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.Map;
import java.util.Optional;

public class MythicMobsLibraryImpl implements MobLibrary {

    private MythicBukkit mythicBukkit;

    public MythicMobsLibraryImpl() {
        this.mythicBukkit = MythicBukkit.inst();
    }

    @Override
    public String identification() {
        return "MythicMobs";
    }

    @Override
    public Entity spawn(Location location, String id, Map<String, Object> mobPropertyMap) {
        if (this.mythicBukkit == null || mythicBukkit.isClosed()) {
            this.mythicBukkit = MythicBukkit.inst();
        }
        Optional<MythicMob> mythicMob = mythicBukkit.getMobManager().getMythicMob(id);
        if (mythicMob.isPresent()) {
            MythicMob theMob = mythicMob.get();
            Position position = Position.of(location);
            AbstractLocation abstractLocation = new AbstractLocation(position);
            ActiveMob activeMob = theMob.spawn(abstractLocation, (Double) mobPropertyMap.get("{level}"));
            return activeMob.getEntity().getBukkitEntity();
        }
        throw new NullPointerException("MythicMobs: " + id + " doesn't exist.");
    }
}
