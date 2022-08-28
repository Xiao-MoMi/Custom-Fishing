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

package net.momirealms.customfishing.hook.skill;

import net.Indyuce.mmocore.experience.EXPSource;
import net.Indyuce.mmocore.experience.Profession;
import org.bukkit.entity.Player;

public class MMOCore implements SkillXP{

    @Override
    public void addXp(Player player, double amount) {
        Profession profession = net.Indyuce.mmocore.MMOCore.plugin.professionManager.get("fishing");
        profession.giveExperience(net.Indyuce.mmocore.MMOCore.plugin.dataProvider.getDataManager().get(player), amount, null , EXPSource.OTHER);
    }

    @Override
    public int getLevel(Player player) {
        return net.Indyuce.mmocore.MMOCore.plugin.dataProvider.getDataManager().get(player).getLevel();
    }
}
