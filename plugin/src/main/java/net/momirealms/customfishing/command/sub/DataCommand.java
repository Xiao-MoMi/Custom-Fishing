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

package net.momirealms.customfishing.command.sub;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.arguments.UUIDArgument;
import net.momirealms.customfishing.adventure.AdventureManagerImpl;
import net.momirealms.customfishing.api.CustomFishingPlugin;
import net.momirealms.customfishing.api.data.DataStorageInterface;
import net.momirealms.customfishing.api.data.LegacyDataStorageInterface;
import net.momirealms.customfishing.api.data.PlayerData;
import net.momirealms.customfishing.api.util.LogUtils;
import net.momirealms.customfishing.storage.method.database.sql.MariaDBImpl;
import net.momirealms.customfishing.storage.method.database.sql.MySQLImpl;
import net.momirealms.customfishing.storage.method.file.YAMLImpl;
import net.momirealms.customfishing.util.CompletableFutures;
import org.bukkit.Bukkit;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class DataCommand {

    public static DataCommand INSTANCE = new DataCommand();

    public CommandAPICommand getDataCommand() {
        return new CommandAPICommand("data")
                .withSubcommands(
                    getExportLegacyCommand(),
                    getExportCommand(),
                    getImportCommand(),
                    getUnlockCommand()
                );
    }

    private CommandAPICommand getUnlockCommand() {
        return new CommandAPICommand("unlock")
                .withArguments(new UUIDArgument("uuid"))
                .executes((sender, args) -> {
                   UUID uuid = (UUID) args.get("uuid");
                   CustomFishingPlugin.get().getStorageManager().getDataSource().lockOrUnlockPlayerData(uuid, false);
                   AdventureManagerImpl.getInstance().sendMessageWithPrefix(sender, "Successfully unlocked.");
                });
    }

    @SuppressWarnings("DuplicatedCode")
    private CommandAPICommand getExportLegacyCommand() {
        return new CommandAPICommand("export-legacy")
                .withArguments(new StringArgument("method")
                .replaceSuggestions(ArgumentSuggestions.strings("MySQL", "MariaDB", "YAML")))
                .executes((sender, args) -> {
                    String arg = (String) args.get("method");
                    if (arg == null) return;
                    CustomFishingPlugin plugin = CustomFishingPlugin.get();
                    plugin.getScheduler().runTaskAsync(() -> {

                        AdventureManagerImpl.getInstance().sendMessageWithPrefix(sender, "Starting <aqua>export</aqua>.");

                        LegacyDataStorageInterface dataStorageInterface;
                        switch (arg) {
                            case "MySQL" -> dataStorageInterface = new MySQLImpl(plugin);
                            case "MariaDB" -> dataStorageInterface = new MariaDBImpl(plugin);
                            case "YAML" -> dataStorageInterface = new YAMLImpl(plugin);
                            default -> {
                                AdventureManagerImpl.getInstance().sendMessageWithPrefix(sender, "No such legacy storage method.");
                                return;
                            }
                        }

                        dataStorageInterface.initialize();
                        Set<UUID> uuids = dataStorageInterface.getUniqueUsers(true);
                        Set<CompletableFuture<Void>> futures = new HashSet<>();
                        AtomicInteger userCount = new AtomicInteger(0);
                        Map<UUID, String> out = Collections.synchronizedMap(new TreeMap<>());

                        for (UUID uuid : uuids) {
                            futures.add(dataStorageInterface.getLegacyPlayerData(uuid).thenAccept(it -> {
                                if (it.isPresent()) {
                                    out.put(uuid, plugin.getStorageManager().toJson(it.get()));
                                    userCount.incrementAndGet();
                                }
                            }));
                        }

                        CompletableFuture<Void> overallFuture = CompletableFutures.allOf(futures);

                        while (true) {
                            try {
                                overallFuture.get(3, TimeUnit.SECONDS);
                            } catch (InterruptedException | ExecutionException e) {
                                e.printStackTrace();
                                break;
                            } catch (TimeoutException e) {
                                LogUtils.info("Progress: " + userCount.get() + "/" + uuids.size());
                                continue;
                            }
                            break;
                        }

                        JsonObject outJson = new JsonObject();
                        for (Map.Entry<UUID, String> entry : out.entrySet()) {
                            outJson.addProperty(entry.getKey().toString(), entry.getValue());
                        }
                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm");
                        String formattedDate = formatter.format(new Date());
                        File outFile = new File(plugin.getDataFolder(), "exported-" + formattedDate + ".json.gz");
                        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(Files.newOutputStream(outFile.toPath())), StandardCharsets.UTF_8))) {
                            new GsonBuilder().disableHtmlEscaping().create().toJson(outJson, writer);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        dataStorageInterface.disable();

                        AdventureManagerImpl.getInstance().sendMessageWithPrefix(sender, "Completed.");
                    });
                });
    }

    @SuppressWarnings("DuplicatedCode")
    private CommandAPICommand getExportCommand() {
        return new CommandAPICommand("export")
                .executesConsole((sender, args) -> {
                    if (Bukkit.getOnlinePlayers().size() != 0) {
                        AdventureManagerImpl.getInstance().sendMessageWithPrefix(sender, "Please kick all the players before exporting. Otherwise the cache will be inconsistent with data, resulting in the backup file not being up to date.");
                        return;
                    }

                    CustomFishingPlugin plugin = CustomFishingPlugin.get();
                    plugin.getScheduler().runTaskAsync(() -> {

                        AdventureManagerImpl.getInstance().sendMessageWithPrefix(sender, "Starting <aqua>export</aqua>.");
                        DataStorageInterface dataStorageInterface = plugin.getStorageManager().getDataSource();

                        Set<UUID> uuids = dataStorageInterface.getUniqueUsers(false);
                        Set<CompletableFuture<Void>> futures = new HashSet<>();
                        AtomicInteger userCount = new AtomicInteger(0);
                        Map<UUID, String> out = Collections.synchronizedMap(new TreeMap<>());

                        int amount = uuids.size();
                        for (UUID uuid : uuids) {
                            futures.add(dataStorageInterface.getPlayerData(uuid, false).thenAccept(it -> {
                                if (it.isPresent()) {
                                    out.put(uuid, plugin.getStorageManager().toJson(it.get()));
                                    userCount.incrementAndGet();
                                }
                            }));
                        }

                        CompletableFuture<Void> overallFuture = CompletableFutures.allOf(futures);

                        while (true) {
                            try {
                                overallFuture.get(3, TimeUnit.SECONDS);
                            } catch (InterruptedException | ExecutionException e) {
                                e.printStackTrace();
                                break;
                            } catch (TimeoutException e) {
                                LogUtils.info("Progress: " + userCount.get() + "/" + amount);
                                continue;
                            }
                            break;
                        }

                        JsonObject outJson = new JsonObject();
                        for (Map.Entry<UUID, String> entry : out.entrySet()) {
                            outJson.addProperty(entry.getKey().toString(), entry.getValue());
                        }
                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm");
                        String formattedDate = formatter.format(new Date());
                        File outFile = new File(plugin.getDataFolder(), "exported-" + formattedDate + ".json.gz");
                        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(Files.newOutputStream(outFile.toPath())), StandardCharsets.UTF_8))) {
                            new GsonBuilder().disableHtmlEscaping().create().toJson(outJson, writer);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        AdventureManagerImpl.getInstance().sendMessageWithPrefix(sender, "Completed.");
                    });
                });
    }

    @SuppressWarnings("DuplicatedCode")
    private CommandAPICommand getImportCommand() {
        return new CommandAPICommand("import")
                .withArguments(new StringArgument("file"))
                .executesConsole((sender, args) -> {
                    if (Bukkit.getOnlinePlayers().size() != 0) {
                        AdventureManagerImpl.getInstance().sendMessageWithPrefix(sender, "Please kick all the players before importing. Otherwise the cache will be inconsistent with data.");
                        return;
                    }

                    String fileName = (String) args.get("file");
                    if (fileName == null) return;
                    CustomFishingPlugin plugin = CustomFishingPlugin.get();

                    File file = new File(plugin.getDataFolder(), fileName);
                    if (!file.exists()) {
                        AdventureManagerImpl.getInstance().sendMessageWithPrefix(sender, "File not exists.");
                        return;
                    }
                    if (!file.getName().endsWith(".json.gz")) {
                        AdventureManagerImpl.getInstance().sendMessageWithPrefix(sender, "Invalid file.");
                        return;
                    }

                    plugin.getScheduler().runTaskAsync(() -> {

                        AdventureManagerImpl.getInstance().sendMessageWithPrefix(sender, "Starting <aqua>import</aqua>.");

                        JsonObject data;
                        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(Files.newInputStream(file.toPath())), StandardCharsets.UTF_8))) {
                            data = new GsonBuilder().disableHtmlEscaping().create().fromJson(reader, JsonObject.class);
                        } catch (IOException e) {
                            AdventureManagerImpl.getInstance().sendMessageWithPrefix(sender, "Error occurred when reading the backup file.");
                            e.printStackTrace();
                            return;
                        }

                        DataStorageInterface dataStorageInterface = plugin.getStorageManager().getDataSource();
                        var entrySet = data.entrySet();
                        int amount = entrySet.size();
                        AtomicInteger userCount = new AtomicInteger(0);
                        Set<CompletableFuture<Void>> futures = new HashSet<>();

                        for (Map.Entry<String, JsonElement> entry : entrySet) {
                            UUID uuid = UUID.fromString(entry.getKey());
                            if (entry.getValue() instanceof JsonPrimitive primitive) {
                                PlayerData playerData = plugin.getStorageManager().fromJson(primitive.getAsString());
                                futures.add(dataStorageInterface.updateOrInsertPlayerData(uuid, playerData, true).thenAccept(it -> userCount.incrementAndGet()));
                            }
                        }

                        CompletableFuture<Void> overallFuture = CompletableFutures.allOf(futures);

                        while (true) {
                            try {
                                overallFuture.get(3, TimeUnit.SECONDS);
                            } catch (InterruptedException | ExecutionException e) {
                                e.printStackTrace();
                                break;
                            } catch (TimeoutException e) {
                                LogUtils.info("Progress: " + userCount.get() + "/" + amount);
                                continue;
                            }
                            break;
                        }

                        AdventureManagerImpl.getInstance().sendMessageWithPrefix(sender, "Completed.");
                    });
                });
    }
}
