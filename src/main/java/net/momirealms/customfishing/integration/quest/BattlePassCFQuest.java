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
import io.github.battlepass.api.events.server.PluginReloadEvent;
import io.github.battlepass.quests.service.base.ExternalQuestContainer;
import io.github.battlepass.registry.quest.QuestRegistry;
import net.momirealms.customfishing.api.event.FishResultEvent;
import net.momirealms.customfishing.fishing.FishResult;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class BattlePassCFQuest implements Listener {

    public static void register() {
        QuestRegistry questRegistry = BattlePlugin.getApi().getQuestRegistry();
        questRegistry.hook("customfishing", FishQuest::new);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBattlePassReload(PluginReloadEvent event) {
        register();
    }

    private static class FishQuest extends ExternalQuestContainer {

        public FishQuest(BattlePlugin battlePlugin) {
            super(battlePlugin, "customfishing");
        }

        @EventHandler
        public void onFishCaught(FishResultEvent event) {
            if (event.isCancelled() || event.getResult() == FishResult.FAILURE)
                return;
            Player player = event.getPlayer();
            if (event.getLootID() != null)
                this.executionBuilder("fish")
                        .player(player)
                        .root(event.getLootID())
                        .progress(event.isDouble() ? 2 : 1)
                        .buildAndExecute();
            if (event.getLoot() != null && event.getLoot().getGroup() != null)
                this.executionBuilder("group")
                        .player(player)
                        .root(event.getLoot().getGroup())
                        .progress(event.isDouble() ? 2 : 1)
                        .buildAndExecute();
        }
    }
}