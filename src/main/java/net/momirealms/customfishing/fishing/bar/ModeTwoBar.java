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

import java.util.Random;

public class ModeTwoBar extends FishingBar {

    private final int[] timeRequirements;
    private final String judgementAreaImage;
    private final String fishImage;
    private final int barEffectiveWidth;
    private final int judgementAreaOffset;
    private final int judgementAreaWidth;
    private final int fishIconWidth;
    private final String[] progress;
    private final double punishment;
    private final double waterResistance;
    private final double pullingStrength;
    private final double looseningLoss;

    public ModeTwoBar(ConfigurationSection section) {
        super(section);
        this.timeRequirements = section.getIntegerList("hold-time-requirements").stream().mapToInt(Integer::intValue).toArray();
        this.judgementAreaImage = section.getString("subtitle.judgment-area");
        this.fishImage = section.getString("subtitle.fish");
        this.barEffectiveWidth = section.getInt("arguments.bar-effective-area-width");
        this.judgementAreaOffset = section.getInt("arguments.judgment-area-offset");
        this.judgementAreaWidth = section.getInt("arguments.judgment-area-width");
        this.fishIconWidth = section.getInt("arguments.fish-icon-width");
        this.punishment = section.getDouble("arguments.punishment");
        this.progress = section.getStringList("progress").toArray(new String[0]);
        this.waterResistance = section.getDouble("arguments.water-resistance", 0.15);
        this.pullingStrength = section.getDouble("arguments.pulling-strength", 0.45);
        this.looseningLoss = section.getDouble("arguments.loosening-strength-loss", 0.3);
    }

    public int getRandomTimeRequirement() {
        return timeRequirements[new Random().nextInt(timeRequirements.length)] * 20;
    }

    public String getJudgementAreaImage() {
        return judgementAreaImage;
    }

    public String getFishImage() {
        return fishImage;
    }

    public int getBarEffectiveWidth() {
        return barEffectiveWidth;
    }

    public int getJudgementAreaOffset() {
        return judgementAreaOffset;
    }

    public int getJudgementAreaWidth() {
        return judgementAreaWidth;
    }

    public int getFishIconWidth() {
        return fishIconWidth;
    }

    public String[] getProgress() {
        return progress;
    }

    public double getPunishment() {
        return punishment;
    }

    public double getWaterResistance() {
        return waterResistance;
    }

    public double getPullingStrength() {
        return pullingStrength;
    }

    public double getLooseningLoss() {
        return looseningLoss;
    }
}
