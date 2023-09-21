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

package net.momirealms.customfishing.mechanic.competition.actionbar;

import net.momirealms.customfishing.api.CustomFishingPlugin;
import net.momirealms.customfishing.api.mechanic.competition.ActionBarConfig;
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

public class ActionBarManager implements Listener {

    private static final ConcurrentHashMap<UUID, ActionBarSender> senderMap = new ConcurrentHashMap<>();
    private final ActionBarConfig actionBarConfig;
    private final Competition competition;

    public ActionBarManager(ActionBarConfig actionBarConfig, Competition competition) {
        this.actionBarConfig = actionBarConfig;
        this.competition = competition;
    }

    /**
     * Loads the ActionBar manager, registering events and showing ActionBar messages to online players.
     */
    public void load() {
        Bukkit.getPluginManager().registerEvents(this, CustomFishingPlugin.getInstance());
        if (actionBarConfig.isShowToAll()) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                ActionBarSender sender = new ActionBarSender(player, actionBarConfig, competition);
                if (!sender.isVisible()) {
                    sender.show();
                }
                senderMap.put(player.getUniqueId(), sender);
            }
        }
    }

    /**
     * Unloads the ActionBar manager, unregistering events and hiding ActionBar messages for all players.
     */
    public void unload() {
        HandlerList.unregisterAll(this);
        for (ActionBarSender ActionBarSender : senderMap.values()) {
            ActionBarSender.hide();
        }
        senderMap.clear();
    }

    /**
     * Handles the PlayerQuitEvent to hide ActionBar messages for a player when they quit the game.
     *
     * @param event The PlayerQuitEvent.
     */
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        ActionBarSender sender = senderMap.remove(player.getUniqueId());
        if (sender != null) {
            if (sender.isVisible())
                sender.hide();
        }
    }

    /**
     * Handles the PlayerJoinEvent to show ActionBar messages to players when they join the game.
     *
     * @param event The PlayerJoinEvent.
     */
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        CustomFishingPlugin.getInstance().getScheduler().runTaskAsyncLater(() -> {
            boolean hasJoined = competition.hasPlayerJoined(player);
            if ((hasJoined || actionBarConfig.isShowToAll())
                    && !senderMap.containsKey(player.getUniqueId())) {
                ActionBarSender sender = new ActionBarSender(player, actionBarConfig, competition);
                if (!sender.isVisible()) {
                    sender.show();
                }
                senderMap.put(player.getUniqueId(), sender);
            }
        }, 200, TimeUnit.MILLISECONDS);
    }

    /**
     * Shows an ActionBar message to a specific player.
     *
     * @param player The player to show the ActionBar message to.
     */
    public void showActionBarTo(Player player) {
        ActionBarSender sender = senderMap.get(player.getUniqueId());
        if (sender == null) {
            sender = new ActionBarSender(player, actionBarConfig, competition);
            senderMap.put(player.getUniqueId(), sender);
        }
        if (!sender.isVisible()) {
            sender.show();
        }
    }
}
