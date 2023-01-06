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

package net.momirealms.customfishing.util;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import net.momirealms.customfishing.CustomFishing;
import net.momirealms.customfishing.helper.Log;
import net.momirealms.customfishing.manager.ConfigManager;
import net.momirealms.customfishing.manager.MessageManager;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class ConfigUtil {

    public static YamlConfiguration getConfig(String configName) {
        File file = new File(CustomFishing.plugin.getDataFolder(), configName);
        if (!file.exists()) CustomFishing.plugin.saveResource(configName, false);
        return YamlConfiguration.loadConfiguration(file);
    }

    public static void reload() {
        ConfigManager.load();
        MessageManager.load();
        CustomFishing.plugin.getLayoutManager().unload();
        CustomFishing.plugin.getLayoutManager().load();
        CustomFishing.plugin.getLootManager().unload();
        CustomFishing.plugin.getLootManager().load();
        CustomFishing.plugin.getBonusManager().unload();
        CustomFishing.plugin.getBonusManager().load();
        CustomFishing.plugin.getFishingManager().unload();
        CustomFishing.plugin.getFishingManager().load();
        CustomFishing.plugin.getCompetitionManager().unload();
        CustomFishing.plugin.getCompetitionManager().load();
        CustomFishing.plugin.getTotemManager().unload();
        CustomFishing.plugin.getTotemManager().load();
        CustomFishing.plugin.getIntegrationManager().unload();
        CustomFishing.plugin.getIntegrationManager().load();
        CustomFishing.plugin.getSellManager().unload();
        CustomFishing.plugin.getSellManager().load();
        CustomFishing.plugin.getBagDataManager().unload();
        CustomFishing.plugin.getBagDataManager().load();
    }

    public static void update(String fileName){
        try {
            YamlDocument.create(new File(CustomFishing.plugin.getDataFolder(), fileName), CustomFishing.plugin.getResource(fileName), GeneralSettings.DEFAULT, LoaderSettings.builder().setAutoUpdate(true).build(), DumperSettings.DEFAULT, UpdaterSettings.builder().setVersioning(new BasicVersioning("config-version")).build());
        } catch (IOException e){
            Log.warn(e.getMessage());
        }
    }

    public static YamlConfiguration readData(File file) {
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                AdventureUtil.consoleMessage("<red>[CustomFishing] Failed to generate data files!</red>");
            }
        }
        return YamlConfiguration.loadConfiguration(file);
    }
}
