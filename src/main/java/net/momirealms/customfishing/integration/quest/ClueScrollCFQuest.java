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

import com.electro2560.dev.cluescrolls.api.*;
import net.momirealms.customfishing.CustomFishing;
import net.momirealms.customfishing.api.event.FishResultEvent;
import net.momirealms.customfishing.fishing.FishResult;
import net.momirealms.customfishing.fishing.loot.Loot;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ClueScrollCFQuest implements Listener {

    private final CustomClue idClue;
    private final CustomClue groupClue;

    public ClueScrollCFQuest() {
        idClue = ClueScrollsAPI.getInstance().registerCustomClue(CustomFishing.getInstance(), "fish", new ClueConfigData("fish_id", DataType.STRING));
        groupClue = ClueScrollsAPI.getInstance().registerCustomClue(CustomFishing.getInstance(), "group", new ClueConfigData("fish_group", DataType.STRING));
    }

    @EventHandler
    public void onFish(FishResultEvent event) {
        if (event.isCancelled() || event.getResult() == FishResult.FAILURE)
            return;
        if (event.getLootID() != null)
            idClue.handle(event.getPlayer(), event.isDouble() ? 2 : 1, new ClueDataPair("fish_id", event.getLootID()));
        if (event.getLoot() != null && event.getLoot().getGroup() != null)
            groupClue.handle(event.getPlayer(), event.isDouble() ? 2 : 1, new ClueDataPair("fish_group", event.getLoot().getGroup()));
    }
}
