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
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import net.momirealms.customfishing.adventure.AdventureManagerImpl;
import net.momirealms.customfishing.api.CustomFishingPlugin;
import net.momirealms.customfishing.api.common.Key;
import net.momirealms.customfishing.api.mechanic.condition.Condition;
import net.momirealms.customfishing.api.mechanic.item.BuildableItem;
import net.momirealms.customfishing.mechanic.item.ItemManagerImpl;
import net.momirealms.customfishing.setting.CFLocale;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.HashMap;

public class ItemCommand {

    public static ItemCommand INSTANCE = new ItemCommand();

    private final HashMap<String, String[]> completionMap = new HashMap<>();

    public CommandAPICommand getItemCommand() {
        return new CommandAPICommand("items")
                .withPermission("customfishing.command.items")
                .withSubcommands(
                    getSubCommand("loot"),
                    getSubCommand("util"),
                    getSubCommand("bait"),
                    getSubCommand("rod")
                );
    }

    private CommandAPICommand getSubCommand(String namespace) {
        completionMap.put(namespace, CustomFishingPlugin.get()
                .getItemManager()
                .getAllItemsKey()
                .stream()
                .filter(it -> it.namespace().equals(namespace))
                .map(Key::value)
                .toList().toArray(new String[0]));
        return new CommandAPICommand(namespace)
                .withSubcommands(
                        getCommand(namespace),
                        giveCommand(namespace)
                );
    }

    private CommandAPICommand getCommand(String namespace) {
        return new CommandAPICommand("get")
                .withArguments(new StringArgument("id")
                        .replaceSuggestions(ArgumentSuggestions.strings(
                                info -> completionMap.get(namespace)
                        )))
                .withOptionalArguments(new IntegerArgument("amount", 1))
                .executesPlayer((player, args) -> {
                    String id = (String) args.get("id");
                    assert id != null;
                    int amount = (int) args.getOrDefault("amount", 1);
                    ItemStack item = CustomFishingPlugin.get().getItemManager().build(player, namespace, id, new Condition(player).getArgs());
                    if (item != null) {
                        int actual = ItemManagerImpl.giveCertainAmountOfItem(player, item, amount);
                        AdventureManagerImpl.getInstance().sendMessageWithPrefix(player, CFLocale.MSG_Get_Item.replace("{item}", id).replace("{amount}", String.valueOf(actual)));
                    } else {
                        AdventureManagerImpl.getInstance().sendMessageWithPrefix(player, CFLocale.MSG_Item_Not_Exists);
                    }
                });
    }

    @SuppressWarnings("unchecked")
    private CommandAPICommand giveCommand(String namespace) {
        return new CommandAPICommand("give")
                .withArguments(new EntitySelectorArgument.ManyPlayers("player"))
                .withArguments(new StringArgument("id")
                        .replaceSuggestions(ArgumentSuggestions.strings(
                                info -> completionMap.get(namespace)
                        )))
                .withOptionalArguments(new IntegerArgument("amount", 1))
                .executes((sender, args) -> {
                    Collection<Player> players = (Collection<Player>) args.get("player");
                    String id = (String) args.get("id");
                    int amount = (int) args.getOrDefault("amount", 1);
                    BuildableItem buildableItem = CustomFishingPlugin.get().getItemManager().getBuildableItem(namespace, id);
                    if (buildableItem != null) {
                        assert players != null;
                        for (Player player : players) {
                            ItemStack item = CustomFishingPlugin.get().getItemManager().build(player, namespace, id, new Condition(player).getArgs());
                            int actual = ItemManagerImpl.giveCertainAmountOfItem(player, item, amount);
                            AdventureManagerImpl.getInstance().sendMessageWithPrefix(sender, CFLocale.MSG_Give_Item.replace("{item}", id).replace("{amount}", String.valueOf(actual)).replace("{player}", player.getName()));
                        }
                    } else {
                        AdventureManagerImpl.getInstance().sendMessageWithPrefix(sender, CFLocale.MSG_Item_Not_Exists);
                    }
                });
    }
}
