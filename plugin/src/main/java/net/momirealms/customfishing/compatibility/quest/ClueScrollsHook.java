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

package net.momirealms.customfishing.compatibility.quest;

import com.electro2560.dev.cluescrolls.api.*;
import net.momirealms.customfishing.api.CustomFishingPlugin;
import net.momirealms.customfishing.api.event.FishingResultEvent;
import net.momirealms.customfishing.api.mechanic.loot.Loot;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ClueScrollsHook implements Listener {

    private final CustomClue idClue;
    private final CustomClue groupClue;

    public ClueScrollsHook() {
        idClue = ClueScrollsAPI.getInstance().registerCustomClue(CustomFishingPlugin.getInstance(), "loot", new ClueConfigData("id", DataType.STRING));
        groupClue = ClueScrollsAPI.getInstance().registerCustomClue(CustomFishingPlugin.getInstance(), "group", new ClueConfigData("group", DataType.STRING));
    }

    public void register() {
        Bukkit.getPluginManager().registerEvents(this, CustomFishingPlugin.get());
    }

    @EventHandler
    public void onFish(FishingResultEvent event) {
        if (event.isCancelled() || event.getResult() == FishingResultEvent.Result.FAILURE)
            return;

        final Player player = event.getPlayer();
        idClue.handle(
                player,
                event.getAmount(),
                new ClueDataPair("id", "any")
        );

        Loot loot = event.getLoot();
        if (loot != null) {
            idClue.handle(
                    player,
                    event.getAmount(),
                    new ClueDataPair("id", loot.getID())
            );
        }

        if (loot != null && loot.getLootGroup() != null) {
            for (String group : event.getLoot().getLootGroup()) {
                groupClue.handle(
                        player,
                        event.getAmount(),
                        new ClueDataPair("group", group)
                );
            }
        }
    }
}
