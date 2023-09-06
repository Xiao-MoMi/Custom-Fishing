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

package net.momirealms.customfishing.api.manager;

import net.momirealms.customfishing.api.mechanic.TempFishingState;
import net.momirealms.customfishing.api.mechanic.effect.Effect;
import net.momirealms.customfishing.api.mechanic.game.GameInstance;
import net.momirealms.customfishing.api.mechanic.game.GameSettings;
import net.momirealms.customfishing.api.mechanic.game.GamingPlayer;
import net.momirealms.customfishing.api.mechanic.loot.Loot;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;

public interface FishingManager {
    boolean removeHook(UUID uuid);

    void setTempFishingState(Player player, TempFishingState tempFishingState);

    void removeHookCheckTask(Player player);

    Optional<FishHook> getHook(UUID uuid);

    void removeTempFishingState(Player player);

    void processGameResult(GamingPlayer gamingPlayer);

    void startFishingGame(Player player, Loot loot, Effect effect);

    void startFishingGame(Player player, GameSettings settings, GameInstance gameInstance);
}
