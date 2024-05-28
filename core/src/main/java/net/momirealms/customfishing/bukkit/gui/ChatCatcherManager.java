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

package net.momirealms.customfishing.bukkit.gui;

import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.common.util.Pair;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ChatCatcherManager implements Listener {

    private final BukkitCustomFishingPlugin plugin;
    private final ConcurrentHashMap<UUID, Pair<String, SectionPage>> pageMap;

    public ChatCatcherManager(BukkitCustomFishingPlugin plugin) {
        this.pageMap = new ConcurrentHashMap<>();
        this.plugin = plugin;
    }

    public void load() {
        Bukkit.getPluginManager().registerEvents(this, plugin.getBoostrap());
    }

    public void unload() {
        this.pageMap.clear();
        HandlerList.unregisterAll(this);
    }

    public void disable() {
        unload();
    }

    public void catchMessage(Player player, String key, SectionPage page) {
        this.pageMap.put(player.getUniqueId(), Pair.of(key, page));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        pageMap.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        final Player player = event.getPlayer();
        var uuid = player.getUniqueId();
        var pair = pageMap.remove(uuid);
        if (pair == null) return;
        event.setCancelled(true);
        plugin.getScheduler().sync().run(() -> {
            pair.right().getSection().set(pair.left(), event.getMessage());
            pair.right().reOpen();
        }, player.getLocation());
    }
}
