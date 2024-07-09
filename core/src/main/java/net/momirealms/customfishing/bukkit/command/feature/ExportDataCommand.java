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

package net.momirealms.customfishing.bukkit.command.feature;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.kyori.adventure.text.Component;
import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.api.storage.DataStorageProvider;
import net.momirealms.customfishing.bukkit.command.BukkitCommandFeature;
import net.momirealms.customfishing.common.command.CustomFishingCommandManager;
import net.momirealms.customfishing.common.locale.MessageConstants;
import net.momirealms.customfishing.common.util.CompletableFutures;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPOutputStream;

public class ExportDataCommand extends BukkitCommandFeature<CommandSender> {

    public ExportDataCommand(CustomFishingCommandManager<CommandSender> commandManager) {
        super(commandManager);
    }

    @Override
    public Command.Builder<? extends CommandSender> assembleCommand(CommandManager<CommandSender> manager, Command.Builder<CommandSender> builder) {
        return builder
                .senderType(ConsoleCommandSender.class)
                .flag(manager.flagBuilder("silent").withAliases("s").build())
                .handler(context -> {
                    if (!Bukkit.getOnlinePlayers().isEmpty()) {
                        handleFeedback(context, MessageConstants.COMMAND_DATA_EXPORT_FAILURE_PLAYER_ONLINE);
                        return;
                    }

                    BukkitCustomFishingPlugin plugin = BukkitCustomFishingPlugin.getInstance();
                    handleFeedback(context, MessageConstants.COMMAND_DATA_EXPORT_START);
                    plugin.getScheduler().async().execute(() -> {

                        DataStorageProvider storageProvider = plugin.getStorageManager().getDataSource();

                        Set<UUID> uuids = storageProvider.getUniqueUsers();
                        Set<CompletableFuture<Void>> futures = new HashSet<>();
                        AtomicInteger userCount = new AtomicInteger(0);
                        Map<UUID, String> out = Collections.synchronizedMap(new TreeMap<>());

                        int amount = uuids.size();
                        for (UUID uuid : uuids) {
                            futures.add(storageProvider.getPlayerData(uuid, false).thenAccept(it -> {
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
                                handleFeedback(context, MessageConstants.COMMAND_DATA_EXPORT_PROGRESS, Component.text(userCount.get()), Component.text(amount));
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
                            throw new RuntimeException("Unexpected issue: ", e);
                        }

                        handleFeedback(context, MessageConstants.COMMAND_DATA_EXPORT_SUCCESS);
                    });
                });
    }

    @Override
    public String getFeatureID() {
        return "data_export";
    }
}
