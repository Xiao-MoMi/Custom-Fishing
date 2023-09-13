package net.momirealms.customfishing.command.sub;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.StringArgument;
import net.momirealms.customfishing.adventure.AdventureManagerImpl;
import net.momirealms.customfishing.api.CustomFishingPlugin;
import net.momirealms.customfishing.api.data.LegacyDataStorageInterface;
import net.momirealms.customfishing.api.util.LogUtils;
import net.momirealms.customfishing.storage.method.database.sql.MariaDBImpl;
import net.momirealms.customfishing.storage.method.database.sql.MySQLImpl;
import net.momirealms.customfishing.storage.method.file.YAMLImpl;
import net.momirealms.customfishing.util.CompletableFutures;

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

public class DataCommand {

    public static DataCommand INSTANCE = new DataCommand();

    public CommandAPICommand getDataCommand() {
        return new CommandAPICommand("data")
                .withSubcommands(
                    getExportLegacyCommand()
                );
    }

    public CommandAPICommand getExportLegacyCommand() {
        return new CommandAPICommand("export-legacy")
                .withArguments(new StringArgument("method")
                .replaceSuggestions(ArgumentSuggestions.strings("MySQL", "MariaDB", "YAML")))
                .executes((sender, args) -> {
                    String arg = (String) args.get("method");
                    if (arg == null) return;
                    CustomFishingPlugin plugin = CustomFishingPlugin.get();
                    plugin.getScheduler().runTaskAsync(() -> {
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

                        plugin.getScheduler().runTaskAsyncLater(dataStorageInterface::disable, 1, TimeUnit.SECONDS);

                        AdventureManagerImpl.getInstance().sendMessageWithPrefix(sender, "Finished.");
                    });
                });
    }
}
