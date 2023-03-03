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

package net.momirealms.customfishing.fishing.bar;

import org.bukkit.configuration.ConfigurationSection;

import java.util.Objects;
import java.util.Set;

public class ModeOneBar extends FishingBar {

    private final int widthPerSection;
    private final double[] successRate;
    private final int totalWidth;
    private final int pointerWidth;

    private final String pointerImage;
    private final int pointerOffset;

    public ModeOneBar(ConfigurationSection section) {
        super(section);
        Set<String> chances = Objects.requireNonNull(section.getConfigurationSection("success-rate-sections")).getKeys(false);
        this.widthPerSection = section.getInt("arguments.width-per-section", 16);
        this.successRate = new double[chances.size()];
        for(int i = 0; i < chances.size(); i++)
            successRate[i] = section.getDouble("success-rate-sections." + (i + 1));
        this.totalWidth = chances.size() * widthPerSection - 1;
        this.pointerImage = section.getString("subtitle.pointer","뀄");
        this.pointerOffset = section.getInt("arguments.pointer-offset");
        this.pointerWidth = section.getInt("arguments.pointer-width");
    }

    public int getWidthPerSection() {
        return widthPerSection;
    }

    public double[] getSuccessRate() {
        return successRate;
    }

    public int getTotalWidth() {
        return totalWidth;
    }



    public String getPointerImage() {
        return pointerImage;
    }

    public int getPointerWidth() {
        return pointerWidth;
    }

    public int getPointerOffset() {
        return pointerOffset;
    }
}
