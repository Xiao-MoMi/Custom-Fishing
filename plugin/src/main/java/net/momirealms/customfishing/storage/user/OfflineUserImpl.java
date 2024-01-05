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

package net.momirealms.customfishing.storage.user;

import net.momirealms.customfishing.adventure.AdventureManagerImpl;
import net.momirealms.customfishing.api.CustomFishingPlugin;
import net.momirealms.customfishing.api.data.EarningData;
import net.momirealms.customfishing.api.data.InventoryData;
import net.momirealms.customfishing.api.data.PlayerData;
import net.momirealms.customfishing.api.data.StatisticData;
import net.momirealms.customfishing.api.data.user.OfflineUser;
import net.momirealms.customfishing.api.mechanic.bag.FishingBagHolder;
import net.momirealms.customfishing.api.mechanic.statistic.Statistics;
import net.momirealms.customfishing.api.util.InventoryUtils;
import net.momirealms.customfishing.compatibility.papi.PlaceholderManagerImpl;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of the OfflineUser interface for representing offline player data.
 */
public class OfflineUserImpl implements OfflineUser {

    private final UUID uuid;
    private final String name;
    private final FishingBagHolder holder;
    private final EarningData earningData;
    private final Statistics statistics;
    public static OfflineUserImpl LOCKED_USER = new OfflineUserImpl(UUID.randomUUID(), "-locked-", PlayerData.empty());

    /**
     * Constructor to create an OfflineUserImpl instance.
     *
     * @param uuid       The UUID of the player.
     * @param name       The name of the player.
     * @param playerData The player's data, including bag contents, earnings, and statistics.
     */
    public OfflineUserImpl(UUID uuid, String name, PlayerData playerData) {
        this.name = name;
        this.uuid = uuid;
        this.holder = new FishingBagHolder(uuid);
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
        // Set up the inventory for the FishingBagHolder
        this.holder.setInventory(InventoryUtils.createInventory(this.holder, playerData.getBagData().size,
                AdventureManagerImpl.getInstance().getComponentFromMiniMessage(
                        PlaceholderManagerImpl.getInstance().parse(
                                offlinePlayer,
                                CustomFishingPlugin.get().getBagManager().getBagTitle(),
                                Map.of("{player}", Optional.ofNullable(offlinePlayer.getName()).orElse(String.valueOf(uuid)))
                        )
                )));
        this.holder.setItems(InventoryUtils.getInventoryItems(playerData.getBagData().serialized));
        this.earningData = playerData.getEarningData();
        int date = CustomFishingPlugin.get().getMarketManager().getDate();
        if (earningData.date != date) {
            earningData.date = date;
            earningData.earnings = 0d;
        }
        this.statistics = new Statistics(playerData.getStatistics());
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public UUID getUUID() {
        return uuid;
    }

    @Override
    public FishingBagHolder getHolder() {
        return holder;
    }

    @Override
    public EarningData getEarningData() {
        return earningData;
    }

    @Override
    public Statistics getStatistics() {
        return statistics;
    }

    @Override
    public boolean isOnline() {
        Player player = Bukkit.getPlayer(uuid);
        return player != null && player.isOnline();
    }

    @Override
    public PlayerData getPlayerData() {
        // Create a new PlayerData instance based on the stored information
        return new PlayerData.Builder()
                .setBagData(new InventoryData(InventoryUtils.stacksToBase64(holder.getInventory().getStorageContents()), holder.getInventory().getSize()))
                .setEarningData(earningData)
                .setStats(new StatisticData(statistics.getStatisticMap(), statistics.getSizeMap()))
                .setName(name)
                .build();
    }
}
