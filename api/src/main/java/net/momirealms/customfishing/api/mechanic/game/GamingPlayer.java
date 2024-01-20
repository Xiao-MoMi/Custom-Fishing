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

package net.momirealms.customfishing.api.mechanic.game;

import net.momirealms.customfishing.api.mechanic.effect.Effect;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public interface GamingPlayer {

    /**
     * Cancel the game
     */
    void cancel();

    boolean isSuccessful();

    /**
     * @return whether to cancel the event
     */
    boolean onRightClick();

    /**
     * @return whether to cancel the event
     */
    boolean onSwapHand();

    /**
     * @return whether to cancel the event
     */
    boolean onLeftClick();

    /**
     * @return whether to cancel the event
     */
    boolean onChat(String message);

    /**
     * @return whether to cancel the event
     */
    boolean onJump();

    /**
     * @return whether to cancel the event
     */
    boolean onSneak();

    Player getPlayer();

    /**
     * @return effect reward based on game results
     */
    @Nullable Effect getEffectReward();
}
