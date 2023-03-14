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

    private final int[] time_requirements;
    private final String judgement_area_image;
    private final String fish_image;
    private final int bar_effective_width;
    private final int judgement_area_offset;
    private final int judgement_area_width;
    private final int fish_icon_width;
    private final String[] progress;
    private final double punishment;
    private final double water_resistance;
    private final double pulling_strength;
    private final double loosening_loss;

    public ModeTwoBar(ConfigurationSection section) {
        super(section);
        this.time_requirements = section.getIntegerList("hold-time-requirements").stream().mapToInt(Integer::intValue).toArray();
        this.judgement_area_image = section.getString("subtitle.judgment-area");
        this.fish_image = section.getString("subtitle.fish");
        this.bar_effective_width = section.getInt("arguments.bar-effective-area-width");
        this.judgement_area_offset = section.getInt("arguments.judgment-area-offset");
        this.judgement_area_width = section.getInt("arguments.judgment-area-width");
        this.fish_icon_width = section.getInt("arguments.fish-icon-width");
        this.punishment = section.getDouble("arguments.punishment");
        this.progress = section.getStringList("progress").toArray(new String[0]);
        this.water_resistance = section.getDouble("arguments.water-resistance", 0.15);
        this.pulling_strength = section.getDouble("arguments.pulling-strength", 0.45);
        this.loosening_loss = section.getDouble("arguments.loosening-strength-loss", 0.3);
    }

    public int getRandomTimeRequirement() {
        return time_requirements[new Random().nextInt(time_requirements.length)] * 20;
    }

    public String getJudgement_area_image() {
        return judgement_area_image;
    }

    public String getFish_image() {
        return fish_image;
    }

    public int getBar_effective_width() {
        return bar_effective_width;
    }

    public int getJudgement_area_offset() {
        return judgement_area_offset;
    }

    public int getJudgement_area_width() {
        return judgement_area_width;
    }

    public int getFish_icon_width() {
        return fish_icon_width;
    }

    public String[] getProgress() {
        return progress;
    }

    public double getPunishment() {
        return punishment;
    }

    public double getWater_resistance() {
        return water_resistance;
    }

    public double getPulling_strength() {
        return pulling_strength;
    }

    public double getLoosening_loss() {
        return loosening_loss;
    }
}
