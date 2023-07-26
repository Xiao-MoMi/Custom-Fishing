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
import net.momirealms.customfishing.fishing.bar.ModeThreeBar;
import net.momirealms.customfishing.manager.FishingManager;
import net.momirealms.customfishing.util.AdventureUtils;
import org.bukkit.entity.Player;

import java.util.concurrent.TimeUnit;

public class ModeThreeGame extends FishingGame {

    private final ModeThreeBar modeThreeBar;
    private int fish_position;
    private int timer;
    private final int timer_max;
    private double strain;
    private int struggling_time;
    private boolean played;

    public ModeThreeGame(
            CustomFishing plugin,
            FishingManager fishingManager,
            long deadline,
            Player player,
            int difficulty,
            ModeThreeBar modeThreeBar
    ) {
        super(plugin, fishingManager, deadline, player, difficulty, modeThreeBar);
        this.fish_position = modeThreeBar.getFishStartPosition();
        this.success = false;
        this.modeThreeBar = modeThreeBar;
        this.timer_max = modeThreeBar.getStrugglingFishImage().length;
        this.gameTask = plugin.getScheduler().runTaskTimerAsync(this, 50, 40, TimeUnit.MILLISECONDS);
        this.played = false;
    }

    @Override
    public void run() {
        super.run();
        timer++;
        if (timer >= timer_max) {
            timer = 0;
        }
        if (struggling_time <= 0) {
            if (Math.random() < ((double) difficulty / 300)) {
                struggling_time = (int) (15 + Math.random() * difficulty * 3);
            }
        } else {
            struggling_time--;
        }
        if (player.isSneaking()) pull();
        else loosen();
        if (fish_position < modeThreeBar.getSuccessPosition() - modeThreeBar.getFishIconWidth() - 1) {
            success();
            return;
        }
        if (fish_position + modeThreeBar.getFishIconWidth() > modeThreeBar.getBarEffectiveWidth() || strain >= modeThreeBar.getUltimateStrain()) {
            fail();
            return;
        }
        showBar();
    }

    public void pull() {
        played = true;
        if (struggling_time > 0) {
            strain += (modeThreeBar.getStrugglingIncrease() + ((double) difficulty / 5));
            fish_position -= 1;
        } else {
            strain += modeThreeBar.getNormalIncrease();
            fish_position -= 2;
        }
    }

    public void loosen() {
        fish_position++;
        strain -= modeThreeBar.getStrainLoss();
    }

    @Override
    public void showBar() {
        String bar = "<font:" + modeThreeBar.getFont() + ">" + modeThreeBar.getBarImage()
                + "<font:" + offsetManager.getFont() + ">" + offsetManager.getOffsetChars(modeThreeBar.getFishOffset() + fish_position) + "</font>"
                + (struggling_time > 0 ? modeThreeBar.getStrugglingFishImage()[timer] : modeThreeBar.getFishImage())
                + "<font:" + offsetManager.getFont() + ">" + offsetManager.getOffsetChars(modeThreeBar.getBarEffectiveWidth() - fish_position - modeThreeBar.getFishIconWidth()) + "</font>"
                + "</font>";
        strain = Math.max(0, Math.min(strain, modeThreeBar.getUltimateStrain()));
        AdventureUtils.playerTitle(
                player,
                modeThreeBar.getTip() != null && !played ? modeThreeBar.getTip() :
                title.replace("{strain}", modeThreeBar.getStrain()[(int) ((strain / modeThreeBar.getUltimateStrain()) * modeThreeBar.getStrain().length)]),
                bar,
                0,
                500,
                0
        );
    }

    @Override
    public boolean isSuccess() {
        return success;
    }
}
