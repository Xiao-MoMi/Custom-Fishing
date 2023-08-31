package net.momirealms.customfishing.api.data.user;

import net.momirealms.customfishing.api.data.EarningData;
import net.momirealms.customfishing.api.data.PlayerData;
import net.momirealms.customfishing.api.mechanic.bag.FishingBagHolder;
import net.momirealms.customfishing.api.mechanic.statistic.Statistics;

import java.util.UUID;

public interface OfflineUser {
    String getName();

    UUID getUUID();

    FishingBagHolder getHolder();

    EarningData getEarningData();

    Statistics getStatistics();

    boolean isOnline();

    PlayerData getPlayerData();
}
