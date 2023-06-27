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

package net.momirealms.customfishing.fishing.loot;

import net.momirealms.customfishing.fishing.MiniGameConfig;
import net.momirealms.customfishing.object.LeveledEnchantment;
import org.jetbrains.annotations.Nullable;

public class DroppedItem extends LootImpl {

    private final boolean randomDurability;
    private LeveledEnchantment[] randomEnchants;
    private final String material;
    private String[] size;
    private float basicPrice;
    private float sizeBonus;

    public DroppedItem(String key, String nick, String material, MiniGameConfig[] fishingGames, int weight, boolean showInFinder, double score, boolean randomDurability, boolean disableBar, boolean disableStats) {
        super(key, nick, fishingGames, weight, showInFinder, score, disableBar, disableStats);
        this.material = material;
        this.randomDurability = randomDurability;
    }

    public boolean isRandomDurability() {
        return randomDurability;
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
