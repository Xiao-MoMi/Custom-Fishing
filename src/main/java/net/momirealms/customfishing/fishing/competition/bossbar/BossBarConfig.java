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

package net.momirealms.customfishing.fishing.competition.bossbar;

import org.bukkit.boss.BarColor;

public class BossBarConfig {

    private final String[] text;
    private final int interval;
    private final BossBarOverlay overlay;
    private final BarColor color;
    private final int rate;
    private boolean showToAll;

    public BossBarConfig(String[] text, BossBarOverlay overlay, BarColor color, int rate, int interval, boolean showToAll) {
        this.text = text;
        this.overlay = overlay;
        this.color = color;
        this.rate = rate;
        this.interval = interval;
        this.showToAll = showToAll;
    }

    public String[] getText() {
        return text;
    }

    public int getInterval() {
        return interval;
    }

    public BossBarOverlay getOverlay() {
        return overlay;
    }

    public BarColor getColor() {
        return color;
    }

    public int getRate() {
        return rate;
    }

    public boolean isShowToAll() {
        return showToAll;
    }
}
