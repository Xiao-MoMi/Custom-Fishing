package net.momirealms.customfishing.object.action;

import net.momirealms.customfishing.CustomFishing;
import org.bukkit.entity.Player;

public record SkillXPImpl(double amount) implements ActionInterface {

    @Override
    public void doOn(Player player) {
        CustomFishing.plugin.getIntegrationManager().getSkillInterface().addXp(player, amount);
    }
}