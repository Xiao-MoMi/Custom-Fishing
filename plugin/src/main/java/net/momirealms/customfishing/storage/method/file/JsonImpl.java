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

package net.momirealms.customfishing.storage.method.file;

import com.google.gson.Gson;
import net.momirealms.customfishing.api.CustomFishingPlugin;
import net.momirealms.customfishing.api.data.PlayerData;
import net.momirealms.customfishing.api.data.StorageType;
import net.momirealms.customfishing.storage.method.AbstractStorage;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class JsonImpl extends AbstractStorage {

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public JsonImpl(CustomFishingPlugin plugin) {
        super(plugin);
        File folder = new File(plugin.getDataFolder(), "data");
        if (!folder.exists()) folder.mkdirs();
    }

    @Override
    public StorageType getStorageType() {
        return StorageType.JSON;
    }

    @Override
    public CompletableFuture<Optional<PlayerData>> getPlayerData(UUID uuid, boolean lock) {
        File file = getPlayerDataFile(uuid);
        PlayerData playerData;
        if (file.exists()) {
            playerData = readFromJsonFile(file, PlayerData.class);
        } else if (Bukkit.getPlayer(uuid) != null) {
            playerData = PlayerData.empty();
        } else {
            playerData = null;
        }
        return CompletableFuture.completedFuture(Optional.ofNullable(playerData));
    }

    @Override
    public CompletableFuture<Boolean> updatePlayerData(UUID uuid, PlayerData playerData, boolean ignore) {
        this.saveToJsonFile(playerData, getPlayerDataFile(uuid));
        return CompletableFuture.completedFuture(true);
    }

    public File getPlayerDataFile(UUID uuid) {
        return new File(plugin.getDataFolder(), "data" + File.separator + uuid + ".json");
    }

    public void saveToJsonFile(Object obj, File filepath) {
        Gson gson = new Gson();
        try (FileWriter file = new FileWriter(filepath)) {
            gson.toJson(obj, file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public <T> T readFromJsonFile(File file, Class<T> classOfT) {
        Gson gson = new Gson();
        String jsonContent = new String(readFileToByteArray(file), StandardCharsets.UTF_8);
        return gson.fromJson(jsonContent, classOfT);
    }

    public byte[] readFileToByteArray(File file) {
        byte[] fileBytes = new byte[(int) file.length()];
        try (FileInputStream fis = new FileInputStream(file)) {
            fis.read(fileBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileBytes;
    }

    @Override
    public Set<UUID> getUniqueUsers(boolean legacy) {
        // No legacy files
        File folder = new File(plugin.getDataFolder(), "data");
        Set<UUID> uuids = new HashSet<>();
        if (folder.exists()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    uuids.add(UUID.fromString(file.getName().substring(file.getName().length() - 5)));
                }
            }
        }
        return uuids;
    }
}
