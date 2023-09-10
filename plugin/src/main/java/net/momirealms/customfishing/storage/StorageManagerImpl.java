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
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

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

    public StorageManagerImpl(CustomFishingPluginImpl plugin) {
        this.plugin = plugin;
        this.locked = new HashSet<>();
        this.onlineUserMap = new ConcurrentHashMap<>();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void reload() {
        YamlConfiguration config = plugin.getConfig("database.yml");
        this.uniqueID = config.getString("unique-server-id", "default");
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
        if (!this.hasRedis && config.getBoolean("Redis.enable", false)) {
            this.hasRedis = true;
            this.redisManager = new RedisManager(plugin);
            this.redisManager.initialize();
        }
        if (this.hasRedis && !config.getBoolean("Redis.enable", false) && this.redisManager != null) {
            this.redisManager.disable();
            this.redisManager = null;
        }
        if (this.timerSaveTask != null && !this.timerSaveTask.isCancelled()) {
            this.timerSaveTask.cancel();
        }
        if (CFConfig.dataSaveInterval != -1)
            this.timerSaveTask = this.plugin.getScheduler().runTaskAsyncTimer(
                    () -> {
                        long time1 = System.currentTimeMillis();
                        this.dataSource.savePlayersData(this.onlineUserMap.values(), false);
                        LogUtils.info("Data Saved for online players. Took " + (System.currentTimeMillis() - time1) + "ms.");
                    },
                    CFConfig.dataSaveInterval,
                    CFConfig.dataSaveInterval,
                    TimeUnit.SECONDS
            );
    }

    public void disable() {
        HandlerList.unregisterAll(this);
        this.dataSource.savePlayersData(onlineUserMap.values(), true);
        this.onlineUserMap.clear();
        if (this.dataSource != null)
            this.dataSource.disable();
        if (this.redisManager != null)
            this.redisManager.disable();
    }

    @Override
    public String getUniqueID() {
        return uniqueID;
    }

    @Override
    public OnlineUser getOnlineUser(UUID uuid) {
        return onlineUserMap.get(uuid);
    }

    @Override
    public CompletableFuture<Optional<OfflineUser>> getOfflineUser(UUID uuid, boolean force) {
        var optionalDataFuture = dataSource.getPlayerData(uuid, force);
        return optionalDataFuture.thenCompose(optionalUser -> {
            if (optionalUser.isEmpty()) {
                // locked
                return CompletableFuture.completedFuture(Optional.empty());
            }
            PlayerData data = optionalUser.get();
            if (data == PlayerData.LOCKED) {
                return CompletableFuture.completedFuture(Optional.of(OfflineUserImpl.LOCKED_USER));
            } else {
                OfflineUser offlineUser = new OfflineUserImpl(uuid, data.getName(), data);
                return CompletableFuture.completedFuture(Optional.of(offlineUser));
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> saveUserData(OfflineUser offlineUser, boolean unlock) {
        return dataSource.savePlayerData(offlineUser.getUUID(), offlineUser.getPlayerData(), unlock);
    }

    @Override
    public CompletableFuture<Integer> getRedisPlayerCount() {
        return redisManager.getPlayerCount();
    }

    @Override
    public DataStorageInterface getDataSource() {
        return dataSource;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        locked.add(uuid);
        if (!hasRedis) {
            waitForDataLockRelease(uuid, 1);
        } else {
            redisReadingData(uuid);
        }
    }

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
                    () -> redisManager.savePlayerData(uuid, data, true).thenRun(
                            () -> dataSource.savePlayerData(uuid, data, true).thenAccept(
                                    result -> {
                                      if (result) locked.remove(uuid);
            })));
        } else {
            dataSource.savePlayerData(uuid, data, true).thenAccept(
                    result -> {
                        if (result) locked.remove(uuid);
                    });
        }
    }

    public void redisReadingData(UUID uuid) {
        // delay 0.5s for another server to insert the key
        plugin.getScheduler().runTaskAsyncLater(() -> redisManager.getChangeServer(uuid).thenAccept(changeServer -> {
           if (!changeServer) {
               waitForDataLockRelease(uuid, 3);
           } else {
               new RedisGetDataTask(uuid);
           }
        }), 500, TimeUnit.MILLISECONDS);
    }

    public class RedisGetDataTask implements Runnable {

        private final UUID uuid;
        private int triedTimes;
        private final CancellableTask task;

        public RedisGetDataTask(UUID uuid) {
            this.uuid = uuid;
            this.task = plugin.getScheduler().runTaskAsyncTimer(this, 200, 200, TimeUnit.MILLISECONDS);
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
            if (triedTimes >= 10) {
                waitForDataLockRelease(uuid, 3);
                return;
            }
            redisManager.getPlayerData(uuid, false).thenAccept(optionalData -> {
                if (optionalData.isPresent()) {
                    putDataInCache(player, optionalData.get());
                    task.cancel();
                }
            });
        }
    }

    // wait 1 second for the lock to release
    // try three times at most
    public void waitForDataLockRelease(UUID uuid, int times) {
        plugin.getScheduler().runTaskAsyncLater(() -> {
        var player = Bukkit.getPlayer(uuid);
        if (player == null || !player.isOnline() || times > 3)
            return;
        this.dataSource.getPlayerData(uuid, false).thenAccept(optionalData -> {
            // should not be empty
            if (optionalData.isEmpty())
                return;
            if (optionalData.get() == PlayerData.LOCKED) {
                waitForDataLockRelease(uuid, times + 1);
            } else {
                putDataInCache(player, optionalData.get());
            }
        });
        }, 1, TimeUnit.SECONDS);
    }

    public void putDataInCache(Player player, PlayerData playerData) {
        locked.remove(player.getUniqueId());
        OnlineUserImpl bukkitUser = new OnlineUserImpl(player, playerData);
        onlineUserMap.put(player.getUniqueId(), bukkitUser);
    }

    @Override
    public boolean isRedisEnabled() {
        return hasRedis;
    }

    @Nullable
    public RedisManager getRedisManager() {
        return redisManager;
    }

    @Override
    public byte[] toBytes(@NotNull PlayerData data) {
        return toJson(data).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    @NotNull
    public String toJson(@NotNull PlayerData data) {
        return new GsonBuilder().create().toJson(data);
    }

    @Override
    @NotNull
    public PlayerData fromBytes(byte[] data) {
        try {
            return new GsonBuilder().create().fromJson(new String(data, StandardCharsets.UTF_8), PlayerData.class);
        } catch (JsonSyntaxException e) {
            throw new DataSerializationException("Failed to get PlayerData from bytes", e);
        }
    }

    public static class DataSerializationException extends RuntimeException {
        protected DataSerializationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
