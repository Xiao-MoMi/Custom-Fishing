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

public class ModeThreeBar extends FishingBar {

    private final String fishImage;
    private final int fishIconWidth;
    private final String[] strain;
    private final String[] strugglingFishImage;
    private final int barEffectiveWidth;
    private final int fishOffset;
    private final int fishStartPosition;
    private final int successPosition;
    private final double ultimateStrain;
    private final double normalIncrease;
    private final double strugglingIncrease;
    private final double strainLoss;

    public ModeThreeBar(ConfigurationSection section) {
        super(section);
        this.fishIconWidth = section.getInt("arguments.fish-icon-width");
        this.fishImage = section.getString("subtitle.fish");
        this.strain = section.getStringList("strain").toArray(new String[0]);
        this.strugglingFishImage = section.getStringList("subtitle.struggling-fish").toArray(new String[0]);
        this.barEffectiveWidth = section.getInt("arguments.bar-effective-area-width");
        this.fishOffset = section.getInt("arguments.fish-offset");
        this.fishStartPosition = section.getInt("arguments.fish-start-position");
        this.successPosition = section.getInt("arguments.success-position");
        this.ultimateStrain = section.getDouble("arguments.ultimate-strain", 50);
        this.normalIncrease = section.getDouble("arguments.normal-pull-strain-increase", 1);
        this.strugglingIncrease = section.getDouble("arguments.struggling-strain-increase", 2);
        this.strainLoss = section.getDouble("arguments.loosening-strain-loss", 2);
    }

    public String getFishImage() {
        return fishImage;
    }

    public int getFishIconWidth() {
        return fishIconWidth;
    }

    public String[] getStrain() {
        return strain;
    }

    public int getBarEffectiveWidth() {
        return barEffectiveWidth;
    }

    public int getFishOffset() {
        return fishOffset;
    }

    public int getFishStartPosition() {
        return fishStartPosition;
    }

    public int getSuccessPosition() {
        return successPosition;
    }

    public String[] getStrugglingFishImage() {
        return strugglingFishImage;
    }

    public double getUltimateStrain() {
        return ultimateStrain;
    }

    public double getNormalIncrease() {
        return normalIncrease;
    }

    public double getStrugglingIncrease() {
        return strugglingIncrease;
    }

    public double getStrainLoss() {
        return strainLoss;
    }
}
