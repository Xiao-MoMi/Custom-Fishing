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

package net.momirealms.customfishing.integration.quest;

import com.electro2560.dev.cluescrolls.api.ClueScrollsAPI;
import com.electro2560.dev.cluescrolls.api.CustomClue;
import net.momirealms.customfishing.CustomFishing;
import net.momirealms.customfishing.api.event.FishResultEvent;
import net.momirealms.customfishing.fishing.FishResult;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ClueScrollCFQuest implements Listener {

    private final CustomClue fishClue;
    private final CustomClue mobClue;
    private final CustomClue commonClue;

    public ClueScrollCFQuest() {
        commonClue = ClueScrollsAPI.getInstance().registerCustomClue(CustomFishing.getInstance(), "fish");
        fishClue = ClueScrollsAPI.getInstance().registerCustomClue(CustomFishing.getInstance(), "catch_fish");
        mobClue = ClueScrollsAPI.getInstance().registerCustomClue(CustomFishing.getInstance(), "catch_mob");
    }

    @EventHandler
    public void onFish(FishResultEvent event) {
        if (event.isCancelled()) return;
        if (event.getResult() == FishResult.CATCH_SPECIAL_ITEM || event.getResult() == FishResult.CATCH_VANILLA_ITEM) {
            fishClue.handle(event.getPlayer(), 1);
            commonClue.handle(event.getPlayer(), 1);
        }
        if (event.getResult() == FishResult.CATCH_MOB) {
            mobClue.handle(event.getPlayer(), 1);
            commonClue.handle(event.getPlayer(), 1);
        }
    }
}
