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

package net.momirealms.customfishing.api;

import net.momirealms.customfishing.api.mechanic.config.ConfigManager;
import net.momirealms.customfishing.api.mechanic.event.EventManager;
import net.momirealms.customfishing.common.plugin.CustomFishingPlugin;

public abstract class BukkitCustomFishingPlugin implements CustomFishingPlugin {

    private static BukkitCustomFishingPlugin instance;

    protected EventManager eventManager;
    protected ConfigManager configManager;

    public BukkitCustomFishingPlugin() {
        instance = this;
    }

    public static BukkitCustomFishingPlugin getInstance() {
        if (instance == null) {
            throw new IllegalArgumentException("Plugin not initialized");
        }
        return instance;
    }

    public EventManager getEventManager() {
        return eventManager;
    }

    @Override
    public ConfigManager getConfigManager() {
        return configManager;
    }
}
