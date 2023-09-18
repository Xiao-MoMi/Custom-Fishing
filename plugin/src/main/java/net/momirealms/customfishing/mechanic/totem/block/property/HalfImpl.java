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

package net.momirealms.customfishing.mechanic.totem.block.property;

import org.bukkit.Axis;
import org.bukkit.block.Block;
import org.bukkit.block.data.Bisected;

import java.io.Serializable;
import java.util.Locale;

public class HalfImpl implements TotemBlockProperty, Serializable {
    private final Bisected.Half half;

    public HalfImpl(Bisected.Half half) {
        this.half = half;
    }

    /**
     * half is not affected by mirroring.
     * @param axis The axis to mirror.
     * @return this
     */
    @Override
    public TotemBlockProperty mirror(Axis axis) {
        return this;
    }

    /**
     * half is not affected by rotation.
     * @return this
     */
    @Override
    public TotemBlockProperty rotate90() {
        return this;
    }

    /**
     * Checks if the block's half is the same as the half of this property.
     * @param block The block to check.
     * @return true if the block's half is the same as the half of this property.
     */
    @Override
    public boolean isPropertyMet(Block block) {
        if (block.getBlockData() instanceof Bisected bisected) {
            return bisected.getHalf().equals(this.half);
        }
        return false;
    }

    /**
     * Returns the raw text of the half property.
     * @return The raw text of the half property.
     */
    @Override
    public String getRawText() {
        return "half=" + half.name().toLowerCase(Locale.ENGLISH);
    }
}
