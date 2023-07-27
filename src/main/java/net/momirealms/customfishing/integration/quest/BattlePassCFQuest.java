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

import io.github.battlepass.BattlePlugin;
import io.github.battlepass.quests.service.base.ExternalQuestContainer;
import io.github.battlepass.registry.quest.QuestRegistry;
import net.momirealms.customfishing.api.event.FishResultEvent;
import net.momirealms.customfishing.fishing.FishResult;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

public class BattlePassCFQuest extends ExternalQuestContainer {

    public BattlePassCFQuest(BattlePlugin battlePlugin) {
        super(battlePlugin, "customfishing");
    }

    public static void register() {
        QuestRegistry questRegistry = BattlePlugin.getApi().getQuestRegistry();
        questRegistry.hook("customfishing", BattlePassCFQuest::new);
    }

    @EventHandler(ignoreCancelled = true)
    public void onFishCaught(FishResultEvent event) {
        if (event.getResult() == FishResult.FAILURE) return;
        Player player = event.getPlayer();
        // Determine if the item is VANILLA_ITEM or MOB
//        if (event.getLoot() == null) {
//            FishResult result = event.getResult();
//            // I didn't know how to refine this judgment, so I just roughly +1
//            if (result == FishResult.CATCH_VANILLA_ITEM || result == FishResult.CATCH_MOB) {
//                this.executionBuilder("fish").player(player).root(event.getItemStack())
//                        .progress(1).buildAndExecute();
//            } else {
//                return;
//            }
//        }
        // event.getLootID() Fish's ID
        // .progress(1) Player can get 1 point
        this.executionBuilder("fish").player(player).root(event.getLootID())
                .progress(1).buildAndExecute();

        if (event.getLoot().getGroup() == null) return;

        this.executionBuilder("fish_group").player(player).root(event.getLoot().getGroup())
                .progress(1).buildAndExecute();
    }
}
