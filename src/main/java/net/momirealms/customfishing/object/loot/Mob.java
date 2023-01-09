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

package net.momirealms.customfishing.object.loot;

import net.momirealms.customfishing.object.fishing.Difficulty;
import org.jetbrains.annotations.NotNull;

public class Mob extends Loot{

    private final String mobID;
    private final int mobLevel;
    private final MobVector mobVector;

    public Mob(String key, Difficulty[] difficulty, int time, int weight, String mobID, int level, MobVector vector) {
        super(key, difficulty, time, weight);
        this.mobID = mobID;
        this.mobLevel = level;
        this.mobVector = vector;
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
