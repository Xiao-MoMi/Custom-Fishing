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

package net.momirealms.customfishing.api.mechanic.competition.info;

import net.kyori.adventure.bossbar.BossBar;

public class BossBarConfigImpl extends AbstractCompetitionInfo implements BossBarConfig {

    private final BossBar.Color color;
    private final BossBar.Overlay overlay;

    public BossBarConfigImpl(boolean enable, int refreshRate, int switchInterval, boolean showToAll, String[] texts, BossBar.Color color, BossBar.Overlay overlay) {
        super(enable, refreshRate, switchInterval, showToAll, texts);
        this.color = color;
        this.overlay = overlay;
    }

    @Override
    public BossBar.Color color() {
        return color;
    }

    @Override
    public BossBar.Overlay overlay() {
        return overlay;
    }

    public static class BuilderImpl implements Builder {
        private int refreshRate = DEFAULT_REFRESH_RATE;
        private int switchInterval = DEFAULT_SWITCH_INTERVAL;
        private boolean showToAll = DEFAULT_VISIBILITY;
        private String[] texts = DEFAULT_TEXTS;
        private BossBar.Overlay overlay = DEFAULT_OVERLAY;
        private BossBar.Color color = DEFAULT_COLOR;
        private boolean enable = true;
        @Override
        public Builder showToAll(boolean showToAll) {
            this.showToAll = showToAll;
            return this;
        }
        @Override
        public Builder refreshRate(int rate) {
            this.refreshRate = rate;
            return this;
        }
        @Override
        public Builder switchInterval(int interval) {
            this.switchInterval = interval;
            return this;
        }
        @Override
        public Builder text(String[] texts) {
            this.texts = texts;
            return this;
        }
        @Override
        public Builder color(BossBar.Color color) {
            this.color = color;
            return this;
        }
        @Override
        public Builder overlay(BossBar.Overlay overlay) {
            this.overlay = overlay;
            return this;
        }
        @Override
        public Builder enable(boolean enable) {
            this.enable = enable;
            return this;
        }
        @Override
        public BossBarConfig build() {
            return new BossBarConfigImpl(enable, refreshRate, switchInterval, showToAll, texts, color, overlay);
        }
    }
}
