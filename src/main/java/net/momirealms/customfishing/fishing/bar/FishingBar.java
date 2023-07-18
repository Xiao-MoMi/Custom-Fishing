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

public abstract class FishingBar {

    protected String[] titles;
    protected String font;
    protected String barImage;
    protected String tip;

    public FishingBar(ConfigurationSection section) {
        this.tip = section.getString("tip");
        this.titles = section.getStringList("title").size() == 0 ? new String[]{section.getString("title")} : section.getStringList("title").toArray(new String[0]);
        this.font = section.getString("subtitle.font", "customfishing:bar");
        this.barImage = section.getString("subtitle.bar","뀃");
    }

    public String getRandomTitle() {
        return titles[new Random().nextInt(titles.length)];
    }

    public String getBarImage() {
        return barImage;
    }

    public String getFont() {
        return font;
    }

    public String getTip() {
        return tip;
    }
}
