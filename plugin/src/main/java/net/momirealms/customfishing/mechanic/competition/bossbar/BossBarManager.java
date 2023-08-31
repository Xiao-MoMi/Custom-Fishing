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

package net.momirealms.customfishing.mechanic.competition.bossbar;

import net.momirealms.customfishing.api.CustomFishingPlugin;
import net.momirealms.customfishing.api.mechanic.competition.BossBarConfig;
import net.momirealms.customfishing.mechanic.competition.Competition;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class BossBarManager implements Listener {

    private static final ConcurrentHashMap<UUID, BossBarSender> senderMap = new ConcurrentHashMap<>();
    private final BossBarConfig bossBarConfig;
    private final Competition competition;

    public BossBarManager(BossBarConfig bossBarConfig, Competition competition) {
        this.bossBarConfig = bossBarConfig;
        this.competition = competition;
    }

    public void load() {
        Bukkit.getPluginManager().registerEvents(this, CustomFishingPlugin.getInstance());
        if (bossBarConfig.isShowToAll()) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                BossBarSender sender = new BossBarSender(player, bossBarConfig, competition);
                if (!sender.isVisible()) {
                    sender.show();
                }
                senderMap.put(player.getUniqueId(), sender);
            }
        }
    }

    public void unload() {
        HandlerList.unregisterAll(this);
        for (BossBarSender bossBarSender : senderMap.values()) {
            bossBarSender.hide();
        }
        senderMap.clear();
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        BossBarSender sender = senderMap.remove(player.getUniqueId());
        if (sender != null) {
            if (sender.isVisible())
                sender.hide();
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        CustomFishingPlugin.getInstance().getScheduler().runTaskAsyncLater(() -> {
            boolean hasJoined = competition.hasPlayerJoined(player);
            if ((hasJoined || bossBarConfig.isShowToAll())
                    && !senderMap.containsKey(player.getUniqueId())) {
                BossBarSender sender = new BossBarSender(player, bossBarConfig, competition);
                if (!sender.isVisible()) {
                    sender.show();
                }
                senderMap.put(player.getUniqueId(), sender);
            }
        }, 200, TimeUnit.MILLISECONDS);
    }

    public void showBossBarTo(Player player) {
        BossBarSender sender = senderMap.get(player.getUniqueId());
        if (sender == null) {
            sender = new BossBarSender(player, bossBarConfig, competition);
            senderMap.put(player.getUniqueId(), sender);
        }
        if (!sender.isVisible()) {
            sender.show();
        }
    }
}
