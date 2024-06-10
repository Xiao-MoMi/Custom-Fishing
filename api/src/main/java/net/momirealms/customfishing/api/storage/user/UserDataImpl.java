package net.momirealms.customfishing.api.storage.user;

import net.momirealms.customfishing.api.mechanic.bag.FishingBagHolder;
import net.momirealms.customfishing.api.mechanic.statistic.FishingStatistics;
import net.momirealms.customfishing.api.storage.data.EarningData;
import net.momirealms.customfishing.api.storage.data.InventoryData;
import net.momirealms.customfishing.api.storage.data.PlayerData;
import net.momirealms.customfishing.api.storage.data.StatisticData;
import net.momirealms.customfishing.api.util.InventoryUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public class UserDataImpl implements UserData {

    private final String name;
    private final UUID uuid;
    private final FishingBagHolder holder;
    private final EarningData earningData;
    private final FishingStatistics statistics;
    private final boolean isLocked;

    public UserDataImpl(String name, UUID uuid, FishingBagHolder holder, EarningData earningData, FishingStatistics statistics, boolean isLocked) {
        this.name = name;
        this.uuid = uuid;
        this.holder = holder;
        this.earningData = earningData;
        this.statistics = statistics;
        this.isLocked = isLocked;
    }

    public static class BuilderImpl implements Builder {
        private String name;
        private UUID uuid;
        private FishingBagHolder holder;
        private EarningData earningData;
        private FishingStatistics statistics;
        private boolean isLocked;
        @Override
        public Builder name(String name) {
            this.name = name;
            return this;
        }
        @Override
        public Builder uuid(UUID uuid) {
            this.uuid = uuid;
            return this;
        }
        @Override
        public Builder holder(FishingBagHolder holder) {
            this.holder = holder;
            return this;
        }
        @Override
        public Builder earningData(EarningData earningData) {
            this.earningData = earningData.copy();
            return this;
        }
        @Override
        public Builder statistics(FishingStatistics statistics) {
            this.statistics = statistics;
            return this;
        }
        @Override
        public Builder locked(boolean isLocked) {
            this.isLocked = isLocked;
            return this;
        }
        @Override
        public Builder data(PlayerData playerData) {
            this.isLocked = playerData.locked();
            this.uuid = playerData.uuid();
            this.name = playerData.name();
            this.earningData = playerData.earningData().copy();
            this.holder = FishingBagHolder.create(playerData.uuid(), InventoryUtils.getInventoryItems(playerData.bagData().serialized), playerData.bagData().size);
            this.statistics = FishingStatistics.builder().amountMap(playerData.statistics().amountMap).sizeMap(playerData.statistics().sizeMap).build();
            return this;
        }
        @Override
        public UserData build() {
            return new UserDataImpl(name, uuid, holder, earningData, statistics, isLocked);
        }
    }

    @NotNull
    @Override
    public String name() {
        return name;
    }

    @NotNull
    @Override
    public UUID uuid() {
        return uuid;
    }

    @Nullable
    @Override
    public Player player() {
        return Bukkit.getPlayer(uuid);
    }

    @NotNull
    @Override
    public FishingBagHolder holder() {
        return holder;
    }

    @NotNull
    @Override
    public EarningData earningData() {
        return earningData;
    }

    @NotNull
    @Override
    public FishingStatistics statistics() {
        return statistics;
    }

    @Override
    public boolean isOnline() {
        return Optional.ofNullable(Bukkit.getPlayer(uuid)).map(OfflinePlayer::isOnline).orElse(false);
    }

    @Override
    public boolean isLocked() {
        return isLocked;
    }

    @NotNull
    @Override
    public PlayerData toPlayerData() {
        return PlayerData.builder()
                .uuid(uuid)
                .bag(new InventoryData(InventoryUtils.stacksToBase64(holder.getInventory().getStorageContents()), holder.getInventory().getSize()))
                .earnings(earningData)
                .statistics(new StatisticData(statistics.amountMap(), statistics.sizeMap()))
                .name(name)
                .build();
    }
}
