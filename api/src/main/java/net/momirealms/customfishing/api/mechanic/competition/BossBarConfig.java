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

package net.momirealms.customfishing.api.mechanic.competition;

import org.bukkit.boss.BarColor;

public class BossBarConfig extends AbstractCompetitionInfo {

    private BarColor color;
    private Overlay overlay;

    public BarColor getColor() {
        return color;
    }

    public Overlay getOverlay() {
        return overlay;
    }

    public static class Builder {

        private final BossBarConfig config;

        public Builder() {
            this.config = new BossBarConfig();
        }

        public Builder showToAll(boolean showToAll) {
            this.config.showToAll = showToAll;
            return this;
        }

        public Builder refreshRate(int rate) {
            this.config.refreshRate = rate;
            return this;
        }

        public Builder switchInterval(int interval) {
            this.config.switchInterval = interval;
            return this;
        }

        public Builder text(String[] texts) {
            this.config.texts = texts;
            return this;
        }

        public Builder color(BarColor color) {
            this.config.color = color;
            return this;
        }

        public Builder overlay(Overlay overlay) {
            this.config.overlay = overlay;
            return this;
        }

        public BossBarConfig build() {
            return this.config;
        }
    }

    public enum Overlay {
        NOTCHED_6,
        NOTCHED_10,
        NOTCHED_12,
        NOTCHED_20,
        PROGRESS
    }
}
