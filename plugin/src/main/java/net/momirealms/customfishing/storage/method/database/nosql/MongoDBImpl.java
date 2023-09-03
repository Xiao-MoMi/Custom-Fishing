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

import com.mongodb.*;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.InsertOneResult;
import net.momirealms.customfishing.api.CustomFishingPlugin;
import net.momirealms.customfishing.api.data.PlayerData;
import net.momirealms.customfishing.api.data.StorageType;
import net.momirealms.customfishing.api.util.LogUtils;
import net.momirealms.customfishing.storage.method.AbstractStorage;
import org.bson.Document;
import org.bson.UuidRepresentation;
import org.bson.types.Binary;
import org.bson.types.ObjectId;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class MongoDBImpl extends AbstractStorage {

    private MongoClient mongoClient;
    private MongoDatabase database;
    private String collectionPrefix;

    public MongoDBImpl(CustomFishingPlugin plugin) {
        super(plugin);
    }

    @Override
    public void initialize() {
        YamlConfiguration config = plugin.getConfig("database.yml");
        ConfigurationSection section = config.getConfigurationSection("MongoDB");
        if (section == null) {
            LogUtils.warn("Failed to load database config. It seems that your config is broken. Please regenerate a new one.");
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

    public String getCollectionName(String sub) {
        return getCollectionPrefix() + "_" + sub;
    }

    public String getCollectionPrefix() {
        return collectionPrefix;
    }

    @Override
    public StorageType getStorageType() {
        return StorageType.MongoDB;
    }

    @Override
    public CompletableFuture<Optional<PlayerData>> getPlayerData(UUID uuid, boolean force) {
        var future = new CompletableFuture<Optional<PlayerData>>();
        plugin.getScheduler().runTaskAsync(() -> {
        MongoCollection<Document> collection = database.getCollection("movies");
        Document doc = collection.find(Filters.eq("uuid", uuid)).first();
        if (doc == null) {
            if (Bukkit.getPlayer(uuid) != null) {
                future.complete(Optional.of(PlayerData.empty()));
            } else {
                future.complete(Optional.of(PlayerData.NEVER_PLAYED));
            }
        } else {
            if (!force && doc.getInteger("lock") != 0) {
                future.complete(Optional.empty());
                return;
            }
            Binary binary = (Binary) doc.get("data");
            future.complete(Optional.of(plugin.getStorageManager().fromBytes(binary.getData())));
        }
        });
        return future;
    }

    @Override
    public CompletableFuture<Boolean> setPlayData(UUID uuid, PlayerData playerData, boolean unlock) {
        var future = new CompletableFuture<Boolean>();
        plugin.getScheduler().runTaskAsync(() -> {
        MongoCollection<Document> collection = database.getCollection(getCollectionName("data"));
        try {
            InsertOneResult result = collection.insertOne(new Document()
                    .append("_id", new ObjectId())
                    .append("uuid", uuid)
                    .append("lock", unlock ? 0 : getCurrentSeconds())
                    .append("data", new Binary(plugin.getStorageManager().toBytes(playerData))));
            future.complete(result.wasAcknowledged());
        } catch (MongoException e) {
            future.completeExceptionally(e);
        }
        });
        return future;
    }
}
