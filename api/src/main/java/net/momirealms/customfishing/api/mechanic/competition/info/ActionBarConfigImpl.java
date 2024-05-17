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

public class ActionBarConfigImpl extends AbstractCompetitionInfo implements ActionBarConfig {

    public ActionBarConfigImpl(int refreshRate, int switchInterval, boolean showToAll, String[] texts) {
        super(refreshRate, switchInterval, showToAll, texts);
    }

    public static class BuilderImpl implements Builder {
        private int refreshRate = DEFAULT_REFRESH_RATE;
        private int switchInterval = DEFAULT_SWITCH_INTERVAL;
        private boolean showToAll = DEFAULT_VISIBILITY;
        private String[] texts = DEFAULT_TEXTS;
        @Override
        public BuilderImpl showToAll(boolean showToAll) {
            this.showToAll = showToAll;
            return this;
        }
        @Override
        public BuilderImpl refreshRate(int rate) {
            this.refreshRate = rate;
            return this;
        }
        @Override
        public BuilderImpl switchInterval(int interval) {
            this.switchInterval = interval;
            return this;
        }
        @Override
        public BuilderImpl text(String[] texts) {
            this.texts = texts;
            return this;
        }
        @Override
        public ActionBarConfigImpl build() {
            return new ActionBarConfigImpl(refreshRate, switchInterval, showToAll, texts);
        }
    }
}
