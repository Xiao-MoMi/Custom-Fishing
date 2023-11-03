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

import net.momirealms.customfishing.api.CustomFishingPlugin;
import net.momirealms.customfishing.api.manager.FishingManager;
import net.momirealms.customfishing.api.mechanic.effect.Effect;
import net.momirealms.customfishing.api.scheduler.CancellableTask;
import org.bukkit.Material;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;

public abstract class AbstractGamingPlayer implements GamingPlayer, Runnable {

    private final FishingManager manager;
    protected long deadline;
    protected boolean success;
    protected CancellableTask task;
    protected Player player;
    protected GameSettings settings;
    protected FishHook fishHook;
    protected boolean isTimeOut;

    public AbstractGamingPlayer(Player player, FishHook hook, GameSettings settings) {
        this.player = player;
        this.fishHook = hook;
        this.settings = settings;
        this.manager = CustomFishingPlugin.get().getFishingManager();
        this.deadline = (long) (System.currentTimeMillis() + settings.getTime() * 1000L);
        this.arrangeTask();
    }

    public void arrangeTask() {
        this.task = CustomFishingPlugin.get().getScheduler().runTaskSyncTimer(this, fishHook.getLocation(), 1, 1);
    }

    @Override
    public void cancel() {
        if (task != null && !task.isCancelled())
            task.cancel();
    }

    @Override
    public boolean isSuccessful() {
        return success;
    }

    @Override
    public boolean onRightClick() {
        endGame();
        return true;
    }

    @Override
    public boolean onChat(String message) {
        return false;
    }

    @Override
    public boolean onSwapHand() {
        return false;
    }

    @Override
    public boolean onJump() {
        return false;
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    @Override
    public Effect getEffectReward() {
        return null;
    }

    @Override
    public void run() {
        timeOutCheck();
        switchItemCheck();
    }

    protected void endGame() {
        this.manager.processGameResult(this);
    }

    protected void setGameResult(boolean success) {
        this.success = success;
    }

    protected void timeOutCheck() {
        if (System.currentTimeMillis() > deadline) {
            isTimeOut = true;
            cancel();
            endGame();
        }
    }

    protected void switchItemCheck() {
        PlayerInventory playerInventory = player.getInventory();
        if (playerInventory.getItemInMainHand().getType() != Material.FISHING_ROD
                && playerInventory.getItemInOffHand().getType() != Material.FISHING_ROD) {
            cancel();
            endGame();
        }
    }
}
