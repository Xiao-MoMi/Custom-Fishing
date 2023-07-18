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
import net.momirealms.customfishing.fishing.bar.FishingBar;
import net.momirealms.customfishing.manager.FishingManager;
import net.momirealms.customfishing.manager.MessageManager;
import net.momirealms.customfishing.manager.OffsetManager;
import net.momirealms.customfishing.util.AdventureUtils;
import org.bukkit.Material;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffectType;

import java.time.LocalTime;
import java.util.concurrent.ScheduledFuture;

public abstract class FishingGame implements Runnable {

    protected OffsetManager offsetManager;
    protected long deadline;
    protected FishingManager fishingManager;
    protected CustomFishing plugin;
    protected Player player;
    protected int difficulty;
    protected String title;
    protected FishHook fishHook;
    protected ScheduledFuture<?> gameTask;
    protected boolean success;
    private int doubleCheckTime;

    public FishingGame(
            CustomFishing plugin,
            FishingManager fishingManager,
            long deadline,
            Player player,
            int difficulty,
            FishingBar fishingBar
    ) {
        this.plugin = plugin;
        this.offsetManager = plugin.getOffsetManager();
        this.fishingManager = fishingManager;
        this.player = player;
        this.deadline = deadline;
        this.difficulty = difficulty;
        this.title = fishingBar.getRandomTitle();
        this.fishHook = fishingManager.getHook(player.getUniqueId());
        this.success = false;
    }

    @Override
    public void run() {
        timeOutCheck();
        switchItemCheck();
        invalidHookCheck();
    }

    public void showBar() {

    }

    public boolean isSuccess() {
        return false;
    }

    protected void timeOutCheck() {
        if (System.currentTimeMillis() > deadline) {
            AdventureUtils.playerMessage(player, MessageManager.prefix + MessageManager.escape);
            cancel();
            fishingManager.removeFishingPlayer(player);
            fishingManager.removeHook(player.getUniqueId());
            fishingManager.fail(player, null, true);
        }
    }

    protected void switchItemCheck() {
        if (!isANewSecond()) return;
        PlayerInventory playerInventory = player.getInventory();
        if (playerInventory.getItemInMainHand().getType() != Material.FISHING_ROD && playerInventory.getItemInOffHand().getType() != Material.FISHING_ROD) {
            cancel();
            fishingManager.removeFishingPlayer(player);
            player.removePotionEffect(PotionEffectType.SLOW);
        }
    }

    protected void invalidHookCheck() {
        if (fishHook == null || !fishHook.isValid()) {
            cancel();
            fishingManager.removeFishingPlayer(player);
            player.removePotionEffect(PotionEffectType.SLOW);
        }
    }

    public void cancel() {
        if (!this.gameTask.isCancelled()) {
            this.gameTask.cancel(false);
        }
    }

    public void success() {
        success = true;
        proceedTheResult();
        cancel();
    }

    public void fail() {
        success = false;
        proceedTheResult();
        cancel();
    }

    public void proceedTheResult() {
        plugin.getScheduler().runTask(() -> {
            FishHook fishHook = fishingManager.getHook(player.getUniqueId());
            if (fishHook != null) {
                fishingManager.proceedReelIn(fishHook.getLocation(), player, this);
                fishingManager.removeHook(player.getUniqueId());
                fishingManager.removeFishingPlayer(player);
            }
        }, fishHook.getLocation());
    }

    private boolean isANewSecond() {
        int minute = LocalTime.now().getSecond();
        if (doubleCheckTime != minute) {
            doubleCheckTime = minute;
            return true;
        } else {
            return false;
        }
    }
}
