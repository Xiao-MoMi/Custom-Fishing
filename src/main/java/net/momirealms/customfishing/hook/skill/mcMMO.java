package net.momirealms.customfishing.hook.skill;

import com.gmail.nossr50.api.ExperienceAPI;
import org.bukkit.entity.Player;

public class mcMMO implements SkillXP{

    @Override
    public void addXp(Player player, double amount) {
        ExperienceAPI.addXP(player, "Fishing", (int) amount, "UNKNOWN");
    }
}
