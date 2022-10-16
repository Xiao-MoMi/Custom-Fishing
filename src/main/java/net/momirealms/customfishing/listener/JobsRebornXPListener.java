package net.momirealms.customfishing.listener;

import com.gamingmesh.jobs.Jobs;
import com.gamingmesh.jobs.api.JobsExpGainEvent;
import com.gamingmesh.jobs.container.Job;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class JobsRebornXPListener implements Listener {

    @EventHandler
    public void onXpGain(JobsExpGainEvent event) {
        Job job = Jobs.getJob("Fisherman");
        if (event.getJob().equals(job)){
            event.setExp(0);
        }
    }
}