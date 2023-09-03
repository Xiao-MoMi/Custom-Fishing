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
 *
 */

package net.momirealms.customfishing.command.sub;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.TextArgument;
import net.momirealms.customfishing.adventure.AdventureManagerImpl;
import net.momirealms.customfishing.api.CustomFishingPlugin;
import net.momirealms.customfishing.api.common.Key;
import net.momirealms.customfishing.mechanic.item.ItemManagerImpl;
import net.momirealms.customfishing.setting.Locale;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;

public class ItemCommand {

    public static ItemCommand INSTANCE = new ItemCommand();

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
        Collection<String> items = CustomFishingPlugin.get()
                .getItemManager()
                .getAllItemsKey()
                .stream()
                .filter(it -> it.namespace().equals(namespace))
                .map(Key::value)
                .toList();
        return new CommandAPICommand(namespace)
                .withSubcommands(
                        getCommand(namespace, items),
                        giveCommand(namespace, items)
                );
    }

    private CommandAPICommand getCommand(String namespace, Collection<String> items) {
        return new CommandAPICommand("get")
                .withArguments(new TextArgument("id").replaceSuggestions(ArgumentSuggestions.strings(items)))
                .withOptionalArguments(new IntegerArgument("amount", 1))
                .executesPlayer((player, args) -> {
                    String id = (String) args.get("id");
                    int amount = (int) args.getOrDefault("amount", 1);
                    ItemStack item = CustomFishingPlugin.get().getItemManager().build(player, namespace, id);
                    if (item != null) {
                        int actual = ItemManagerImpl.giveCertainAmountOfItem(player, item, amount);
                        AdventureManagerImpl.getInstance().sendMessageWithPrefix(player, Locale.MSG_Get_Item.replace("{item}", id).replace("{amount}", String.valueOf(actual)));
                    } else {
                        AdventureManagerImpl.getInstance().sendMessageWithPrefix(player, Locale.MSG_Item_Not_Exists);
                    }
                });
    }

    private CommandAPICommand giveCommand(String namespace, Collection<String> items) {
        return new CommandAPICommand("give")
                .withArguments(new EntitySelectorArgument.ManyPlayers("player"))
                .withArguments(new TextArgument("id").replaceSuggestions(ArgumentSuggestions.strings(items)))
                .withOptionalArguments(new IntegerArgument("amount", 1))
                .executes((sender, args) -> {
                    Collection<Player> players = (Collection<Player>) args.get("player");
                    String id = (String) args.get("id");
                    int amount = (int) args.getOrDefault("amount", 1);
                    ItemStack item = CustomFishingPlugin.get().getItemManager().build(players.stream().findAny().get(), namespace, id);
                    if (item != null) {
                        for (Player player : players) {
                            int actual = ItemManagerImpl.giveCertainAmountOfItem(player, item, amount);
                            AdventureManagerImpl.getInstance().sendMessageWithPrefix(sender, Locale.MSG_Give_Item.replace("{item}", id).replace("{amount}", String.valueOf(actual)).replace("{player}", player.getName()));
                        }
                    } else {
                        AdventureManagerImpl.getInstance().sendMessageWithPrefix(sender, Locale.MSG_Item_Not_Exists);
                    }
                });
    }
}
