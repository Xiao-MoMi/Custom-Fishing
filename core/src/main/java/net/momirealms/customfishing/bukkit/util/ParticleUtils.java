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

package net.momirealms.customfishing.bukkit.util;

import net.momirealms.customfishing.common.helper.VersionHelper;
import org.bukkit.Particle;

public class ParticleUtils {

    public static Particle getParticle(String particle) {
        if (!VersionHelper.isVersionNewerThan1_20_5()) return Particle.valueOf(particle);
        return switch (particle) {
            case "REDSTONE" -> Particle.valueOf("DUST");
            case "VILLAGER_HAPPY" -> Particle.valueOf("HAPPY_VILLAGER");
            default -> Particle.valueOf(particle);
        };
    }
}
