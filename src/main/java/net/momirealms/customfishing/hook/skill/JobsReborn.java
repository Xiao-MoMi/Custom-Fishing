package net.momirealms.customfishing.hook.skill;

import com.gamingmesh.jobs.Jobs;
import com.gamingmesh.jobs.container.Job;
import com.gamingmesh.jobs.container.JobProgression;
import com.gamingmesh.jobs.container.JobsPlayer;
import org.bukkit.entity.Player;

import java.util.List;

public class JobsReborn implements SkillXP{

    @Override
    public void addXp(Player player, double amount) {
        JobsPlayer jobsPlayer = Jobs.getPlayerManager().getJobsPlayer(player);

        if (jobsPlayer != null) {
            List<JobProgression> jobs = jobsPlayer.getJobProgression();

            Job job = Jobs.getJob("Fisherman");

            for (JobProgression progression : jobs)
                if (progression.getJob().equals(job)){
                    progression.addExperience(amount);
                }
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
}
