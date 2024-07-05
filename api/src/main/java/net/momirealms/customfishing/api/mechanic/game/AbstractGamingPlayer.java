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

package net.momirealms.customfishing.api.mechanic.game;

import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.api.mechanic.context.ContextKeys;
import net.momirealms.customfishing.api.mechanic.fishing.CustomFishingHook;
import net.momirealms.customfishing.common.plugin.scheduler.SchedulerTask;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;

import java.util.concurrent.TimeUnit;

public abstract class AbstractGamingPlayer implements GamingPlayer, Runnable {

//    private final FishingManager manager;
    protected long deadline;
    protected boolean success;
    protected SchedulerTask task;
    protected GameSetting settings;
    protected CustomFishingHook hook;
    protected boolean isTimeOut;
    private boolean valid = true;
    private boolean firstFlag = true;

    public AbstractGamingPlayer(CustomFishingHook hook, GameSetting settings) {
        this.hook = hook;
        this.settings = settings;
        this.deadline = (long) (System.currentTimeMillis() + settings.time() * 1000L);
        this.arrangeTask();
    }

    public void arrangeTask() {
        this.task = BukkitCustomFishingPlugin.getInstance().getScheduler().asyncRepeating(this, 50, 50, TimeUnit.MILLISECONDS);
    }

    @Override
    public void destroy() {
        if (task != null) task.cancel();
        valid = false;
    }

    @Override
    public void cancel() {
        destroy();
    }

    @Override
    public boolean isSuccessful() {
        return success;
    }

    @ApiStatus.Internal
    public void internalRightClick() {
        firstFlag = true;
        handleRightClick();
    }

    @Override
    public void handleRightClick() {
        endGame();
    }

    @ApiStatus.Internal
    public boolean internalLeftClick() {
        if (firstFlag) {
            firstFlag = false;
            return false;
        }
        return handleLeftClick();
    }

    @Override
    public boolean handleLeftClick() {
        return false;
    }

    @Override
    public boolean handleChat(String message) {
        return false;
    }

    @Override
    public void handleSwapHand() {
    }

    @Override
    public boolean handleJump() {
        return false;
    }

    @Override
    public boolean handleSneak() {
        return false;
    }

    @Override
    public Player getPlayer() {
        return hook.getContext().getHolder();
    }

    @Override
    public void run() {
        if (timeOutCheck()) {
            return;
        }
        tick();
    }

    @Override
    public boolean isValid() {
        return valid;
    }

    protected abstract void tick();

    protected void endGame() {
        hook.handleGameResult();
        valid = false;
    }

    protected void setGameResult(boolean success) {
        this.success = success;
    }

    protected boolean timeOutCheck() {
        long delta = deadline - System.currentTimeMillis();
        if (delta <= 0) {
            isTimeOut = true;
            endGame();
            return true;
        }
        hook.getContext().arg(ContextKeys.TIME_LEFT, String.format("%.1f", (double) delta / 1000));
        return false;
    }
}
