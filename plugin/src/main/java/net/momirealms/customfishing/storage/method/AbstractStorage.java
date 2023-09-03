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
 *
 */

package net.momirealms.customfishing.storage.method;

import net.momirealms.customfishing.api.CustomFishingPlugin;
import net.momirealms.customfishing.api.data.DataStorageInterface;

import java.time.Instant;

public abstract class AbstractStorage implements DataStorageInterface {

    protected CustomFishingPlugin plugin;

    public AbstractStorage(CustomFishingPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void initialize() {

    }

    @Override
    public void disable() {

    }

    public int getCurrentSeconds() {
        return (int) Instant.now().getEpochSecond();
    }
}
