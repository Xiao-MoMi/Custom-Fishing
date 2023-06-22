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

package net.momirealms.customfishing.fishing.action;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class ChainImpl extends AbstractAction implements Action {

    private final Action[] actions;

    public ChainImpl(Action[] actions, double chance) {
        super(chance);
        this.actions = actions;
    }

    @Override
    public void doOn(Player player, @Nullable Player anotherPlayer) {
        if (!canExecute()) return;
        for (Action action : actions) {
            action.doOn(player, anotherPlayer);
        }
    }
}
