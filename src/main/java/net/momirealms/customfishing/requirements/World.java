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

package net.momirealms.customfishing.requirements;

import net.momirealms.customfishing.utils.AdventureManager;

import java.util.List;

public record World(List<String> worlds) implements Requirement {

    public List<String> getWorlds() {
        return this.worlds;
    }

    @Override
    public boolean isConditionMet(FishingCondition fishingCondition) {
        org.bukkit.World world = fishingCondition.getLocation().getWorld();
        if (world != null) {
            return worlds.contains(world.getName());
        }
        AdventureManager.consoleMessage("<red>[CustomFishing] 这条消息不应该出现,玩家钓鱼时所处的世界并不存在!</red>");
        return false;
    }
}