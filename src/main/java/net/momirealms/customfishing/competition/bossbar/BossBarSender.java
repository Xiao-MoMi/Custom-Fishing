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

package net.momirealms.customfishing.competition.bossbar;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.momirealms.customfishing.ConfigReader;
import net.momirealms.customfishing.CustomFishing;
import net.momirealms.customfishing.competition.Competition;
import net.momirealms.customfishing.competition.CompetitionSchedule;
import net.momirealms.customfishing.hook.PapiHook;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Optional;

public class BossBarSender extends BukkitRunnable {

    private final Player player;
    private final Audience audience;
    private BossBar bossBar;
    private int timer;
    private final BossBarConfig bossbarConfig;
    private final BossBar.Color color;
    private final BossBar.Overlay overlay;

    public BossBarSender(Player player, BossBarConfig bossbarConfig){
        this.player = player;
        this.bossbarConfig = bossbarConfig;
        this.audience = CustomFishing.adventure.player(player);
        this.timer = 0;
        this.color = bossbarConfig.getColor();
        this.overlay = bossbarConfig.getOverlay();
    }

    public void hideBossbar(){
        audience.hideBossBar(bossBar);
    }

    public void showBossbar(){
        String text;
        if (ConfigReader.Config.papi){
            text = PapiHook.parse(player, bossbarConfig.getText());
        }else {
            text = bossbarConfig.getText();
        }
        bossBar = BossBar.bossBar(
                MiniMessage.miniMessage().deserialize(text.replace("{time}", String.valueOf(Competition.remainingTime))
                .replace("{rank}", Optional.ofNullable(CompetitionSchedule.competition.getRanking().getPlayerRank(player.getName())).orElse(ConfigReader.Message.noRank))
                .replace("{minute}", String.format("%02d",Competition.remainingTime/60))
                .replace("{second}",String.format("%02d",Competition.remainingTime%60))
                .replace("{point}", String.format("%.1f",Optional.ofNullable(CompetitionSchedule.competition.getRanking().getCompetitionPlayer(player.getName())).orElse(Competition.emptyPlayer).getScore()))),
                Competition.progress,
                color,
                overlay);
        audience.showBossBar(bossBar);
    }

    @Override
    public void run() {
        if (timer < bossbarConfig.getRate()){
            timer++;
        }else {
            String text;
            if (ConfigReader.Config.papi){
                text = PapiHook.parse(player, bossbarConfig.getText());
            }else {
                text = bossbarConfig.getText();
            }
            bossBar.name(
                    MiniMessage.miniMessage().deserialize(text.replace("{time}", String.valueOf(Competition.remainingTime))
                    .replace("{rank}", Optional.ofNullable(CompetitionSchedule.competition.getRanking().getPlayerRank(player.getName())).orElse(ConfigReader.Message.noRank))
                    .replace("{minute}", String.format("%02d",Competition.remainingTime/60))
                    .replace("{second}",String.format("%02d",Competition.remainingTime%60))
                    .replace("{point}", String.format("%.1f",Optional.ofNullable(CompetitionSchedule.competition.getRanking().getCompetitionPlayer(player.getName())).orElse(Competition.emptyPlayer).getScore()))));
            bossBar.progress(Competition.progress);
            timer = 0;
        }
    }
}
