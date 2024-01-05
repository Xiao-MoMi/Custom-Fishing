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

package net.momirealms.customfishing.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import net.momirealms.customfishing.CustomFishingPluginImpl;
import net.momirealms.customfishing.api.CustomFishingPlugin;
import net.momirealms.customfishing.api.data.DataStorageInterface;
import net.momirealms.customfishing.api.data.PlayerData;
import net.momirealms.customfishing.api.data.StorageType;
import net.momirealms.customfishing.api.data.user.OfflineUser;
import net.momirealms.customfishing.api.data.user.OnlineUser;
import net.momirealms.customfishing.api.manager.StorageManager;
import net.momirealms.customfishing.api.scheduler.CancellableTask;
import net.momirealms.customfishing.api.util.LogUtils;
import net.momirealms.customfishing.setting.CFConfig;
import net.momirealms.customfishing.storage.method.database.nosql.MongoDBImpl;
import net.momirealms.customfishing.storage.method.database.nosql.RedisManager;
import net.momirealms.customfishing.storage.method.database.sql.H2Impl;
import net.momirealms.customfishing.storage.method.database.sql.MariaDBImpl;
import net.momirealms.customfishing.storage.method.database.sql.MySQLImpl;
import net.momirealms.customfishing.storage.method.database.sql.SQLiteImpl;
import net.momirealms.customfishing.storage.method.file.JsonImpl;
import net.momirealms.customfishing.storage.method.file.YAMLImpl;
import net.momirealms.customfishing.storage.user.OfflineUserImpl;
import net.momirealms.customfishing.storage.user.OnlineUserImpl;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * This class implements the StorageManager interface and is responsible for managing player data storage.
 * It includes methods to handle player data retrieval, storage, and serialization.
 */
public class StorageManagerImpl implements StorageManager, Listener {

    private final CustomFishingPlugin plugin;
    private DataStorageInterface dataSource;
    private StorageType previousType;
    private final ConcurrentHashMap<UUID, OnlineUser> onlineUserMap;
    private final HashSet<UUID> locked;
    private boolean hasRedis;
    private RedisManager redisManager;
    private String uniqueID;
    private CancellableTask timerSaveTask;
    private final Gson gson;

    public StorageManagerImpl(CustomFishingPluginImpl plugin) {
        this.plugin = plugin;
        this.locked = new HashSet<>();
        this.onlineUserMap = new ConcurrentHashMap<>();
        this.gson = new GsonBuilder().create();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Reloads the storage manager configuration.
     */
    public void reload() {
        YamlConfiguration config = plugin.getConfig("database.yml");
        this.uniqueID = config.getString("unique-server-id", "default");

        // Check if storage type has changed and reinitialize if necessary
        StorageType storageType = StorageType.valueOf(config.getString("data-storage-method", "H2"));
        if (storageType != previousType) {
            if (this.dataSource != null) this.dataSource.disable();
            this.previousType = storageType;
            switch (storageType) {
                case H2 -> this.dataSource = new H2Impl(plugin);
                case JSON -> this.dataSource = new JsonImpl(plugin);
                case YAML -> this.dataSource = new YAMLImpl(plugin);
                case SQLite -> this.dataSource = new SQLiteImpl(plugin);
                case MySQL -> this.dataSource = new MySQLImpl(plugin);
                case MariaDB -> this.dataSource = new MariaDBImpl(plugin);
                case MongoDB -> this.dataSource = new MongoDBImpl(plugin);
            }
            if (this.dataSource != null) this.dataSource.initialize();
            else LogUtils.severe("No storage type is set.");
        }

        // Handle Redis configuration
        if (!this.hasRedis && config.getBoolean("Redis.enable", false)) {
            this.hasRedis = true;
            this.redisManager = new RedisManager(plugin);
            this.redisManager.initialize();
        }

        // Disable Redis if it was enabled but is now disabled
        if (this.hasRedis && !config.getBoolean("Redis.enable", false) && this.redisManager != null) {
            this.redisManager.disable();
            this.redisManager = null;
        }

        // Cancel any existing timerSaveTask
        if (this.timerSaveTask != null && !this.timerSaveTask.isCancelled()) {
            this.timerSaveTask.cancel();
        }

        // Schedule periodic data saving if dataSaveInterval is configured
        if (CFConfig.dataSaveInterval != -1 && CFConfig.dataSaveInterval != 0)
            this.timerSaveTask = this.plugin.getScheduler().runTaskAsyncTimer(
                    () -> {
                        long time1 = System.currentTimeMillis();
                        this.dataSource.updateManyPlayersData(this.onlineUserMap.values(), !CFConfig.lockData);
                        if (CFConfig.logDataSaving)
                            LogUtils.info("Data Saved for online players. Took " + (System.currentTimeMillis() - time1) + "ms.");
                    },
                    CFConfig.dataSaveInterval,
                    CFConfig.dataSaveInterval,
                    TimeUnit.SECONDS
            );
    }

    /**
     * Disables the storage manager and cleans up resources.
     */
    public void disable() {
        HandlerList.unregisterAll(this);
        this.dataSource.updateManyPlayersData(onlineUserMap.values(), true);
        this.onlineUserMap.clear();
        if (this.dataSource != null)
            this.dataSource.disable();
        if (this.redisManager != null)
            this.redisManager.disable();
    }

    /**
     * Gets the unique server identifier.
     *
     * @return The unique server identifier.
     */
    @NotNull
    @Override
    public String getUniqueID() {
        return uniqueID;
    }

    /**
     * Gets an OnlineUser instance for the specified UUID.
     *
     * @param uuid The UUID of the player.
     * @return An OnlineUser instance if the player is online, or null if not.
     */
    @Override
    public OnlineUser getOnlineUser(UUID uuid) {
        return onlineUserMap.get(uuid);
    }

    @Override
    public Collection<OnlineUser> getOnlineUsers() {
        return onlineUserMap.values();
    }

    /**
     * Asynchronously retrieves an OfflineUser instance for the specified UUID.
     *
     * @param uuid The UUID of the player.
     * @param lock Whether to lock the data during retrieval.
     * @return A CompletableFuture that resolves to an Optional containing the OfflineUser instance if found, or empty if not found or locked.
     */
    @Override
    public CompletableFuture<Optional<OfflineUser>> getOfflineUser(UUID uuid, boolean lock) {
        var optionalDataFuture = dataSource.getPlayerData(uuid, lock);
        return optionalDataFuture.thenCompose(optionalUser -> {
            if (optionalUser.isEmpty()) {
                // locked
                return CompletableFuture.completedFuture(Optional.empty());
            }
            PlayerData data = optionalUser.get();
            if (data.isLocked()) {
                return CompletableFuture.completedFuture(Optional.of(OfflineUserImpl.LOCKED_USER));
            } else {
                OfflineUser offlineUser = new OfflineUserImpl(uuid, data.getName(), data);
                return CompletableFuture.completedFuture(Optional.of(offlineUser));
            }
        });
    }

    @Override
    public boolean isLockedData(OfflineUser offlineUser) {
        return OfflineUserImpl.LOCKED_USER == offlineUser;
    }

    /**
     * Asynchronously saves user data for an OfflineUser.
     *
     * @param offlineUser The OfflineUser whose data needs to be saved.
     * @param unlock Whether to unlock the data after saving.
     * @return A CompletableFuture that resolves to a boolean indicating the success of the data saving operation.
     */
    @Override
    public CompletableFuture<Boolean> saveUserData(OfflineUser offlineUser, boolean unlock) {
        return dataSource.updatePlayerData(offlineUser.getUUID(), offlineUser.getPlayerData(), unlock);
    }

    /**
     * Gets the data source used for data storage.
     *
     * @return The data source.
     */
    @Override
    public DataStorageInterface getDataSource() {
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
            waitForDataLockRelease(uuid, 1);
        } else {
            plugin.getScheduler().runTaskAsyncLater(() -> redisManager.getChangeServer(uuid).thenAccept(changeServer -> {
                if (!changeServer) {
                    waitForDataLockRelease(uuid, 3);
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

        OnlineUser onlineUser = onlineUserMap.remove(uuid);
        if (onlineUser == null) return;
        PlayerData data = onlineUser.getPlayerData();

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
    public class RedisGetDataTask implements Runnable {

        private final UUID uuid;
        private int triedTimes;
        private final CancellableTask task;

        public RedisGetDataTask(UUID uuid) {
            this.uuid = uuid;
            this.task = plugin.getScheduler().runTaskAsyncTimer(this, 0, 333, TimeUnit.MILLISECONDS);
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
                waitForDataLockRelease(uuid, 3);
                return;
            }
            redisManager.getPlayerData(uuid, false).thenAccept(optionalData -> {
                if (optionalData.isPresent()) {
                    putDataInCache(player, optionalData.get());
                    task.cancel();
                    if (CFConfig.lockData) dataSource.lockOrUnlockPlayerData(uuid, true);
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
    public void waitForDataLockRelease(UUID uuid, int times) {
        plugin.getScheduler().runTaskAsyncLater(() -> {
        var player = Bukkit.getPlayer(uuid);
        if (player == null || !player.isOnline())
            return;
        if (times > 3) {
            LogUtils.warn("Tried 3 times when getting data for " + uuid + ". Giving up.");
            return;
        }
        this.dataSource.getPlayerData(uuid, CFConfig.lockData).thenAccept(optionalData -> {
            // Data should not be empty
            if (optionalData.isEmpty()) {
                LogUtils.severe("Unexpected error: Data is null");
                return;
            }

            if (optionalData.get().isLocked()) {
                waitForDataLockRelease(uuid, times + 1);
            } else {
                try {
                    putDataInCache(player, optionalData.get());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        }, 1, TimeUnit.SECONDS);
    }

    /**
     * Puts player data in cache and removes the player from the locked set.
     *
     * @param player     The player whose data is being cached.
     * @param playerData The data to be cached.
     */
    public void putDataInCache(Player player, PlayerData playerData) {
        locked.remove(player.getUniqueId());
        OnlineUserImpl bukkitUser = new OnlineUserImpl(player, playerData);
        onlineUserMap.put(player.getUniqueId(), bukkitUser);
    }

    /**
     * Checks if Redis is enabled.
     *
     * @return True if Redis is enabled; otherwise, false.
     */
    @Override
    public boolean isRedisEnabled() {
        return hasRedis;
    }

    /**
     * Gets the RedisManager instance.
     *
     * @return The RedisManager instance.
     */
    @Nullable
    public RedisManager getRedisManager() {
        return redisManager;
    }

    /**
     * Converts PlayerData to bytes.
     *
     * @param data The PlayerData to be converted.
     * @return The byte array representation of PlayerData.
     */
    @NotNull
    @Override
    public byte[] toBytes(@NotNull PlayerData data) {
        return toJson(data).getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Converts PlayerData to JSON format.
     *
     * @param data The PlayerData to be converted.
     * @return The JSON string representation of PlayerData.
     */
    @Override
    @NotNull
    public String toJson(@NotNull PlayerData data) {
        return gson.toJson(data);
    }

    /**
     * Converts JSON string to PlayerData.
     *
     * @param json The JSON string to be converted.
     * @return The PlayerData object.
     */
    @NotNull
    @Override
    public PlayerData fromJson(String json) {
        try {
            return gson.fromJson(json, PlayerData.class);
        } catch (JsonSyntaxException e) {
            LogUtils.severe("Failed to parse PlayerData from json");
            LogUtils.info("Json: " + json);
            throw new RuntimeException(e);
        }
    }

    /**
     * Converts bytes to PlayerData.
     *
     * @param data The byte array to be converted.
     * @return The PlayerData object.
     */
    @Override
    @NotNull
    public PlayerData fromBytes(byte[] data) {
        return fromJson(new String(data, StandardCharsets.UTF_8));
    }
}
