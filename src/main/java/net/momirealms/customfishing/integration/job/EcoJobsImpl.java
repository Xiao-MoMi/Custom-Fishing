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

import com.willfp.ecojobs.api.EcoJobsAPI;
import com.willfp.ecojobs.jobs.Job;
import com.willfp.ecojobs.jobs.Jobs;
import net.momirealms.customfishing.integration.JobInterface;
import org.bukkit.entity.Player;

public class EcoJobsImpl implements JobInterface {

    @Override
    public void addXp(Player player, double amount) {
        for (Job job : EcoJobsAPI.getActiveJobs(player)) {
            if (job.getId().equals("fisherman")) {
                EcoJobsAPI.giveJobExperience(player, job, amount);
            }
        }
    }

    @Override
    public int getLevel(Player player) {
//        Job job = Jobs.getByID("fisherman");
//        if (job == null) return 0;
//        return EcoJobsAPI.getJobLevel(player, job);
        //TODO com.willfp.libreforge.loader.configs.ConfigCategory
        return 0;
    }
}
