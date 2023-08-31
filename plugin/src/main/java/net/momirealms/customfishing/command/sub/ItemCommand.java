package net.momirealms.customfishing.command.sub;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.TextArgument;
import net.momirealms.customfishing.adventure.AdventureManagerImpl;
import net.momirealms.customfishing.api.CustomFishingPlugin;
import net.momirealms.customfishing.api.common.Key;
import net.momirealms.customfishing.api.util.LogUtils;
import net.momirealms.customfishing.setting.Locale;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

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
                        int actual = giveCertainAmountOfItem(player, item, amount);
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
                            int actual = giveCertainAmountOfItem(player, item, amount);
                            AdventureManagerImpl.getInstance().sendMessageWithPrefix(sender, Locale.MSG_Give_Item.replace("{item}", id).replace("{amount}", String.valueOf(actual)).replace("{player}", player.getName()));
                        }
                    } else {
                        AdventureManagerImpl.getInstance().sendMessageWithPrefix(sender, Locale.MSG_Item_Not_Exists);
                    }
                });
    }

    private int giveCertainAmountOfItem(Player player, ItemStack itemStack, int amount) {
        PlayerInventory inventory = player.getInventory();
        String metaStr = itemStack.getItemMeta().getAsString();
        int maxStackSize = itemStack.getMaxStackSize();

        if (amount > maxStackSize * 100) {
            LogUtils.warn("Detected too many items spawning. Lowering the amount to " + (maxStackSize * 100));
            amount = maxStackSize * 100;
        }

        int actualAmount = amount;

        for (ItemStack other : inventory.getStorageContents()) {
            if (other != null) {
                if (other.getType() == itemStack.getType() && other.getItemMeta().getAsString().equals(metaStr)) {
                    if (other.getAmount() < maxStackSize) {
                        int delta = maxStackSize - other.getAmount();
                        if (amount > delta) {
                            other.setAmount(maxStackSize);
                            amount -= delta;
                        } else {
                            other.setAmount(amount + other.getAmount());
                            return actualAmount;
                        }
                    }
                }
            }
        }

        if (amount > 0) {
            for (ItemStack other : inventory.getStorageContents()) {
                if (other == null) {
                    if (amount > maxStackSize) {
                        amount -= maxStackSize;
                        ItemStack cloned = itemStack.clone();
                        cloned.setAmount(maxStackSize);
                        inventory.addItem(cloned);
                    } else {
                        ItemStack cloned = itemStack.clone();
                        cloned.setAmount(amount);
                        inventory.addItem(cloned);
                        return actualAmount;
                    }
                }
            }
        }

        if (amount > 0) {
            for (int i = 0; i < amount / maxStackSize; i++) {
                ItemStack cloned = itemStack.clone();
                cloned.setAmount(maxStackSize);
                player.getWorld().dropItem(player.getLocation(), cloned);
            }
            int left = amount % maxStackSize;
            if (left != 0) {
                ItemStack cloned = itemStack.clone();
                cloned.setAmount(left);
                player.getWorld().dropItem(player.getLocation(), cloned);
            }
        }

        return actualAmount;
    }
}
