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
import io.github.battlepass.quests.quests.external.executor.ExternalQuestExecutor;
import io.github.battlepass.registry.quest.QuestRegistry;
import net.momirealms.customfishing.api.event.FishResultEvent;
import net.momirealms.customfishing.fishing.FishResult;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class BattlePassCFQuest extends ExternalQuestExecutor implements Listener {

    public static void register() {
        QuestRegistry questRegistry = BattlePlugin.getApi().getQuestRegistry();
        questRegistry.hook("customfishing", BattlePassCFQuest::new);
    }

    public BattlePassCFQuest(BattlePlugin battlePlugin) {
        super(battlePlugin, "customfishing");
    }

    @EventHandler
    public void onFishCaught(FishResultEvent event) {
        if (event.isCancelled()) return;
        Player player = event.getPlayer();
        if (event.getResult() == FishResult.CATCH_SPECIAL_ITEM || event.getResult() == FishResult.CATCH_VANILLA_ITEM || event.getResult() == FishResult.CATCH_MOB) {
            this.execute("fish", player, (var1x) -> var1x.root(event.getLoot_id()));
        }
    }
}
