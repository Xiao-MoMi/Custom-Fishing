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

package net.momirealms.customfishing.storage.method.database.nosql;

import net.momirealms.customfishing.api.CustomFishingPlugin;
import net.momirealms.customfishing.api.data.PlayerData;
import net.momirealms.customfishing.api.data.StorageType;
import net.momirealms.customfishing.api.util.LogUtils;
import net.momirealms.customfishing.setting.CFConfig;
import net.momirealms.customfishing.storage.method.AbstractStorage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import redis.clients.jedis.*;
import redis.clients.jedis.exceptions.JedisException;
import redis.clients.jedis.params.XReadParams;
import redis.clients.jedis.resps.StreamEntry;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * A RedisManager class responsible for managing interactions with a Redis server for data storage.
 */
public class RedisManager extends AbstractStorage {

    private static RedisManager instance;
    private final static String STREAM = "customfishing";
    private JedisPool jedisPool;
    private String password;
    private int port;
    private String host;
    private boolean useSSL;
    private BlockingThreadTask threadTask;
    private boolean isNewerThan5;

    public RedisManager(CustomFishingPlugin plugin) {
        super(plugin);
        instance = this;
    }

    /**
     * Get the singleton instance of the RedisManager.
     *
     * @return The RedisManager instance.
     */
    public static RedisManager getInstance() {
        return instance;
    }

    /**
     * Get a Jedis resource for interacting with the Redis server.
     *
     * @return A Jedis resource.
     */
    public Jedis getJedis() {
        return jedisPool.getResource();
    }

    /**
     * Initialize the Redis connection and configuration based on the plugin's YAML configuration.
     */
    @Override
    public void initialize() {
        YamlConfiguration config = plugin.getConfig("database.yml");
        ConfigurationSection section = config.getConfigurationSection("Redis");
        if (section == null) {
            LogUtils.warn("Failed to load database config. It seems that your config is broken. Please regenerate a new one.");
            return;
        }

        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setTestWhileIdle(true);
        jedisPoolConfig.setTimeBetweenEvictionRuns(Duration.ofMillis(30000));
        jedisPoolConfig.setNumTestsPerEvictionRun(-1);
        jedisPoolConfig.setMinEvictableIdleTime(Duration.ofMillis(section.getInt("MinEvictableIdleTimeMillis",1800000)));
        jedisPoolConfig.setMaxTotal(section.getInt("MaxTotal",8));
        jedisPoolConfig.setMaxIdle(section.getInt("MaxIdle",8));
        jedisPoolConfig.setMinIdle(section.getInt("MinIdle",1));
        jedisPoolConfig.setMaxWait(Duration.ofMillis(section.getInt("MaxWaitMillis")));

        password = section.getString("password", "");
        port = section.getInt("port", 6379);
        host = section.getString("host", "localhost");
        useSSL = section.getBoolean("use-ssl", false);

        if (password.isBlank()) {
            jedisPool = new JedisPool(jedisPoolConfig, host, port, 0, useSSL);
        } else {
            jedisPool = new JedisPool(jedisPoolConfig, host, port, 0, password, useSSL);
        }
        String info;
        try (Jedis jedis = jedisPool.getResource()) {
            info = jedis.info();
            LogUtils.info("Redis server connected.");
        } catch (JedisException e) {
            LogUtils.warn("Failed to connect redis.", e);
            return;
        }

        String version = parseRedisVersion(info);
        if (isRedisNewerThan5(version)) {
            // For Redis 5.0+
            this.threadTask = new BlockingThreadTask();
            this.isNewerThan5 = true;
        } else {
            // For Redis 2.0+
            this.subscribe();
            this.isNewerThan5 = false;
        }
    }

    /**
     * Disable the Redis connection by closing the JedisPool.
     */
    @Override
    public void disable() {
        if (threadTask != null)
            threadTask.stop();
        if (jedisPool != null && !jedisPool.isClosed())
            jedisPool.close();
    }

    /**
     * Send a message to Redis on a specified channel.
     *
     * @param server The Redis channel to send the message to.
     * @param message The message to send.
     */
    public void publishRedisMessage(@NotNull String server, @NotNull String message) {
        message = server + ";" + message;
        plugin.debug("Sent Redis message: " + message);
        if (isNewerThan5) {
            try (Jedis jedis = jedisPool.getResource()) {
                HashMap<String, String> messages = new HashMap<>();
                messages.put("value", message);
                jedis.xadd(getStream(), StreamEntryID.NEW_ENTRY, messages);
            }
        } else {
            try (Jedis jedis = jedisPool.getResource()) {
                jedis.publish(getStream(), message);
            }
        }
    }

    /**
     * Subscribe to Redis messages on a separate thread and handle received messages.
     */
    private void subscribe() {
        Thread thread = new Thread(() -> {
            try (final Jedis jedis = password.isBlank() ?
                    new Jedis(host, port, 0, useSSL) :
                    new Jedis(host, port, DefaultJedisClientConfig
                            .builder()
                            .password(password)
                            .timeoutMillis(0)
                            .ssl(useSSL)
                            .build())
            ) {
                jedis.connect();
                jedis.subscribe(new JedisPubSub() {
                    @Override
                    public void onMessage(String channel, String message) {
                        if (!channel.equals(getStream())) {
                            return;
                        }
                        handleMessage(message);
                    }
                }, getStream());
            }
        });
        thread.start();
    }

    private static void handleMessage(String message) {
        CustomFishingPlugin.get().debug("Received Redis message: " + message);
        String[] split = message.split(";");
        String server = split[0];
        if (!CFConfig.serverGroup.contains(server)) {
            return;
        }
        String action = split[1];
        CustomFishingPlugin.get().getScheduler().runTaskSync(() -> {
            switch (action) {
                case "start" -> {
                    // start competition for all the servers that connected to redis
                    CustomFishingPlugin.get().getCompetitionManager().startCompetition(split[2], true, null);
                }
                case "end" -> {
                    if (CustomFishingPlugin.get().getCompetitionManager().getOnGoingCompetition() != null)
                        CustomFishingPlugin.get().getCompetitionManager().getOnGoingCompetition().end();
                }
                case "stop" -> {
                    if (CustomFishingPlugin.get().getCompetitionManager().getOnGoingCompetition() != null)
                        CustomFishingPlugin.get().getCompetitionManager().getOnGoingCompetition().stop();
                }
            }
        }, new Location(Bukkit.getWorlds().get(0),0,0,0));
    }

    @Override
    public StorageType getStorageType() {
        return StorageType.Redis;
    }

    /**
     * Set a "change server" flag for a specified player UUID in Redis.
     *
     * @param uuid The UUID of the player.
     * @return A CompletableFuture indicating the operation's completion.
     */
    public CompletableFuture<Void> setChangeServer(UUID uuid) {
        var future = new CompletableFuture<Void>();
        plugin.getScheduler().runTaskAsync(() -> {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.setex(
                    getRedisKey("cf_server", uuid),
                    10,
                    new byte[0]
            );
        }
        future.complete(null);
            plugin.debug("Server data set for " + uuid);
        });
        return future;
    }

    /**
     * Get the "change server" flag for a specified player UUID from Redis and remove it.
     *
     * @param uuid The UUID of the player.
     * @return A CompletableFuture with a Boolean indicating whether the flag was set.
     */
    public CompletableFuture<Boolean> getChangeServer(UUID uuid) {
        var future = new CompletableFuture<Boolean>();
        plugin.getScheduler().runTaskAsync(() -> {
        try (Jedis jedis = jedisPool.getResource()) {
            byte[] key = getRedisKey("cf_server", uuid);
            if (jedis.get(key) != null) {
                jedis.del(key);
                future.complete(true);
                plugin.debug("Server data retrieved for " + uuid + "; value: true");
            } else {
                future.complete(false);
                plugin.debug("Server data retrieved for " + uuid + "; value: false");
            }
        }
        });
        return future;
    }

    /**
     * Asynchronously retrieve player data from Redis.
     *
     * @param uuid The UUID of the player.
     * @param lock Flag indicating whether to lock the data.
     * @return A CompletableFuture with an optional PlayerData.
     */
    @Override
    public CompletableFuture<Optional<PlayerData>> getPlayerData(UUID uuid, boolean lock) {
        var future = new CompletableFuture<Optional<PlayerData>>();
        plugin.getScheduler().runTaskAsync(() -> {
        try (Jedis jedis = jedisPool.getResource()) {
            byte[] key = getRedisKey("cf_data", uuid);
            byte[] data = jedis.get(key);
            jedis.del(key);
            if (data != null) {
                future.complete(Optional.of(plugin.getStorageManager().fromBytes(data)));
                plugin.debug("Redis data retrieved for " + uuid + "; normal data");
            } else {
                future.complete(Optional.empty());
                plugin.debug("Redis data retrieved for " + uuid + "; empty data");
            }
        } catch (Exception e) {
            future.complete(Optional.empty());
            LogUtils.warn("Failed to get redis data for " + uuid, e);
        }
        });
        return future;
    }

    /**
     * Asynchronously update player data in Redis.
     *
     * @param uuid       The UUID of the player.
     * @param playerData The player's data to update.
     * @param ignore     Flag indicating whether to ignore the update (not used).
     * @return A CompletableFuture indicating the update result.
     */
    @Override
    public CompletableFuture<Boolean> updatePlayerData(UUID uuid, PlayerData playerData, boolean ignore) {
        var future = new CompletableFuture<Boolean>();
        plugin.getScheduler().runTaskAsync(() -> {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.setex(
                    getRedisKey("cf_data", uuid),
                    10,
                    plugin.getStorageManager().toBytes(playerData)
            );
            future.complete(true);
            plugin.debug("Redis data set for " + uuid);
        } catch (Exception e) {
            future.complete(false);
            LogUtils.warn("Failed to set redis data for player " + uuid, e);
        }
        });
        return future;
    }

    /**
     * Get a set of unique player UUIDs from Redis (Returns an empty set).
     * This method is designed for importing and exporting so it would not actually be called.
     *
     * @param legacy Flag indicating whether to retrieve legacy data (not used).
     * @return An empty set of UUIDs.
     */
    @Override
    public Set<UUID> getUniqueUsers(boolean legacy) {
        return new HashSet<>();
    }

    /**
     * Generate a Redis key for a specified key and UUID.
     *
     * @param key  The key identifier.
     * @param uuid The UUID to include in the key.
     * @return A byte array representing the Redis key.
     */
    private byte[] getRedisKey(String key, @NotNull UUID uuid) {
        return (key + ":" + uuid).getBytes(StandardCharsets.UTF_8);
    }

    public static String getStream() {
        return STREAM;
    }

    private static boolean isRedisNewerThan5(String version) {
        String[] split = version.split("\\.");
        int major = Integer.parseInt(split[0]);
        if (major < 7) {
            LogUtils.warn(String.format("Detected that you are running an outdated Redis server. v%s. ", version));
            LogUtils.warn("It's recommended to update to avoid security vulnerabilities!");
        }
        return major >= 5;
    }

    private static String parseRedisVersion(String info) {
        for (String line : info.split("\n")) {
            if (line.startsWith("redis_version:")) {
                return line.split(":")[1];
            }
        }
        return "Unknown";
    }

    public class BlockingThreadTask {

        private boolean stopped;

        public void stop() {
            stopped = true;
        }

        public BlockingThreadTask() {
            Thread thread = new Thread(() -> {
                var map = new HashMap<String, StreamEntryID>();
                map.put(getStream(), StreamEntryID.LAST_ENTRY);
                while (!this.stopped) {
                    try {
                        var connection = getJedis();
                        if (connection != null) {
                            var messages = connection.xread(XReadParams.xReadParams().count(1).block(2000), map);
                            connection.close();
                            if (messages != null && messages.size() != 0) {
                                for (Map.Entry<String, List<StreamEntry>> message : messages) {
                                    if (message.getKey().equals(getStream())) {
                                        var value = message.getValue().get(0).getFields().get("value");
                                        handleMessage(value);
                                    }
                                }
                            }
                        } else {
                            Thread.sleep(2000);
                        }
                    } catch (Exception e) {
                        LogUtils.warn("Failed to connect redis. Try reconnecting 10s later",e);
                        try {
                            Thread.sleep(10000);
                        } catch (InterruptedException ex) {
                            this.stopped = true;
                        }
                    }
                }
            });
            thread.start();
        }
    }
}
