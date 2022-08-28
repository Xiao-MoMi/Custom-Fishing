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

package net.momirealms.customfishing.object;

import org.bukkit.NamespacedKey;

public class LeveledEnchantment {

    private final NamespacedKey key;
    private final int level;
    private double chance;

    public LeveledEnchantment(NamespacedKey key, int level){
        this.key = key;
        this.level = level;
    }

    public void setChance(double chance) {
        this.chance = chance;
    }

    public int getLevel() {
        return level;
    }

    public NamespacedKey getKey() {
        return key;
    }

    public double getChance() {
        return chance;
    }
}
