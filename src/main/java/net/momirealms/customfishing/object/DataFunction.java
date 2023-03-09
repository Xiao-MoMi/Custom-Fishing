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

package net.momirealms.customfishing.object;

import java.util.HashMap;
import java.util.UUID;

public abstract class DataFunction extends Function {

    protected final HashMap<UUID, Integer> triedTimes;

    public DataFunction() {
        this.triedTimes = new HashMap<>();
    }

    protected boolean checkTriedTimes(UUID uuid) {
        Integer previous = triedTimes.get(uuid);
        if (previous == null) {
            triedTimes.put(uuid, 1);
            return false;
        }
        else if (previous > 2) {
            triedTimes.remove(uuid);
            return true;
        }
        else {
            triedTimes.put(uuid, previous + 1);
            return false;
        }
    }
}
