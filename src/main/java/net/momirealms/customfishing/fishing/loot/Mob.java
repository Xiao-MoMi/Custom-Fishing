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

package net.momirealms.customfishing.fishing.loot;

import net.momirealms.customfishing.fishing.MiniGameConfig;
import org.jetbrains.annotations.NotNull;

public class Mob extends Loot{

    private final String mobID;
    private final int mobLevel;
    private final MobVector mobVector;

    public Mob(String key, String nick, MiniGameConfig[] fishingGames, int weight, boolean showInFinder, double score, String mobID, int mobLevel, MobVector mobVector, boolean disableBar, boolean disableStats) {
        super(key, nick, fishingGames, weight, showInFinder, score, disableBar, disableStats);
        this.mobID = mobID;
        this.mobLevel = mobLevel;
        this.mobVector = mobVector;
    }

    public String getMobID() {
        return mobID;
    }

    public int getMobLevel() {
        return mobLevel;
    }

    @NotNull
    public MobVector getMobVector() {
        return mobVector;
    }
}
