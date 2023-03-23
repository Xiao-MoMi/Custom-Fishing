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
import net.momirealms.customfishing.fishing.bar.ModeOneBar;
import net.momirealms.customfishing.manager.FishingManager;
import net.momirealms.customfishing.util.AdventureUtil;
import org.bukkit.entity.Player;

public class ModeOneGame extends FishingGame {

    private int progress;
    private boolean face;
    private final ModeOneBar modeOneBar;

    public ModeOneGame(CustomFishing plugin, FishingManager fishingManager, long deadline, Player player, int difficulty, ModeOneBar modeOneBar) {
        super(plugin, fishingManager, deadline, player, difficulty, modeOneBar);
        this.face = true;
        this.modeOneBar = modeOneBar;
    }

    @Override
    public void run() {
        super.run();
        if (face) progress += difficulty;
        else progress -= difficulty;
        if (progress > modeOneBar.getTotalWidth()) {
            face = !face;
            progress = 2 * modeOneBar.getTotalWidth() - progress;
        }
        else if (progress < 0) {
            face = !face;
            progress = -progress;
        }
        showBar();
    }

    @Override
    public void showBar() {
        String bar = "<font:" + modeOneBar.getFont() + ">" + modeOneBar.getBarImage()
                        + "<font:" + offsetManager.getFont() + ">" + offsetManager.getOffsetChars(modeOneBar.getPointerOffset() + progress) + "</font>"
                        + modeOneBar.getPointerImage()
                        + "<font:" + offsetManager.getFont() + ">" + offsetManager.getOffsetChars(modeOneBar.getTotalWidth() - progress - modeOneBar.getPointerWidth()) + "</font></font>";
        AdventureUtil.playerTitle(player, title, bar,0,500,0);
    }

    @Override
    public boolean isSuccess() {
        int last = progress / modeOneBar.getWidthPerSection();
        return (Math.random() < modeOneBar.getSuccessRate()[last]);
    }
}
