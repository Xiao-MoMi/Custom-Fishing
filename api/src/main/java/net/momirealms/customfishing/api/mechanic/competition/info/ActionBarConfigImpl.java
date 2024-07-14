/*
 *  Copyright (C) <2024> <XiaoMoMi>
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

public class ActionBarConfigImpl extends AbstractCompetitionInfo implements ActionBarConfig {

    public ActionBarConfigImpl(boolean enable, int refreshRate, int switchInterval, boolean showToAll, String[] texts) {
        super(enable, refreshRate, switchInterval, showToAll, texts);
    }

    public static class BuilderImpl implements Builder {
        private int refreshRate = DEFAULT_REFRESH_RATE;
        private int switchInterval = DEFAULT_SWITCH_INTERVAL;
        private boolean showToAll = DEFAULT_VISIBILITY;
        private String[] texts = DEFAULT_TEXTS;
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
        public Builder enable(boolean enable) {
            this.enable = enable;
            return this;
        }
        @Override
        public ActionBarConfig build() {
            return new ActionBarConfigImpl(enable, refreshRate, switchInterval, showToAll, texts);
        }
    }
}
