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

package net.momirealms.customfishing.bukkit.storage.method.database.nosql;

import com.mongodb.*;
import com.mongodb.client.*;
import com.mongodb.client.model.*;
import com.mongodb.client.result.UpdateResult;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.api.mechanic.config.ConfigManager;
import net.momirealms.customfishing.api.storage.StorageType;
import net.momirealms.customfishing.api.storage.data.PlayerData;
import net.momirealms.customfishing.api.storage.user.UserData;
import net.momirealms.customfishing.bukkit.storage.method.AbstractStorage;
import org.bson.Document;
import org.bson.UuidRepresentation;
import org.bson.conversions.Bson;
import org.bson.types.Binary;
import org.bukkit.Bukkit;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class MongoDBProvider extends AbstractStorage {

    private MongoClient mongoClient;
    private MongoDatabase database;
    private String collectionPrefix;

    public MongoDBProvider(BukkitCustomFishingPlugin plugin) {
        super(plugin);
    }

    @Override
    public void initialize(YamlDocument config) {
        Section section = config.getSection("MongoDB");
        if (section == null) {
            plugin.getPluginLogger().warn("Failed to load database config. It seems that your config is broken. Please regenerate a new one.");
            return;
        }

        collectionPrefix = section.getString("collection-prefix", "customfishing");
        var settings = MongoClientSettings.builder().uuidRepresentation(UuidRepresentation.STANDARD);
        if (!section.getString("connection-uri", "").equals("")) {
            settings.applyConnectionString(new ConnectionString(section.getString("connection-uri", "")));
            mongoClient = MongoClients.create(settings.build());
            return;
        }

        if (section.contains("user")) {
            MongoCredential credential = MongoCredential.createCredential(
                    section.getString("user", "root"),
                    section.getString("database", "minecraft"),
                    section.getString("password", "password").toCharArray()
            );
            settings.credential(credential);
        }

        settings.applyToClusterSettings(builder -> builder.hosts(Collections.singletonList(new ServerAddress(
                section.getString("host", "localhost"),
                section.getInt("port", 27017)
        ))));
        this.mongoClient = MongoClients.create(settings.build());
        this.database = mongoClient.getDatabase(section.getString("database", "minecraft"));
    }

    @Override
    public void disable() {
        if (this.mongoClient != null) {
            this.mongoClient.close();
        }
    }

    /**
     * Get the collection name for a specific subcategory of data.
     *
     * @param value The subcategory identifier.
     * @return The full collection name including the prefix.
     */
    public String getCollectionName(String value) {
        return getCollectionPrefix() + "_" + value;
    }

    /**
     * Get the collection prefix used for MongoDB collections.
     *
     * @return The collection prefix.
     */
    public String getCollectionPrefix() {
        return collectionPrefix;
    }

    @Override
    public StorageType getStorageType() {
        return StorageType.MongoDB;
    }

    @Override
    public CompletableFuture<Optional<PlayerData>> getPlayerData(UUID uuid, boolean lock) {
        var future = new CompletableFuture<Optional<PlayerData>>();
        plugin.getScheduler().async().execute(() -> {
        MongoCollection<Document> collection = database.getCollection(getCollectionName("data"));
        Document doc = collection.find(Filters.eq("uuid", uuid)).first();
        if (doc == null) {
            if (Bukkit.getPlayer(uuid) != null) {
                if (lock) lockOrUnlockPlayerData(uuid, true);
                future.complete(Optional.of(PlayerData.empty()));
            } else {
                future.complete(Optional.empty());
            }
        } else {
            Binary binary = (Binary) doc.get("data");
            PlayerData data = plugin.getStorageManager().fromBytes(binary.getData());
            data.uuid(uuid);
            if (doc.getInteger("lock") != 0 && getCurrentSeconds() - ConfigManager.dataSaveInterval() <= doc.getInteger("lock")) {
                data.locked(true);
                future.complete(Optional.of(data));
                return;
            }
            if (lock) lockOrUnlockPlayerData(uuid, true);
            future.complete(Optional.of(data));
        }
        });
        return future;
    }

    @Override
    public CompletableFuture<Boolean> updatePlayerData(UUID uuid, PlayerData playerData, boolean unlock) {
        var future = new CompletableFuture<Boolean>();
        plugin.getScheduler().async().execute(() -> {
        MongoCollection<Document> collection = database.getCollection(getCollectionName("data"));
        try {
            Document query = new Document("uuid", uuid);
            Bson updates = Updates.combine(
                    Updates.set("lock", unlock ? 0 : getCurrentSeconds()),
                    Updates.set("data", new Binary(plugin.getStorageManager().toBytes(playerData))));
            UpdateOptions options = new UpdateOptions().upsert(true);
            UpdateResult result = collection.updateOne(query, updates, options);
            future.complete(result.wasAcknowledged());
        } catch (MongoException e) {
            future.completeExceptionally(e);
        }
        });
        return future;
    }

    @Override
    public void updateManyPlayersData(Collection<? extends UserData> users, boolean unlock) {
        MongoCollection<Document> collection = database.getCollection(getCollectionName("data"));
        try {
            int lock = unlock ? 0 : getCurrentSeconds();
            var list = users.stream().map(it -> new UpdateOneModel<Document>(
                    new Document("uuid", it.uuid()),
                    Updates.combine(
                            Updates.set("lock", lock),
                            Updates.set("data", new Binary(plugin.getStorageManager().toBytes(it.toPlayerData())))
                    ),
                    new UpdateOptions().upsert(true)
            )
            ).toList();
            if (list.isEmpty()) return;
            collection.bulkWrite(list);
        } catch (MongoException e) {
            plugin.getPluginLogger().warn("Failed to update data for online players", e);
        }
    }

    @Override
    public void lockOrUnlockPlayerData(UUID uuid, boolean lock) {
        MongoCollection<Document> collection = database.getCollection(getCollectionName("data"));
        try {
            Document query = new Document("uuid", uuid);
            Bson updates = Updates.combine(Updates.set("lock", !lock ? 0 : getCurrentSeconds()));
            UpdateOptions options = new UpdateOptions().upsert(true);
            collection.updateOne(query, updates, options);
        } catch (MongoException e) {
            plugin.getPluginLogger().warn("Failed to lock data for " + uuid, e);
        }
    }

    @Override
    public Set<UUID> getUniqueUsers() {
        // no legacy files
        Set<UUID> uuids = new HashSet<>();
        MongoCollection<Document> collection = database.getCollection(getCollectionName("data"));
        try {
            Bson projectionFields = Projections.fields(Projections.include("uuid"));
            try (MongoCursor<Document> cursor = collection.find().projection(projectionFields).iterator()) {
                while (cursor.hasNext()) {
                    uuids.add(cursor.next().get("uuid", UUID.class));
                }
            }
        } catch (MongoException e) {
            plugin.getPluginLogger().warn("Failed to get unique data.", e);
        }
        return uuids;
    }
}
