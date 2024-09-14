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

package net.momirealms.customfishing.bukkit.migration;

import dev.dejvokep.boostedyaml.YamlDocument;
import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.api.mechanic.config.ConfigType;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;

public class Migration {

    private final BukkitCustomFishingPlugin plugin;

    public Migration(BukkitCustomFishingPlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        // if there exists market.yml, then do migration
        File marketFile = new File(plugin.getDataFolder(), "market.yml");
        if (!marketFile.exists()) {
            return;
        }

        File configFile = new File(plugin.getDataFolder(), "config.yml");
        YamlDocument market = plugin.getConfigManager().loadData(marketFile);
        YamlDocument config = plugin.getConfigManager().loadData(configFile);

        String configVersion = config.getString("config-version");
        if (Integer.parseInt(configVersion) >= 33) {
            marketFile.delete();
            return;
        }

        for (Map.Entry<String, Object> entry : market.getStringRouteMappedValues(false).entrySet()) {
            config.set("mechanics.market." + entry.getKey(), entry.getValue());
        }
        try {
            config.save(configFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.replaceConfigPlaceholders();
        this.replaceConditions("loot-conditions");
        this.replaceConditions("game-conditions");
        this.replaceLoots();

        // delete the file at last
        marketFile.delete();
    }

    private void replaceLoots() {
        Deque<File> fileDeque = new ArrayDeque<>();
        for (ConfigType type : ConfigType.values()) {
            File typeFolder = new File(plugin.getDataFolder(), "contents" + File.separator + type.path());
            if (!typeFolder.exists()) {
                if (!typeFolder.mkdirs()) return;
                plugin.getBootstrap().saveResource("contents" + File.separator + type.path() + File.separator + "default.yml", false);
            }
            fileDeque.push(typeFolder);
            while (!fileDeque.isEmpty()) {
                File file = fileDeque.pop();
                File[] files = file.listFiles();
                if (files == null) continue;
                for (File subFile : files) {
                    if (subFile.isDirectory()) {
                        fileDeque.push(subFile);
                    } else if (subFile.isFile() && subFile.getName().endsWith(".yml")) {
                        replacePlaceholders(subFile);
                    }
                }
            }
        }
    }

    private void replacePlaceholders(File file) {
        String line;
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append(System.lineSeparator());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        };
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            writer.write(sb.toString()
                    .replace("{score}", "{score_formatted}")
                    .replace("{size}", "{size_formatted}")
                    .replace("{SIZE}", "{size}")
                    .replace("{price}", "{price_formatted}")
                    .replace("{PRICE}", "{price}")
                    .replace("{loot}", "{id}")
                    .replace("{tension}", "{progress}")
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void replaceConditions(String file) {
        String line;
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(plugin.getDataFolder(), file + ".yml")), StandardCharsets.UTF_8))) {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append(System.lineSeparator());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        File outPut = new File(plugin.getDataFolder(), file + ".yml");
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(outPut), StandardCharsets.UTF_8))) {
            writer.write(sb.toString()
                    .replace("lava-fishing: true", "in-lava: true")
                    .replace("lava-fishing: false", "in-water: true")
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void replaceConfigPlaceholders() {
        String line;
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(plugin.getDataFolder(), "config.yml")), StandardCharsets.UTF_8))) {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append(System.lineSeparator());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        File outPut = new File(plugin.getDataFolder(), "config.yml");
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(outPut), StandardCharsets.UTF_8))) {
            writer.write(sb.toString()
                    .replace("{sold-item-amount}", "{sold_item_amount}")
                    .replace("{size}", "{size_formatted}")
                    .replace("{record}", "{record_formatted}")
                    .replace("{loot}", "{id}")
                    .replace("{BASE}", "{base}")
                    .replace("{BONUS}", "{bonus}")
                    .replace("{SIZE}", "{size}"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
