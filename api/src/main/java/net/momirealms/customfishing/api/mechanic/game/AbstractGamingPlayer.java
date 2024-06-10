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
import net.momirealms.customfishing.api.mechanic.effect.Effect;
import net.momirealms.customfishing.common.plugin.scheduler.SchedulerTask;
import org.bukkit.Material;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;

import java.util.concurrent.TimeUnit;

public abstract class AbstractGamingPlayer implements GamingPlayer, Runnable {

//    private final FishingManager manager;
    protected long deadline;
    protected boolean success;
    protected SchedulerTask task;
    protected Player player;
    protected GameSettings settings;
    protected FishHook fishHook;
    protected boolean isTimeOut;

    public AbstractGamingPlayer(Player player, FishHook hook, GameSettings settings) {
        this.player = player;
        this.fishHook = hook;
        this.settings = settings;
//        this.manager = BukkitCustomFishingPlugin.getInstance().get();
        this.deadline = (long) (System.currentTimeMillis() + settings.time() * 1000L);
        this.arrangeTask();
    }

    public void arrangeTask() {
        this.task = BukkitCustomFishingPlugin.getInstance().getScheduler().asyncRepeating(this, 50, 50, TimeUnit.MILLISECONDS);
    }

    @Override
    public void cancel() {
        if (task != null) {
            task.cancel();
        }
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
    public boolean onLeftClick() {
        return false;
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
    public boolean onSneak() {
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
        if (timeOutCheck()) {
            return;
        }
        switchItemCheck();
        onTick();
    }

    public void onTick() {

    }

    protected void endGame() {
//        this.manager.processGameResult(this);
    }

    protected void setGameResult(boolean success) {
        this.success = success;
    }

    protected boolean timeOutCheck() {
        if (System.currentTimeMillis() > deadline) {
            isTimeOut = true;
            cancel();
            endGame();
            return true;
        }
        return false;
    }

    protected void switchItemCheck() {
        PlayerInventory playerInventory = player.getInventory();
        if (playerInventory.getItemInMainHand().getType() != Material.FISHING_ROD
            && playerInventory.getItemInOffHand().getType() != Material.FISHING_ROD
        ) {
            cancel();
            endGame();
        }
    }
}
