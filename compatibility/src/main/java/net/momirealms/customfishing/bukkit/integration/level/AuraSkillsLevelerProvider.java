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

package net.momirealms.customfishing.bukkit.integration.level;

import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.registry.NamespacedId;
import net.momirealms.customfishing.api.event.FishingLootSpawnEvent;
import net.momirealms.customfishing.api.integration.LevelerProvider;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class AuraSkillsLevelerProvider implements LevelerProvider {

    @Override
    public String identifier() {
        return "AuraSkills";
    }

    @Override
    public void addXp(@NotNull Player player, @NotNull String target, double amount) {
        AuraSkillsApi.get().getUser(player.getUniqueId())
                .addSkillXp(AuraSkillsApi.get().getGlobalRegistry().getSkill(NamespacedId.fromDefault(target)), amount);
    }

    @Override
    public int getLevel(@NotNull Player player, @NotNull String target) {
        return AuraSkillsApi.get().getUser(player.getUniqueId()).getSkillLevel(
                AuraSkillsApi.get().getGlobalRegistry().getSkill(NamespacedId.fromDefault(target))
        );
    }
}

