/*
 *  Copyright (C) <2024> <XiaoMoMi>
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

package net.momirealms.customfishing.api.util;

import org.bukkit.Location;

import java.util.Objects;

/**
 * A simple representation of a location in a Minecraft world, identified by its world name and coordinates.
 */
public record SimpleLocation(String worldName, int x, int y, int z) {

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SimpleLocation other = (SimpleLocation) obj;
        if (!Objects.equals(worldName, other.worldName())) {
            return false;
        }
        if (Double.doubleToLongBits(this.x) != Double.doubleToLongBits(other.x)) {
            return false;
        }
        if (Double.doubleToLongBits(this.y) != Double.doubleToLongBits(other.y)) {
            return false;
        }
        if (Double.doubleToLongBits(this.z) != Double.doubleToLongBits(other.z)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 19 * hash + (worldName != null ? worldName.hashCode() : 0);
        hash = 19 * hash + Long.hashCode(Double.doubleToLongBits(this.x));
        hash = 19 * hash + Long.hashCode(Double.doubleToLongBits(this.y));
        hash = 19 * hash + Long.hashCode(Double.doubleToLongBits(this.z));
        return hash;
    }

    /**
     * Creates a {@link SimpleLocation} from a {@link Location} object.
     *
     * @param location the {@link Location} object
     * @return a new {@link SimpleLocation} representing the same location
     */
    public static SimpleLocation of(Location location) {
        return new SimpleLocation(location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }
}