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

package net.momirealms.customfishing.fishing.competition.bossbar;

import net.momirealms.customfishing.CustomFishing;
import net.momirealms.customfishing.fishing.competition.Competition;
import net.momirealms.customfishing.listener.JoinQuitListener;
import net.momirealms.customfishing.object.Function;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class BossBarManager extends Function {

    private static final ConcurrentHashMap<UUID, BossBarSender> senderMap = new ConcurrentHashMap<>();
    private final JoinQuitListener joinQuitListener;
    private final BossBarConfig bossBarConfig;

    public BossBarManager(BossBarConfig bossBarConfig) {
        this.joinQuitListener = new JoinQuitListener(this);
        this.bossBarConfig = bossBarConfig;
    }

    @Override
    public void load() {
        Bukkit.getPluginManager().registerEvents(this.joinQuitListener, CustomFishing.getInstance());
    }

    @Override
    public void unload() {
        if (this.joinQuitListener != null) HandlerList.unregisterAll(this.joinQuitListener);
        for (BossBarSender bossBarSender : senderMap.values()) {
            bossBarSender.hide();
        }
        senderMap.clear();
    }

    @Override
    public void onQuit(Player player) {
        BossBarSender sender = senderMap.get(player.getUniqueId());
        if (sender != null) {
            if (sender.isVisible()) {
                sender.hide();
            }
            senderMap.remove(player.getUniqueId());
        }
    }

    @Override
    public void onJoin(Player player) {
        CustomFishing.getInstance().getScheduler().runTaskAsyncLater(() -> {
            if (Competition.currentCompetition != null){
                boolean hasJoined = Competition.currentCompetition.isJoined(player);
                if ((hasJoined || bossBarConfig.isShowToAll()) && senderMap.get(player.getUniqueId()) == null) {
                    BossBarSender sender = new BossBarSender(player, bossBarConfig);
                    sender.setHasClaimedJoinReward(hasJoined);
                    if (!sender.isVisible()) {
                        sender.show();
                    }
                    senderMap.put(player.getUniqueId(), sender);
                }
            }
        }, 200, TimeUnit.MILLISECONDS);
    }

    public void tryJoin(Player player, boolean hasJoinReward) {
        BossBarSender sender = senderMap.get(player.getUniqueId());
        if (sender == null) {
            sender = new BossBarSender(player, Competition.currentCompetition.getCompetitionConfig().getBossBarConfig());
            senderMap.put(player.getUniqueId(), sender);
        }
        if (!sender.isVisible()) {
            sender.show();
        }
        if (hasJoinReward && !sender.hasClaimedJoin()) {
            sender.setHasClaimedJoinReward(true);
            for (String joinCmd : Competition.currentCompetition.getCompetitionConfig().getJoinCommand()){
                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), joinCmd.replace("{player}", player.getName()));
            }
        }
    }
}
