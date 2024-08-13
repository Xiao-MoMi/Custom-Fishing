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

package net.momirealms.customfishing.api.mechanic.fishing;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.common.util.RandomUtils;
import net.momirealms.sparrow.heart.SparrowHeart;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class AntiAutoFishing {

    public static void prevent(Player player, FishHook hook) {
        BukkitCustomFishingPlugin.getInstance().debug("Try preventing player " + player.getName() + " from auto fishing");
        BukkitCustomFishingPlugin.getInstance().getSenderFactory().getAudience(player)
                .playSound(Sound.sound(
                        Key.key("minecraft", "entity.fishing_bobber.splash"),
                        Sound.Source.NEUTRAL,
                        0f,
                        1f
                ), hook.getX(), hook.getY(), hook.getZ());
        double motion = -0.4 * RandomUtils.generateRandomDouble(0.6, 1.0);
        SparrowHeart.getInstance().sendClientSideEntityMotion(player, new Vector(0, motion, 0), hook.getEntityId());
        SparrowHeart.getInstance().sendClientSideEntityMotion(player, new Vector(0, 0, 0), hook.getEntityId());
    }
}
