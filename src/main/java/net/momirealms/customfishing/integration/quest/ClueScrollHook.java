package net.momirealms.customfishing.integration.quest;

import com.electro2560.dev.cluescrolls.api.ClueScrollsAPI;
import com.electro2560.dev.cluescrolls.api.CustomClue;
import net.momirealms.customfishing.CustomFishing;
import net.momirealms.customfishing.api.event.FishResultEvent;
import net.momirealms.customfishing.object.fishing.FishResult;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ClueScrollHook implements Listener {

    private final CustomClue fishClue;
    private final CustomClue mobClue;
    private final CustomClue commonClue;

    public ClueScrollHook () {
        commonClue = ClueScrollsAPI.getInstance().registerCustomClue(CustomFishing.plugin, "fish");
        fishClue = ClueScrollsAPI.getInstance().registerCustomClue(CustomFishing.plugin, "catch_fish");
        mobClue = ClueScrollsAPI.getInstance().registerCustomClue(CustomFishing.plugin, "catch_mob");
    }

    @EventHandler
    public void onFish(FishResultEvent event) {
        if (event.getResult() == FishResult.CAUGHT_LOOT || event.getResult() == FishResult.CAUGHT_VANILLA) {
            fishClue.handle(event.getPlayer(), 1);
            commonClue.handle(event.getPlayer(), 1);
        }
        if (event.getResult() == FishResult.CAUGHT_MOB) {
            mobClue.handle(event.getPlayer(), 1);
            commonClue.handle(event.getPlayer(), 1);
        }
    }
}
