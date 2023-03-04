package net.momirealms.customfishing.fishing.mode;

import net.momirealms.customfishing.CustomFishing;
import net.momirealms.customfishing.fishing.bar.FishingBar;
import net.momirealms.customfishing.manager.FishingManager;
import org.bukkit.entity.Player;

public class ModeThreeGame extends FishingGame {

    public ModeThreeGame(CustomFishing plugin, FishingManager fishingManager, long deadline, Player player, int difficulty, FishingBar fishingBar) {
        super(plugin, fishingManager, deadline, player, difficulty, fishingBar);
    }
}
