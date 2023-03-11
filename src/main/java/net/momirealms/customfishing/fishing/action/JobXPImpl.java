package net.momirealms.customfishing.fishing.action;

import net.momirealms.customfishing.CustomFishing;
import net.momirealms.customfishing.integration.JobInterface;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public record JobXPImpl(double amount) implements Action {

    @Override
    public void doOn(Player player, @Nullable Player anotherPlayer) {
        JobInterface jobInterface = CustomFishing.getInstance().getIntegrationManager().getJobInterface();
        if (jobInterface == null) return;
        jobInterface.addXp(player, amount);
    }
}
