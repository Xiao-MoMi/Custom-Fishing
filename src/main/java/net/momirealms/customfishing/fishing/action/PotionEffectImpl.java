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
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.Nullable;

public class PotionEffectImpl extends AbstractAction implements Action {

    private final PotionEffect potionEffect;

    public PotionEffectImpl(PotionEffect potionEffect, double chance) {
        super(chance);
        this.potionEffect = potionEffect;
    }

    @Override
    public void doOn(Player player, @Nullable Player anotherPlayer) {
        if (!canExecute()) return;
        player.addPotionEffect(potionEffect);
    }
}
