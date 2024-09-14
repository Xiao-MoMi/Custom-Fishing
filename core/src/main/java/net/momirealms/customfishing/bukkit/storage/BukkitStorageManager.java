/*
 *  Copyright (C) <2024> <XiaoMoMi>
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

package net.momirealms.customfishing.bukkit.storage;

import com.google.gson.JsonSyntaxException;
import dev.dejvokep.boostedyaml.YamlDocument;
import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.api.mechanic.config.ConfigManager;
import net.momirealms.customfishing.api.storage.DataStorageProvider;
import net.momirealms.customfishing.api.storage.StorageManager;
import net.momirealms.customfishing.api.storage.StorageType;
import net.momirealms.customfishing.api.storage.data.PlayerData;
import net.momirealms.customfishing.api.storage.user.UserData;
import net.momirealms.customfishing.bukkit.storage.method.database.nosql.MongoDBProvider;
import net.momirealms.customfishing.bukkit.storage.method.database.nosql.RedisManager;
import net.momirealms.customfishing.bukkit.storage.method.database.sql.H2Provider;
import net.momirealms.customfishing.bukkit.storage.method.database.sql.MariaDBProvider;
import net.momirealms.customfishing.bukkit.storage.method.database.sql.MySQLProvider;
import net.momirealms.customfishing.bukkit.storage.method.database.sql.SQLiteProvider;
import net.momirealms.customfishing.bukkit.storage.method.file.JsonProvider;
import net.momirealms.customfishing.bukkit.storage.method.file.YAMLProvider;
import net.momirealms.customfishing.common.helper.GsonHelper;
import net.momirealms.customfishing.common.plugin.scheduler.SchedulerTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class BukkitStorageManager implements StorageManager, Listener {

    private final BukkitCustomFishingPlugin plugin;
    private DataStorageProvider dataSource;
    private StorageType previousType;
    private final ConcurrentHashMap<UUID, UserData> onlineUserMap;
    private final HashSet<UUID> locked;
    private boolean hasRedis;
    private RedisManager redisManager;
    private String serverID;
    private SchedulerTask timerSaveTask;

    public BukkitStorageManager(BukkitCustomFishingPlugin plugin) {
        this.plugin = plugin;
        this.locked = new HashSet<>();
        this.onlineUserMap = new ConcurrentHashMap<>();
        Bukkit.getPluginManager().registerEvents(this, plugin.getBootstrap());
    }

    @Override
    public void reload() {
        YamlDocument config = plugin.getConfigManager().loadConfig("database.yml");
        this.serverID = config.getString("unique-server-id", "default");
        try {
            config.save(new File(plugin.getBootstrap().getDataFolder(), "database.yml"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // Check if storage type has changed and reinitialize if necessary
        StorageType storageType = StorageType.valueOf(config.getString("data-storage-method", "H2"));
        if (storageType != previousType) {
            if (this.dataSource != null) this.dataSource.disable();
            this.previousType = storageType;
            switch (storageType) {
                case H2 -> this.dataSource = new H2Provider(plugin);
                case JSON -> this.dataSource = new JsonProvider(plugin);
                case YAML -> this.dataSource = new YAMLProvider(plugin);
                case SQLite -> this.dataSource = new SQLiteProvider(plugin);
                case MySQL -> this.dataSource = new MySQLProvider(plugin);
                case MariaDB -> this.dataSource = new MariaDBProvider(plugin);
                case MongoDB -> this.dataSource = new MongoDBProvider(plugin);
            }
            if (this.dataSource != null) this.dataSource.initialize(config);
            else plugin.getPluginLogger().severe("No storage type is set.");
        }

        // Handle Redis configuration
        if (!this.hasRedis && config.getBoolean("Redis.enable", false)) {
            this.redisManager = new RedisManager(plugin);
            this.redisManager.initialize(config);
            this.hasRedis = true;
        }

        // Disable Redis if it was enabled but is now disabled
        if (this.hasRedis && !config.getBoolean("Redis.enable", false) && this.redisManager != null) {
            this.hasRedis = false;
            this.redisManager.disable();
            this.redisManager = null;
        }

        // Cancel any existing timerSaveTask
        if (this.timerSaveTask != null) {
            this.timerSaveTask.cancel();
        }

        // Schedule periodic data saving if dataSaveInterval is configured
        if (ConfigManager.dataSaveInterval() > 0)
            this.timerSaveTask = this.plugin.getScheduler().asyncRepeating(
                    () -> {
                        long time1 = System.currentTimeMillis();
                        this.dataSource.updateManyPlayersData(this.onlineUserMap.values(), !ConfigManager.lockData());
                        if (ConfigManager.logDataSaving())
                            plugin.getPluginLogger().info("Data Saved for online players. Took " + (System.currentTimeMillis() - time1) + "ms.");
                    },
                    ConfigManager.dataSaveInterval(),
                    ConfigManager.dataSaveInterval(),
                    TimeUnit.SECONDS
            );
    }

    /**
     * Disables the storage manager and cleans up resources.
     */
    @Override
    public void disable() {
        HandlerList.unregisterAll(this);
        if (this.timerSaveTask != null)
            this.timerSaveTask.cancel();
        if (this.dataSource != null && !onlineUserMap.isEmpty())
            this.dataSource.updateManyPlayersData(onlineUserMap.values(), true);
        if (this.dataSource != null)
            this.dataSource.disable();
        if (this.redisManager != null)
            this.redisManager.disable();
        this.onlineUserMap.clear();
    }

    @NotNull
    @Override
    public String getServerID() {
        return serverID;
    }

    @NotNull
    @Override
    public Optional<UserData> getOnlineUser(UUID uuid) {
        return Optional.ofNullable(onlineUserMap.get(uuid));
    }

    @NotNull
    @Override
    public Collection<UserData> getOnlineUsers() {
        return onlineUserMap.values();
    }

    @Override
    public CompletableFuture<Optional<UserData>> getOfflineUserData(UUID uuid, boolean lock) {
        CompletableFuture<Optional<PlayerData>> optionalDataFuture = dataSource.getPlayerData(uuid, lock);
        return optionalDataFuture.thenCompose(optionalUser -> {
            if (optionalUser.isEmpty()) {
                return CompletableFuture.completedFuture(Optional.empty());
            }
            PlayerData data = optionalUser.get();
            return CompletableFuture.completedFuture(Optional.of(UserData.builder()
                    .data(data)
                    .build()));
        });
    }

    @Override
    public CompletableFuture<Boolean> saveUserData(UserData userData, boolean unlock) {
        return dataSource.updatePlayerData(userData.uuid(), userData.toPlayerData(), unlock);
    }

    @NotNull
    @Override
    public DataStorageProvider getDataSource() {
        return dataSource;
    }

    /**
     * Event handler for when a player joins the server.
     * Locks the player's data and initiates data retrieval if Redis is not used,
     * otherwise, it starts a Redis data retrieval task.
     */
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        locked.add(uuid);
        if (!hasRedis) {
            waitLock(uuid, 1);
        } else {
            plugin.getScheduler().asyncLater(() -> redisManager.getChangeServer(uuid).thenAccept(changeServer -> {
                if (!changeServer) {
                    waitLock(uuid, 3);
                } else {
                    new RedisGetDataTask(uuid);
                }
            }), 500, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * Event handler for when a player quits the server.
     * If the player is not locked, it removes their OnlineUser instance,
     * updates the player's data in Redis and the data source.
     */
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (locked.contains(uuid))
            return;

        UserData onlineUser = onlineUserMap.remove(uuid);
        if (onlineUser == null) return;
        PlayerData data = onlineUser.toPlayerData();

        if (hasRedis) {
            redisManager.setChangeServer(uuid).thenRun(
                    () -> redisManager.updatePlayerData(uuid, data, true).thenRun(
                            () -> dataSource.updatePlayerData(uuid, data, true).thenAccept(
                                    result -> {
                                      if (result) locked.remove(uuid);
            })));
        } else {
            dataSource.updatePlayerData(uuid, data, true).thenAccept(
                    result -> {
                        if (result) locked.remove(uuid);
                    });
        }
    }

    /**
     * Runnable task for asynchronously retrieving data from Redis.
     * Retries up to 6 times and cancels the task if the player is offline.
     */
    private class RedisGetDataTask implements Runnable {

        private final UUID uuid;
        private int triedTimes;
        private final SchedulerTask task;

        public RedisGetDataTask(UUID uuid) {
            this.uuid = uuid;
            this.task = plugin.getScheduler().asyncRepeating(this, 0, 333, TimeUnit.MILLISECONDS);
        }

        @Override
        public void run() {
            triedTimes++;
            Player player = Bukkit.getPlayer(uuid);
            if (player == null || !player.isOnline()) {
                // offline
                task.cancel();
                return;
            }
            if (triedTimes >= 6) {
                waitLock(uuid, 3);
                task.cancel();
                return;
            }
            redisManager.getPlayerData(uuid, false).thenAccept(optionalData -> {
                if (optionalData.isPresent()) {
                    addOnlineUser(player, optionalData.get());
                    task.cancel();
                    if (ConfigManager.lockData()) dataSource.lockOrUnlockPlayerData(uuid, true);
                }
            });
        }
    }

    /**
     * Waits for data lock release with a delay and a maximum of three retries.
     *
     * @param uuid  The UUID of the player.
     * @param times The number of times this method has been retried.
     */
    private void waitLock(UUID uuid, int times) {
        plugin.getScheduler().asyncLater(() -> {
        var player = Bukkit.getPlayer(uuid);
        if (player == null || !player.isOnline())
            return;
        if (times > 3) {
            plugin.getPluginLogger().warn("Tried 3 times getting data for " + uuid + ". Giving up.");
            return;
        }
        this.dataSource.getPlayerData(uuid, ConfigManager.lockData()).thenAccept(optionalData -> {
            // Data should not be empty
            if (optionalData.isEmpty()) {
                plugin.getPluginLogger().severe("Unexpected error: Data is null");
                return;
            }

            if (optionalData.get().locked()) {
                waitLock(uuid, times + 1);
            } else {
                try {
                    addOnlineUser(player, optionalData.get());
                } catch (Exception e) {
                    plugin.getPluginLogger().severe("Unexpected error: " + e.getMessage(), e);
                }
            }
        });
        }, 1, TimeUnit.SECONDS);
    }

    private void addOnlineUser(Player player, PlayerData playerData) {
        this.locked.remove(player.getUniqueId());
        this.onlineUserMap.put(player.getUniqueId(), UserData.builder()
                .data(playerData)
                // update the name
                .name(player.getName())
                .build());
    }

    @Override
    public boolean isRedisEnabled() {
        return hasRedis;
    }

    @Nullable
    public RedisManager getRedisManager() {
        return redisManager;
    }

    @NotNull
    @Override
    public byte[] toBytes(@NotNull PlayerData data) {
        return toJson(data).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    @NotNull
    public String toJson(@NotNull PlayerData data) {
        return GsonHelper.get().toJson(data);
    }

    @NotNull
    @Override
    public PlayerData fromJson(String json) {
        try {
            return GsonHelper.get().fromJson(json, PlayerData.class);
        } catch (JsonSyntaxException e) {
            plugin.getPluginLogger().severe("Failed to parse PlayerData from json");
            plugin.getPluginLogger().info("Json: " + json);
            throw new RuntimeException(e);
        }
    }

    @Override
    @NotNull
    public PlayerData fromBytes(byte[] data) {
        return fromJson(new String(data, StandardCharsets.UTF_8));
    }
}
