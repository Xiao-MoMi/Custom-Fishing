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

package net.momirealms.customfishing.integration.job;

import com.gamingmesh.jobs.Jobs;
import com.gamingmesh.jobs.container.*;
import com.gamingmesh.jobs.listeners.JobsPaymentListener;
import net.momirealms.customfishing.api.event.FishResultEvent;
import net.momirealms.customfishing.fishing.FishResult;
import net.momirealms.customfishing.integration.JobInterface;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.List;

public class JobsRebornImpl implements JobInterface, Listener {

    @Override
    public void addXp(Player player, double amount) {
        JobsPlayer jobsPlayer = Jobs.getPlayerManager().getJobsPlayer(player);
        if (jobsPlayer != null) {
            List<JobProgression> jobs = jobsPlayer.getJobProgression();
            Job job = Jobs.getJob("Fisherman");
            for (JobProgression progression : jobs)
                if (progression.getJob().equals(job))
                    progression.addExperience(amount);
        }
    }

    @Override
    public int getLevel(Player player) {
        JobsPlayer jobsPlayer = Jobs.getPlayerManager().getJobsPlayer(player);
        if (jobsPlayer != null) {
            List<JobProgression> jobs = jobsPlayer.getJobProgression();
            Job job = Jobs.getJob("Fisherman");
            for (JobProgression progression : jobs)
                if (progression.getJob().equals(job))
                    return progression.getLevel();
        }
        return 0;
    }

    @EventHandler
    public void onFish(FishResultEvent event) {
        if (event.isCancelled() || event.getResult() == FishResult.FAILURE) return;
        Player player = event.getPlayer();
        if (!Jobs.getGCManager().canPerformActionInWorld(player.getWorld())) return;
        if (!JobsPaymentListener.payIfCreative(player))
            return;

        if (!Jobs.getPermissionHandler().hasWorldPermission(player, player.getLocation().getWorld().getName()))
            return;

        JobsPlayer jobsPlayer = Jobs.getPlayerManager().getJobsPlayer(player);
        if (jobsPlayer == null) return;

        Jobs.action(jobsPlayer, new CustomFishingInfo(event.getLootID(), ActionType.MMKILL));
    }

    public static class CustomFishingInfo extends BaseActionInfo {
        private final String name;

        public CustomFishingInfo(String name, ActionType type) {
            super(type);
            this.name = name;
        }

        public String getName() {
            return this.name;
        }

        public String getNameWithSub() {
            return this.name;
        }
    }
}
