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

package net.momirealms.customfishing.manager;

import net.momirealms.customfishing.CustomFishing;
import net.momirealms.customfishing.data.storage.DataStorageInterface;
import net.momirealms.customfishing.data.storage.FileStorageImpl;
import net.momirealms.customfishing.data.storage.MySQLStorageImpl;
import net.momirealms.customfishing.object.Function;
import net.momirealms.customfishing.util.ConfigUtil;
import org.bukkit.configuration.file.YamlConfiguration;

public class DataManager extends Function {

    private final DataStorageInterface dataStorageInterface;
    private CustomFishing plugin;

    public DataManager(CustomFishing plugin) {
        this.plugin = plugin;
        YamlConfiguration config = ConfigUtil.getConfig("database.yml");
        if (config.getString("data-storage-method","YAML").equalsIgnoreCase("YAML")) {
            this.dataStorageInterface = new FileStorageImpl(plugin);
        } else {
            this.dataStorageInterface = new MySQLStorageImpl(plugin);
        }
        load();
    }

    public DataStorageInterface getDataStorageInterface() {
        return dataStorageInterface;
    }

    @Override
    public void load() {
        this.dataStorageInterface.initialize();
    }

    @Override
    public void unload() {
        this.dataStorageInterface.disable();
    }
}
