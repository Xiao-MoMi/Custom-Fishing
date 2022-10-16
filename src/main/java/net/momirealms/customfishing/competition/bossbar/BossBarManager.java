package net.momirealms.customfishing.competition.bossbar;

import net.momirealms.customfishing.Function;
import net.momirealms.customfishing.competition.Competition;
import org.bukkit.entity.Player;

public class BossBarManager extends Function {

    private final Competition competition;

    public BossBarManager(Competition competition) {
        this.competition = competition;
    }

    @Override
    public void load() {
        super.load();
    }

    @Override
    public void unload() {
        super.unload();
    }

    @Override
    public void onQuit(Player player) {
        super.onQuit(player);
    }

    @Override
    public void onJoin(Player player) {
        super.onJoin(player);
    }
}
