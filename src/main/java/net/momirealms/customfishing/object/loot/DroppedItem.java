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

package net.momirealms.customfishing.object.loot;

import net.momirealms.customfishing.object.fishing.Difficulty;
import org.jetbrains.annotations.Nullable;

public class DroppedItem extends Loot{

    private boolean randomDurability;
    private LeveledEnchantment[] randomEnchants;
    private final String material;
    private String[] size;
    private float basicPrice;
    private float sizeBonus;

    public DroppedItem(String key, Difficulty difficulty, int time, int weight, String material) {
        super(key, difficulty, time, weight);
        this.material = material;
    }

    public boolean isRandomDurability() {
        return randomDurability;
    }

    public void setRandomDurability(boolean randomDurability) {
        this.randomDurability = randomDurability;
    }

    @Nullable
    public LeveledEnchantment[] getRandomEnchants() {
        return randomEnchants;
    }

    public void setRandomEnchants(LeveledEnchantment[] randomEnchants) {
        this.randomEnchants = randomEnchants;
    }

    public String getMaterial() {
        return material;
    }

    public String[] getSize() {
        return size;
    }

    public void setSize(String[] size) {
        this.size = size;
    }

    public float getBasicPrice() {
        return basicPrice;
    }

    public void setBasicPrice(float basicPrice) {
        this.basicPrice = basicPrice;
    }

    public float getSizeBonus() {
        return sizeBonus;
    }

    public void setSizeBonus(float sizeBonus) {
        this.sizeBonus = sizeBonus;
    }
}
