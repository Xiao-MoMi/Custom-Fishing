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

package net.momirealms.customfishing.bukkit.compatibility.quest;

import io.github.battlepass.BattlePlugin;
import io.github.battlepass.api.events.server.PluginReloadEvent;
import net.advancedplugins.bp.impl.actions.ActionRegistry;
import net.advancedplugins.bp.impl.actions.external.executor.ActionQuestExecutor;
import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.api.event.FishingResultEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class BattlePassQuest implements Listener {

    public BattlePassQuest() {
        Bukkit.getPluginManager().registerEvents(this, BukkitCustomFishingPlugin.getInstance().getBoostrap());
    }

    public void register() {
        ActionRegistry actionRegistry = BattlePlugin.getPlugin().getActionRegistry();
        actionRegistry.hook("customfishing", BPFishingQuest::new);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBattlePassReload(PluginReloadEvent event){
        register();
    }

    private static class BPFishingQuest extends ActionQuestExecutor {
        public BPFishingQuest(JavaPlugin plugin) {
            super(plugin, "customfishing");
        }

        @EventHandler
        public void onFish(FishingResultEvent event){
            if (event.isCancelled() || event.getResult() == FishingResultEvent.Result.FAILURE)
                return;
            Player player = event.getPlayer();

            // Single Fish Quest
            if (event.getLoot().getID() != null)
                this.executionBuilder("loot")
                        .player(player)
                        .root(event.getLoot().getID())
                        .progress(event.getAmount())
                        .buildAndExecute();

            // Group Fish Quest
            String[] lootGroup = event.getLoot().lootGroup();
            if (event.getLoot() != null && lootGroup != null)
                for (String group : lootGroup) {
                    this.executionBuilder("group")
                            .player(player)
                            .root(group)
                            .progress(event.getAmount())
                            .buildAndExecute();
                }
        }
    }
}
