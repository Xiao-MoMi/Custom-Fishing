/*
 *  Copyright (C) <2024> <XiaoMoMi>
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

package net.momirealms.customfishing.bukkit.command.feature;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.kyori.adventure.text.Component;
import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.api.storage.DataStorageProvider;
import net.momirealms.customfishing.api.storage.data.PlayerData;
import net.momirealms.customfishing.bukkit.command.BukkitCommandFeature;
import net.momirealms.customfishing.common.command.CustomFishingCommandManager;
import net.momirealms.customfishing.common.locale.MessageConstants;
import net.momirealms.customfishing.common.util.CompletableFutures;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.parser.standard.StringParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPInputStream;

public class ImportDataCommand extends BukkitCommandFeature<CommandSender> {

    public ImportDataCommand(CustomFishingCommandManager<CommandSender> commandManager) {
        super(commandManager);
    }

    @Override
    public Command.Builder<? extends CommandSender> assembleCommand(CommandManager<CommandSender> manager, Command.Builder<CommandSender> builder) {
        return builder
                .senderType(ConsoleCommandSender.class)
                .flag(manager.flagBuilder("silent").withAliases("s").build())
                .required("file", StringParser.greedyFlagYieldingStringParser())
                .handler(context -> {
                    if (!Bukkit.getOnlinePlayers().isEmpty()) {
                        handleFeedback(context, MessageConstants.COMMAND_DATA_IMPORT_FAILURE_PLAYER_ONLINE);
                        return;
                    }
                    String fileName = context.get("file");
                    BukkitCustomFishingPlugin plugin = BukkitCustomFishingPlugin.getInstance();
                    File file = new File(plugin.getDataFolder(), fileName);
                    if (!file.exists()) {
                        handleFeedback(context, MessageConstants.COMMAND_DATA_IMPORT_FAILURE_NOT_EXISTS);
                        return;
                    }
                    if (!file.getName().endsWith(".json.gz")) {
                        handleFeedback(context, MessageConstants.COMMAND_DATA_IMPORT_FAILURE_INVALID_FILE);
                        return;
                    }

                    handleFeedback(context, MessageConstants.COMMAND_DATA_IMPORT_START);
                    plugin.getScheduler().async().execute(() -> {

                        JsonObject data;
                        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(Files.newInputStream(file.toPath())), StandardCharsets.UTF_8))) {
                            data = new GsonBuilder().disableHtmlEscaping().create().fromJson(reader, JsonObject.class);
                        } catch (IOException e) {
                            throw new RuntimeException("Unexpected issue: ", e);
                        }

                        DataStorageProvider storageProvider = plugin.getStorageManager().getDataSource();
                        var entrySet = data.entrySet();
                        int amount = entrySet.size();
                        AtomicInteger userCount = new AtomicInteger(0);
                        Set<CompletableFuture<Void>> futures = new HashSet<>();

                        for (Map.Entry<String, JsonElement> entry : entrySet) {
                            UUID uuid = UUID.fromString(entry.getKey());
                            if (entry.getValue() instanceof JsonPrimitive primitive) {
                                PlayerData playerData = plugin.getStorageManager().fromJson(primitive.getAsString());
                                futures.add(storageProvider.updateOrInsertPlayerData(uuid, playerData, true).thenAccept(it -> userCount.incrementAndGet()));
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
                                handleFeedback(context, MessageConstants.COMMAND_DATA_IMPORT_PROGRESS, Component.text(userCount.get()), Component.text(amount));
                                continue;
                            }
                            break;
                        }

                        handleFeedback(context, MessageConstants.COMMAND_DATA_IMPORT_SUCCESS);
                    });
                });
    }

    @Override
    public String getFeatureID() {
        return "data_import";
    }
}
