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
    private final BossBar.Color color;
    private final BossBar.Overlay overlay;
    private final String text;
    private final int rate;

    public BossBarSender(Player player, BossBarConfig bossbarConfig){
        this.player = player;
        this.audience = CustomFishing.adventure.player(player);
        this.timer = 0;
        this.color = bossbarConfig.getColor();
        this.overlay = bossbarConfig.getOverlay();
        this.text = bossbarConfig.getText();
        this.rate = bossbarConfig.getRate();
    }

    public void hideBossbar(){
        audience.hideBossBar(bossBar);
    }

    public void showBossbar(){
        String newText = updateText();
        bossBar = BossBar.bossBar(
                CustomFishing.miniMessage.deserialize(newText),
                Competition.progress,
                color,
                overlay);
        audience.showBossBar(bossBar);
    }

    @Override
    public void run() {
        if (timer < rate){
            timer++;
        }else {
            updateText();
            String newText = updateText();
            bossBar.name(CustomFishing.miniMessage.deserialize(newText));
            bossBar.progress(Competition.progress);
            timer = 0;
        }
    }

    private String updateText() {
        String text;
        if (ConfigReader.Config.papi){
            text = PapiHook.parse(player, this.text);
        }else {
            text = this.text;
        }
        String newText;
        if (ConfigReader.Config.papi){
            newText = PapiHook.parse(player, text.replace("{time}", String.valueOf(Competition.remainingTime))
                    .replace("{rank}", Optional.ofNullable(CompetitionSchedule.competition.getRanking().getPlayerRank(player.getName())).orElse(ConfigReader.Message.noRank))
                    .replace("{minute}", String.format("%02d",Competition.remainingTime/60))
                    .replace("{second}",String.format("%02d",Competition.remainingTime%60))
                    .replace("{point}", String.format("%.1f",Optional.ofNullable(CompetitionSchedule.competition.getRanking().getCompetitionPlayer(player.getName())).orElse(Competition.emptyPlayer).getScore())));
        }else {
            newText = text.replace("{time}", String.valueOf(Competition.remainingTime))
                    .replace("{rank}", Optional.ofNullable(CompetitionSchedule.competition.getRanking().getPlayerRank(player.getName())).orElse(ConfigReader.Message.noRank))
                    .replace("{minute}", String.format("%02d",Competition.remainingTime/60))
                    .replace("{second}",String.format("%02d",Competition.remainingTime%60))
                    .replace("{point}", String.format("%.1f",Optional.ofNullable(CompetitionSchedule.competition.getRanking().getCompetitionPlayer(player.getName())).orElse(Competition.emptyPlayer).getScore()));
        }
        return newText;
    }
}
