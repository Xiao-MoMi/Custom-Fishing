package net.momirealms.customfishing.api.data.user;

import net.momirealms.customfishing.api.data.EarningData;
import net.momirealms.customfishing.api.data.PlayerData;
import net.momirealms.customfishing.api.mechanic.bag.FishingBagHolder;
import net.momirealms.customfishing.api.mechanic.statistic.Statistics;
import org.bukkit.entity.Player;

import java.util.UUID;

public interface OnlineUser {
    Player getPlayer();

    String getName();

    UUID getUUID();

    FishingBagHolder getHolder();

    EarningData getEarningData();

    Statistics getStatistics();

    boolean isOnline();

    PlayerData getPlayerData();
}
