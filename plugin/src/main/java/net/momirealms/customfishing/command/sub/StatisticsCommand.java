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

package net.momirealms.customfishing.command.sub;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.*;
import net.momirealms.customfishing.adventure.AdventureManagerImpl;
import net.momirealms.customfishing.api.CustomFishingPlugin;
import net.momirealms.customfishing.api.mechanic.condition.Condition;
import net.momirealms.customfishing.api.mechanic.loot.Loot;
import net.momirealms.customfishing.api.mechanic.statistic.Statistics;
import net.momirealms.customfishing.api.util.LogUtils;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

public class StatisticsCommand {

    public static StatisticsCommand INSTANCE = new StatisticsCommand();

    private Collection<String> loots = new HashSet<>();

    public CommandAPICommand getStatisticsCommand() {
        loots = CustomFishingPlugin.get().getLootManager().getAllLoots().stream().filter(it -> !it.disableStats()).map(Loot::getID).toList();
        return new CommandAPICommand("statistics")
                .withSubcommands(
                        getSetCommand(),
                        getResetCommand(),
                        getQueryCommand(),
                        getAddCommand()
                );
    }

    @SuppressWarnings("unchecked")
    private CommandAPICommand getSetCommand() {
        return new CommandAPICommand("set")
                .withArguments(new EntitySelectorArgument.ManyPlayers("player"))
                .withArguments(new StringArgument("id").replaceSuggestions(ArgumentSuggestions.strings(loots)))
                .withArguments(new IntegerArgument("amount", 0))
                .executes((sender, args) -> {
                    Collection<Player> players = (Collection<Player>) args.get("player");
                    String id = (String) args.get("id");
                    int amount = (int) args.getOrDefault("amount", 0);
                    assert players != null;
                    Loot loot = CustomFishingPlugin.get().getLootManager().getLoot(id);
                    for (Player player : players) {
                        Statistics statistics = CustomFishingPlugin.get().getStatisticsManager().getStatistics(player.getUniqueId());
                        if (statistics != null) {
                            if (loot != null)
                                statistics.setData(id, amount);
                            else
                                throw new RuntimeException("Loot " + id + " doesn't exist.");
                        } else {
                            LogUtils.warn("Player " + player.getName() + "'s statistics data has not been loaded.");
                        }
                    }
                });
    }

    @SuppressWarnings("unchecked")
    private CommandAPICommand getResetCommand() {
        return new CommandAPICommand("reset")
                .withArguments(new EntitySelectorArgument.ManyPlayers("player"))
                .executes((sender, args) -> {
                    Collection<Player> players = (Collection<Player>) args.get("player");
                    assert players != null;
                    for (Player player : players) {
                        Statistics statistics = CustomFishingPlugin.get().getStatisticsManager().getStatistics(player.getUniqueId());
                        if (statistics != null) {
                            statistics.reset();
                        } else {
                            LogUtils.warn("Player " + player.getName() + "'s statistics data has not been loaded.");
                        }
                    }
                });
    }

    @SuppressWarnings("unchecked")
    private CommandAPICommand getAddCommand() {
        return new CommandAPICommand("add")
                .withArguments(new EntitySelectorArgument.ManyPlayers("player"))
                .withArguments(new StringArgument("id").replaceSuggestions(ArgumentSuggestions.strings(loots)))
                .withArguments(new IntegerArgument("amount", 0))
                .executes((sender, args) -> {
                    Collection<Player> players = (Collection<Player>) args.get("player");
                    String id = (String) args.get("id");
                    int amount = (int) args.getOrDefault("amount", 0);
                    assert players != null;
                    Loot loot = CustomFishingPlugin.get().getLootManager().getLoot(id);
                    for (Player player : players) {
                        Statistics statistics = CustomFishingPlugin.get().getStatisticsManager().getStatistics(player.getUniqueId());
                        if (statistics != null) {
                            if (loot != null)
                                statistics.addLootAmount(loot, new Condition(player), amount);
                            else
                                throw new RuntimeException("Loot " + id + " doesn't exist.");
                        } else {
                            LogUtils.warn("Player " + player.getName() + "'s statistics data has not been loaded.");
                        }
                    }
                });
    }

    private CommandAPICommand getQueryCommand() {
        return new CommandAPICommand("query")
                .withArguments(new PlayerArgument("player"))
                .executes((sender, args) -> {
                    Player player = (Player) args.get("player");
                    assert player != null;
                    Statistics statistics = CustomFishingPlugin.get().getStatisticsManager().getStatistics(player.getUniqueId());
                    if (statistics != null) {
                        var adventure = AdventureManagerImpl.getInstance();
                        for (Map.Entry<String, Integer> entry : statistics.getStatisticMap().entrySet()) {
                            adventure.sendMessage(sender, entry.getKey() + ": " + entry.getValue());
                        }
                    } else {
                        throw new RuntimeException("Player " + player.getName() + "'s statistics data has not been loaded.");
                    }
                });
    }
}
