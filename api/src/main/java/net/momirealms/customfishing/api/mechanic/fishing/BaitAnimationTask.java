/*
 *  Copyright (C) <2024> <XiaoMoMi>
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

package net.momirealms.customfishing.api.mechanic.fishing;

import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.common.plugin.scheduler.SchedulerTask;
import net.momirealms.sparrow.heart.SparrowHeart;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.TimeUnit;

public class BaitAnimationTask implements Runnable {

    private final SchedulerTask task;
    private final int entityID;
    private final Player player;
    private final FishHook fishHook;

    public BaitAnimationTask(BukkitCustomFishingPlugin plugin, Player player, FishHook fishHook, ItemStack baitItem) {
        this.player = player;
        this.fishHook = fishHook;
        this.task = plugin.getScheduler().asyncRepeating(this, 50, 50, TimeUnit.MILLISECONDS);
        ItemStack itemStack = baitItem.clone();
        itemStack.setAmount(1);
        this.entityID = SparrowHeart.getInstance().dropFakeItem(player, itemStack, fishHook.getLocation().clone().subtract(0,0.6,0));
    }

    @Override
    public void run() {
        SparrowHeart.getInstance().sendClientSideEntityMotion(player, fishHook.getVelocity(), entityID);
        SparrowHeart.getInstance().sendClientSideTeleportEntity(player, fishHook.getLocation().clone().subtract(0,0.6,0), fishHook.getVelocity(), false, entityID);
    }

    public void cancel() {
        task.cancel();
        SparrowHeart.getInstance().removeClientSideEntity(player, entityID);
    }
}
