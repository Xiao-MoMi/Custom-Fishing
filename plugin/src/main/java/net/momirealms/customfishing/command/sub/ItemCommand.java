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

import de.tr7zw.changeme.nbtapi.NBTItem;
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
import net.momirealms.customfishing.api.util.LogUtils;
import net.momirealms.customfishing.setting.CFLocale;
import net.momirealms.customfishing.util.ItemUtils;
import net.momirealms.customfishing.util.NBTUtils;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ItemCommand {

    public static ItemCommand INSTANCE = new ItemCommand();

    private final HashMap<String, String[]> completionMap = new HashMap<>();

    public CommandAPICommand getItemCommand() {
        return new CommandAPICommand("items")
                .withSubcommands(
                    getSubCommand("item"),
                    getSubCommand("util"),
                    getSubCommand("bait"),
                    getSubCommand("rod"),
                    getSubCommand("hook")
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
                        giveCommand(namespace),
                        importCommand(namespace)
                );
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private CommandAPICommand importCommand(String namespace) {
        return new CommandAPICommand("import")
                .withArguments(new StringArgument("key"))
                .withOptionalArguments(new StringArgument("file"))
                .executesPlayer((player, args) -> {
                    String key = (String) args.get("key");
                    String fileName = args.getOrDefault("file","import") + ".yml";
                    ItemStack itemStack = player.getInventory().getItemInMainHand();
                    if (itemStack.getType() == Material.AIR)
                        return;
                    File file = new File(CustomFishingPlugin.get().getDataFolder(),
                            "contents" + File.separator + namespace + File.separator + fileName);
                    try {
                        if (!file.exists()) {
                            file.createNewFile();
                        }
                        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                        config.set(key + ".material", itemStack.getType().toString());
                        config.set(key + ".amount", itemStack.getAmount());
                        Map<String, Object> nbtMap = NBTUtils.compoundToMap(new NBTItem(itemStack));
                        if (nbtMap.size() != 0) {
                            config.createSection(key + ".nbt", nbtMap);
                        }
                        try {
                            config.save(file);
                            AdventureManagerImpl.getInstance().sendMessageWithPrefix(player, "Imported! Saved to " + file.getAbsolutePath());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } catch (IOException e) {
                        LogUtils.warn("Failed to create imported file.", e);
                    }
                });
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
                        int actual = ItemUtils.giveItem(player, item, amount);
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
                            int actual = ItemUtils.giveItem(player, item, amount);
                            AdventureManagerImpl.getInstance().sendMessageWithPrefix(sender, CFLocale.MSG_Give_Item.replace("{item}", id).replace("{amount}", String.valueOf(actual)).replace("{player}", player.getName()));
                        }
                    } else {
                        AdventureManagerImpl.getInstance().sendMessageWithPrefix(sender, CFLocale.MSG_Item_Not_Exists);
                    }
                });
    }
}
