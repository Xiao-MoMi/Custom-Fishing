package net.momirealms.customfishing.timer;

import net.momirealms.customfishing.CustomFishing;
import net.momirealms.customfishing.bar.Difficulty;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public class Timer {

    private final TimerTask timerTask;
    private final BukkitTask task;
    private final String layout;

    public Timer(Player player, Difficulty difficulty, String layout) {
        this.layout = layout;
        this.timerTask = new TimerTask(player, difficulty, layout);
        this.task = timerTask.runTaskTimerAsynchronously(CustomFishing.instance, 1,1);
        timerTask.setTaskID(task.getTaskId());
    }

    public TimerTask getTimerTask(){ return this.timerTask; }
    public int getTaskID (){ return this.task.getTaskId(); }
    public String getLayout(){return this.layout;}
}