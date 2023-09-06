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
import net.momirealms.customfishing.api.scheduler.CancellableTask;
import net.momirealms.customfishing.api.util.LogUtils;
import net.momirealms.customfishing.storage.method.AbstractStorage;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import redis.clients.jedis.*;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisException;
import redis.clients.jedis.resps.Tuple;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class RedisManager extends AbstractStorage {

    private static RedisManager instance;
    private JedisPool jedisPool;
    private String password;
    private int port;
    private String host;
    private JedisPoolConfig jedisPoolConfig;
    private boolean useSSL;
    private Jedis subscriber;
    private JedisPubSub pubSub;
    private CancellableTask checkConnectionTask;

    public RedisManager(CustomFishingPlugin plugin) {
        super(plugin);
        instance = this;
    }

    public static RedisManager getInstance() {
        return instance;
    }

    public Jedis getJedis() {
        return jedisPool.getResource();
    }

    @Override
    public void initialize() {
        YamlConfiguration config = plugin.getConfig("database.yml");
        ConfigurationSection section = config.getConfigurationSection("Redis");
        if (section == null) {
            LogUtils.warn("Failed to load database config. It seems that your config is broken. Please regenerate a new one.");
            return;
        }

        jedisPoolConfig = new JedisPoolConfig();
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
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.ping();
        } catch (JedisException e) {
            LogUtils.warn("Failed to connect redis.", e);
        }

        this.checkConnectionTask = plugin.getScheduler().runTaskAsyncTimer(() -> {
            try {
                pubSub.ping();
            } catch (JedisConnectionException e) {
                subscribe();
            }
        }, 30, 30, TimeUnit.SECONDS);
    }

    @Override
    public void disable() {
        this.removeServerPlayers(plugin.getStorageManager().getUniqueID());
        if (checkConnectionTask != null && !checkConnectionTask.isCancelled())
            checkConnectionTask.cancel();
        if (jedisPool != null && !jedisPool.isClosed())
            jedisPool.close();
        if (pubSub != null && !pubSub.isSubscribed())
            pubSub.unsubscribe();
        if (subscriber != null)
            subscriber.close();
    }

    public void sendRedisMessage(@NotNull String channel, @NotNull String message) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.publish(channel, message);
        }
    }

    private void subscribe() {
        new Thread(() -> {
        try (final Jedis jedis = password.isBlank() ?
            new Jedis(host, port, 0, useSSL) :
            new Jedis(host, port, DefaultJedisClientConfig
                    .builder()
                    .password(password)
                    .timeoutMillis(0)
                    .ssl(useSSL)
                    .build())
        ) {
            subscriber = jedis;
            subscriber.connect();
            pubSub = new JedisPubSub() {
                @Override
                public void onMessage(String channel, String message) {
                    if (!channel.equals("cf_competition")) {
                        return;
                    }
                    String[] split = message.split(";");
                    String action = split[0];
                    switch (action) {
                        case "start" -> {
                            // start competition for all the servers that connected to redis
                            plugin.getCompetitionManager().startCompetition(split[1], true, false);
                        }
                        case "end" -> {
                            if (plugin.getCompetitionManager().getOnGoingCompetition() != null)
                                plugin.getCompetitionManager().getOnGoingCompetition().end();
                        }
                        case "stop" -> {
                            if (plugin.getCompetitionManager().getOnGoingCompetition() != null)
                                plugin.getCompetitionManager().getOnGoingCompetition().stop();
                        }
                    }
                }
            };
            subscriber.subscribe(pubSub, "cf_competition");
        }
        }).start();
    }

    @Override
    public StorageType getStorageType() {
        return StorageType.Redis;
    }

    public CompletableFuture<Integer> getPlayerCount() {
        var future = new CompletableFuture<Integer>();
        plugin.getScheduler().runTaskAsync(() -> {
            int players = 0;
            try (Jedis jedis = jedisPool.getResource()) {
                var list = jedis.zrangeWithScores("cf_players",0, -1);
                for (Tuple tuple : list) {
                    players += (int) tuple.getScore();
                }
            }
            future.complete(players);
        });
        return future;
    }

    public CompletableFuture<Void> setServerPlayers(int amount, String unique) {
        var future = new CompletableFuture<Void>();
        plugin.getScheduler().runTaskAsync(() -> {
            try (Jedis jedis = jedisPool.getResource()) {
                jedis.zadd("cf_players", amount, unique);
            }
            future.complete(null);
        });
        return future;
    }

    public void removeServerPlayers(String unique) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.zrem("cf_players", unique);
        }
    }

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
        });
        return future;
    }

    public CompletableFuture<Boolean> getChangeServer(UUID uuid) {
        var future = new CompletableFuture<Boolean>();
        plugin.getScheduler().runTaskAsync(() -> {
        try (Jedis jedis = jedisPool.getResource()) {
            byte[] key = getRedisKey("cf_server", uuid);
            if (jedis.get(key) != null) {
                jedis.del(key);
                future.complete(true);
            } else {
                future.complete(false);
            }
        }
        });
        return future;
    }

    @Override
    public CompletableFuture<Optional<PlayerData>> getPlayerData(UUID uuid, boolean ignore) {
        var future = new  CompletableFuture<Optional<PlayerData>>();
        plugin.getScheduler().runTaskAsync(() -> {
        try (Jedis jedis = jedisPool.getResource()) {
            byte[] key = getRedisKey("cf_data", uuid);
            byte[] data = jedis.get(key);
            jedis.del(key);
            if (data != null) {
                future.complete(Optional.of(plugin.getStorageManager().fromBytes(data)));
            } else {
                future.complete(Optional.empty());
            }
        }
        });
        return future;
    }

    @Override
    public CompletableFuture<Boolean> savePlayerData(UUID uuid, PlayerData playerData, boolean ignore) {
        var future = new  CompletableFuture<Boolean>();
        plugin.getScheduler().runTaskAsync(() -> {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.setex(
                    getRedisKey("cf_data", uuid),
                    10,
                    plugin.getStorageManager().toBytes(playerData)
            );
        }
        });
        return future;
    }

    private byte[] getRedisKey(String key, @NotNull UUID uuid) {
        return (key + ":" + uuid).getBytes(StandardCharsets.UTF_8);
    }
}
