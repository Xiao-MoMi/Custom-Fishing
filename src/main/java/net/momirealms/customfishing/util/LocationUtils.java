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

package net.momirealms.customfishing.util;

import net.momirealms.customfishing.CustomFishing;
import net.momirealms.customfishing.object.SimpleLocation;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.stream.Collectors;

public class LocationUtils {

    public static SimpleLocation getSimpleLocation(Location location) {
        return new SimpleLocation(location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public static Location getItemFrameBlockLocation(Location frameLoc) {
        return new Location(frameLoc.getWorld(), frameLoc.getBlockX(), frameLoc.getBlockY(), frameLoc.getBlockZ());
    }

    @Nullable
    public static Location getLocation(SimpleLocation location) {
        World world = Bukkit.getWorld(location.worldName());
        if (world == null) return null;
        return new Location(world, location.x(), location.y(), location.z());
    }

    public static SimpleLocation getSimpleLocation(String location, String world) {
        String[] loc = StringUtils.split(location, ",");
        return new SimpleLocation(world, Integer.parseInt(loc[0]), Integer.parseInt(loc[1]), Integer.parseInt(loc[2]));
    }

    public static Location getLocation(String location, World world) {
        String[] loc = StringUtils.split(location, ",");
        return new Location(world, Integer.parseInt(loc[0]), Integer.parseInt(loc[1]), Integer.parseInt(loc[2]));
    }

    public static String getStringLocation(Location location) {
        return location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ();
    }

    public static Collection<Player> getNearbyPlayers(Location location, double radius) {
        Collection<Player> nearbyPlayers;
        if (CustomFishing.getInstance().getVersionHelper().isSpigot()) {
            nearbyPlayers = location.getWorld().getNearbyEntities(location, radius, radius, radius)
                    .stream()
                    .filter(entity -> entity instanceof Player)
                    .map(entity -> (Player) entity)
                    .collect(Collectors.toList());
        }
        else {
            nearbyPlayers = location.getNearbyPlayers(radius);
        }
        return nearbyPlayers;
    }

    public static double getDistance(Location location1, Location location2) {
        double deltaX = location1.getX() - location2.getX();
        double deltaY = location1.getY() - location2.getY();
        double deltaZ = location1.getZ() - location2.getZ();
        return Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
    }
}
