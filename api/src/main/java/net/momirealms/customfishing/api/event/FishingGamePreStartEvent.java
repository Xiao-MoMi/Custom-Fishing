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

import net.momirealms.customfishing.api.mechanic.fishing.CustomFishingHook;
import net.momirealms.customfishing.api.mechanic.game.GameSetting;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class FishingGamePreStartEvent extends PlayerEvent {
    private static final HandlerList handlerList = new HandlerList();
    private final CustomFishingHook hook;
    private GameSetting setting;

    public FishingGamePreStartEvent(@NotNull CustomFishingHook hook, GameSetting setting) {
        super(hook.getContext().holder());
        this.setting = setting;
        this.hook = hook;
    }

    public CustomFishingHook hook() {
        return hook;
    }

    public GameSetting setting() {
        return setting;
    }

    public void setting(GameSetting setting) {
        this.setting = setting;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }
}
