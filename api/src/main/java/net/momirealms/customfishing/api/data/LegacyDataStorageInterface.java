package net.momirealms.customfishing.api.data;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface LegacyDataStorageInterface extends DataStorageInterface {

    CompletableFuture<Optional<PlayerData>> getLegacyPlayerData(UUID uuid);
}
