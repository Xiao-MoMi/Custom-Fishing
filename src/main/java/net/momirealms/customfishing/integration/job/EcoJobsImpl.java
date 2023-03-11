package net.momirealms.customfishing.integration.job;

import com.willfp.ecojobs.api.EcoJobsAPI;
import com.willfp.ecojobs.jobs.Job;
import com.willfp.ecojobs.jobs.Jobs;
import net.momirealms.customfishing.integration.JobInterface;
import org.bukkit.entity.Player;

public class EcoJobsImpl implements JobInterface {

    private final EcoJobsAPI api;

    public EcoJobsImpl() {
        this.api = EcoJobsAPI.getInstance();
    }

    @Override
    public void addXp(Player player, double amount) {
        Job job = api.getActiveJob(player);
        if (job == null) return;
        if (job.getId().equals("fisherman")) {
            api.giveJobExperience(player, job, amount);
        }
    }

    @Override
    public int getLevel(Player player) {
        Job job = Jobs.getByID("fisherman");
        if (job == null) return 0;
        return api.getJobLevel(player, job);
    }
}
