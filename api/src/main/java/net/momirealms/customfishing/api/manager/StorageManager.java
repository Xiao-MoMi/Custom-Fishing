package net.momirealms.customfishing.api.manager;

import net.momirealms.customfishing.api.data.DataStorageInterface;
import net.momirealms.customfishing.api.data.PlayerData;
import net.momirealms.customfishing.api.data.user.OfflineUser;
import net.momirealms.customfishing.api.data.user.OnlineUser;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface StorageManager {

    /**
     * Get server unique id
     * @return id
     */
    String getUniqueID();

    /**
     * Get online user's data
     * @param uuid uuid
     * @return online user data
     */
    OnlineUser getOnlineUser(UUID uuid);

    /**
     * Get an offline user's data
     * force reading would ignore the database lock
     * Otherwise it would return Optional.empty() if data is locked
     * It an offline user never played the server, its name would equal "" (empty string)
     * @param uuid uuid
     * @param force force
     * @return offline user data
     */
    CompletableFuture<Optional<OfflineUser>> getOfflineUser(UUID uuid, boolean force);

    /**
     * Get all the players in servers that connected to the same redis server
     * @return amount
     */
    CompletableFuture<Integer> getRedisPlayerCount();

    /**
     * Get plugin data source
     * @return data source
     */
    DataStorageInterface getDataSource();

    boolean isRedisEnabled();

    byte[] toBytes(@NotNull PlayerData data);

    @NotNull String toJson(@NotNull PlayerData data);

    @NotNull PlayerData fromBytes(byte[] data);
}
