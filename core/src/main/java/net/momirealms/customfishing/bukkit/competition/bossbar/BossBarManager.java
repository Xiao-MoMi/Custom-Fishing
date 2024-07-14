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

package net.momirealms.customfishing.bukkit.competition.bossbar;

import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.api.mechanic.competition.info.BossBarConfig;
import net.momirealms.customfishing.bukkit.competition.Competition;
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

    /**
     * Loads the boss bar manager, registering events and showing boss bars to online players.
     */
    public void load() {
        Bukkit.getPluginManager().registerEvents(this, BukkitCustomFishingPlugin.getInstance().getBoostrap());
        if (bossBarConfig.showToAll()) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                BossBarSender sender = new BossBarSender(player, bossBarConfig, competition);
                if (!sender.isVisible()) {
                    sender.show();
                }
                senderMap.put(player.getUniqueId(), sender);
            }
        }
    }

    /**
     * Unloads the boss bar manager, unregistering events and hiding boss bars for all players.
     */
    public void unload() {
        HandlerList.unregisterAll(this);
        for (BossBarSender bossBarSender : senderMap.values()) {
            bossBarSender.hide();
        }
        senderMap.clear();
    }

    /**
     * Handles the PlayerQuitEvent to hide the boss bar for a player when they quit the game.
     *
     * @param event The PlayerQuitEvent.
     */
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        BossBarSender sender = senderMap.remove(player.getUniqueId());
        if (sender != null) {
            if (sender.isVisible())
                sender.hide();
        }
    }

    /**
     * Handles the PlayerJoinEvent to show boss bars to players when they join the game.
     *
     * @param event The PlayerJoinEvent.
     */
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        BukkitCustomFishingPlugin.getInstance().getScheduler().asyncLater(() -> {
            boolean hasJoined = competition.hasPlayerJoined(player);
            if ((hasJoined || bossBarConfig.showToAll())
                    && !senderMap.containsKey(player.getUniqueId())) {
                BossBarSender sender = new BossBarSender(player, bossBarConfig, competition);
                if (!sender.isVisible()) {
                    sender.show();
                }
                senderMap.put(player.getUniqueId(), sender);
            }
        }, 200, TimeUnit.MILLISECONDS);
    }

    /**
     * Shows a boss bar to a specific player.
     *
     * @param player The player to show the boss bar to.
     */
    public void showBossBarTo(Player player) {
        BossBarSender sender = senderMap.get(player.getUniqueId());
        if (sender == null) {
            sender = new BossBarSender(player, bossBarConfig, competition);
            senderMap.put(player.getUniqueId(), sender);
        }
        if (!sender.isVisible())
            sender.show();
    }
}
