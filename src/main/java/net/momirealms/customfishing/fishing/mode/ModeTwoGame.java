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

package net.momirealms.customfishing.fishing.mode;

import net.momirealms.customfishing.CustomFishing;
import net.momirealms.customfishing.fishing.bar.ModeTwoBar;
import net.momirealms.customfishing.manager.FishingManager;
import net.momirealms.customfishing.util.AdventureUtils;
import net.momirealms.customfishing.util.LocationUtils;
import org.bukkit.Location;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerFishEvent;

import java.util.concurrent.TimeUnit;

public class ModeTwoGame extends FishingGame {

    private double hold_time;
    private final ModeTwoBar modeTwoBar;
    private double judgement_position;
    private double fish_position;
    private double judgement_velocity;
    private double fish_velocity;
    private int timer;
    private final int time_requirement;
    private boolean played;

    public ModeTwoGame(
            CustomFishing plugin,
            FishingManager fishingManager,
            long deadline,
            Player player,
            int difficulty,
            ModeTwoBar modeTwoBar,
            Location hookLoc
    ) {
        super(plugin, fishingManager, deadline, player, difficulty, modeTwoBar);
        this.success = false;
        this.judgement_position = (double) (modeTwoBar.getBarEffectiveWidth() - modeTwoBar.getJudgementAreaWidth()) / 2;
        this.fish_position = 0;
        this.timer = 0;
        this.modeTwoBar = modeTwoBar;
        this.time_requirement = modeTwoBar.getRandomTimeRequirement();
        this.played = false;
        this.gameTask = plugin.getScheduler().runTaskTimerAsync(this, 50, 40, TimeUnit.MILLISECONDS);
    }

    @Override
    public void run() {
        super.run();
        if (player.isSneaking()) addV();
        else reduceV();
        if (timer < 40 - difficulty) {
            timer++;
        } else {
            timer = 0;
            if (Math.random() > ((double) 1 / (difficulty + 1))) {
                burst();
            }
        }
        judgement_position += judgement_velocity;
        fish_position += fish_velocity;
        fraction();
        calibrate();
        if (fish_position >= judgement_position - 2 && fish_position + modeTwoBar.getFishIconWidth() <= judgement_position + modeTwoBar.getJudgementAreaWidth() + 2) {
            hold_time += 0.66;
        } else {
            hold_time -= modeTwoBar.getPunishment() * 0.66;
        }
        if (hold_time >= time_requirement) {
            success();
            return;
        }
        showBar();
    }

    @Override
    public void showBar() {
        String bar = "<font:" + modeTwoBar.getFont() + ">" + modeTwoBar.getBarImage()
                + "<font:" + offsetManager.getFont() + ">" + offsetManager.getOffsetChars((int) (modeTwoBar.getJudgementAreaOffset() + judgement_position)) + "</font>"
                + modeTwoBar.getJudgementAreaImage()
                + "<font:" + offsetManager.getFont() + ">" + offsetManager.getOffsetChars((int) (modeTwoBar.getBarEffectiveWidth() - judgement_position - modeTwoBar.getJudgementAreaWidth())) + "</font>"
                + "<font:" + offsetManager.getFont() + ">" + offsetManager.getOffsetChars((int) (-modeTwoBar.getBarEffectiveWidth() - 1 + fish_position)) + "</font>"
                + modeTwoBar.getFishImage()
                + "<font:" + offsetManager.getFont() + ">" + offsetManager.getOffsetChars((int) (modeTwoBar.getBarEffectiveWidth() - fish_position - modeTwoBar.getFishIconWidth() + 1)) + "</font>"
                + "</font>";
        hold_time = Math.max(0, Math.min(hold_time, time_requirement));
        AdventureUtils.playerTitle(
                player,
                modeTwoBar.getTip() != null && !played ? modeTwoBar.getTip() :
                title.replace("{progress}", modeTwoBar.getProgress()[(int) ((hold_time / time_requirement) * modeTwoBar.getProgress().length)])
                ,
                bar,
                0,
                500,
                0
        );
    }

    private void burst() {
        if (Math.random() < (judgement_position / modeTwoBar.getBarEffectiveWidth())) {
            judgement_velocity = -1 - 0.8 * Math.random() * difficulty;
        } else {
            judgement_velocity = 1 + 0.8 * Math.random() * difficulty;
        }
    }

    private void fraction() {
        if (judgement_velocity > 0) {
            judgement_velocity -= modeTwoBar.getWaterResistance();
            if (judgement_velocity < 0) judgement_velocity = 0;
        } else {
            judgement_velocity += modeTwoBar.getWaterResistance();
            if (judgement_velocity > 0) judgement_velocity = 0;
        }
    }

    private void reduceV() {
        fish_velocity -= modeTwoBar.getLooseningLoss();
    }

    private void addV() {
        played = true;
        fish_velocity += modeTwoBar.getPullingStrength();
    }

    private void calibrate() {
        if (fish_position < 0) {
            fish_position = 0;
            fish_velocity = 0;
        }
        if (fish_position + modeTwoBar.getFishIconWidth() > modeTwoBar.getBarEffectiveWidth()) {
            fish_position = modeTwoBar.getBarEffectiveWidth() - modeTwoBar.getFishIconWidth();
            fish_velocity = 0;
        }
        if (judgement_position < 0) {
            judgement_position = 0;
            judgement_velocity = 0;
        }
        if (judgement_position + modeTwoBar.getJudgementAreaWidth() > modeTwoBar.getBarEffectiveWidth()) {
            judgement_position = modeTwoBar.getBarEffectiveWidth() - modeTwoBar.getJudgementAreaWidth();
            judgement_velocity = 0;
        }
    }

    @Override
    public boolean isSuccess() {
        return success;
    }
}
