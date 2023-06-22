package net.momirealms.customfishing.fishing.action;

import net.momirealms.customfishing.CustomFishing;
import net.momirealms.customfishing.integration.JobInterface;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class JobXPImpl extends AbstractAction implements Action {

    private final double amount;

    public JobXPImpl(double amount, double chance) {
        super(chance);
        this.amount = amount;
    }

    @Override
    public void doOn(Player player, @Nullable Player anotherPlayer) {
        if (!canExecute()) return;
        JobInterface jobInterface = CustomFishing.getInstance().getIntegrationManager().getJobInterface();
        if (jobInterface == null) return;
        jobInterface.addXp(player, amount);
    }
}
