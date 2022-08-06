package net.momirealms.customfishing.hook.skill;

import org.bukkit.entity.Player;

public interface SkillXP {
    void addXp(Player player, double amount);
}
