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

package net.momirealms.customfishing.mechanic.bag;

import net.momirealms.customfishing.CustomFishingPluginImpl;
import net.momirealms.customfishing.api.CustomFishingPlugin;
import net.momirealms.customfishing.api.manager.BagManager;
import net.momirealms.customfishing.api.mechanic.bag.FishingBagHolder;
import net.momirealms.customfishing.api.util.LogUtils;
import net.momirealms.customfishing.setting.Config;
import org.bukkit.inventory.Inventory;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BagManagerImpl implements BagManager {

    private final CustomFishingPlugin plugin;
    private final ConcurrentHashMap<UUID, FishingBagHolder> bagMap;

    public BagManagerImpl(CustomFishingPluginImpl plugin) {
        this.plugin = plugin;
        this.bagMap = new ConcurrentHashMap<>();
    }

    @Override
    public boolean isBagEnabled() {
        return Config.enableFishingBag;
    }

    public void load() {

    }

    public void unload() {

    }

    public void disable() {
        unload();
    }

    @Override
    public Inventory getOnlineBagInventory(UUID uuid) {
        var onlinePlayer = plugin.getStorageManager().getOnlineUser(uuid);
        if (onlinePlayer == null) {
            LogUtils.warn("Player " + uuid + "'s bag data is not loaded.");
            return null;
        }
        return onlinePlayer.getHolder().getInventory();
    }
}
