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

package net.momirealms.customfishing.api.event;

import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * This class represents an event that is triggered when the Custom Fishing plugin is reloaded.
 */
public class CustomFishingReloadEvent extends Event {

    private static final HandlerList handlerList = new HandlerList();
    private final BukkitCustomFishingPlugin plugin;

    /**
     * Constructs a new CustomFishingReloadEvent.
     *
     * @param plugin The instance of the Custom Fishing plugin that is being reloaded
     */
    public CustomFishingReloadEvent(BukkitCustomFishingPlugin plugin) {
        this.plugin = plugin;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return getHandlerList();
    }

    /**
     * Gets the instance of the {@link BukkitCustomFishingPlugin} that is being reloaded.
     *
     * @return The instance of the Custom Fishing plugin
     */
    public BukkitCustomFishingPlugin getPluginInstance() {
        return plugin;
    }
}
