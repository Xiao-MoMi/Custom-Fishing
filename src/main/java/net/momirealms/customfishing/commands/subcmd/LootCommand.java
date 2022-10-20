package net.momirealms.customfishing.commands.subcmd;

import net.momirealms.customfishing.commands.AbstractSubCommand;
import net.momirealms.customfishing.commands.SubCommand;
import net.momirealms.customfishing.manager.LootManager;
import net.momirealms.customfishing.manager.MessageManager;
import net.momirealms.customfishing.object.loot.DroppedItem;
import net.momirealms.customfishing.object.loot.Loot;
import net.momirealms.customfishing.util.AdventureUtil;
import net.momirealms.customfishing.util.ItemStackUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LootCommand extends AbstractSubCommand {

    public static final SubCommand INSTANCE = new LootCommand();

    public LootCommand() {
        super("loot", null);
    }

    @Override
    public boolean onCommand(CommandSender sender, List<String> args) {
        if (args.size() < 2) {
            AdventureUtil.sendMessage(sender, MessageManager.prefix + MessageManager.lackArgs);
        }
        else if (args.get(0).equalsIgnoreCase("get")) {
            if (sender instanceof Player player){
                if (!loots().contains(args.get(1))){
                    AdventureUtil.sendMessage(sender, MessageManager.prefix + MessageManager.itemNotExist);
                    return true;
                }
                if (args.size() == 2){
                    ItemStackUtil.givePlayerLoot(player, args.get(1), 1);
                    AdventureUtil.playerMessage(player, MessageManager.prefix + MessageManager.getItem.replace("{Amount}", "1").replace("{Item}", args.get(1)));
                } else {
                    if (Integer.parseInt(args.get(2)) < 1){
                        AdventureUtil.sendMessage(sender, MessageManager.prefix + MessageManager.wrongAmount);
                        return true;
                    }
                    ItemStackUtil.givePlayerLoot(player, args.get(1), Integer.parseInt(args.get(2)));
                    AdventureUtil.playerMessage(player, MessageManager.prefix + MessageManager.getItem.replace("{Amount}", args.get(2)).replace("{Item}", args.get(1)));
                }
            } else {
                AdventureUtil.consoleMessage(MessageManager.prefix + MessageManager.noConsole);
            }
        }
        else if (args.get(0).equalsIgnoreCase("give")) {
            if (args.size() < 3){
                AdventureUtil.sendMessage(sender, MessageManager.prefix + MessageManager.lackArgs);
                return true;
            }
            Player player = Bukkit.getPlayer(args.get(1));
            if (player == null){
                AdventureUtil.sendMessage(sender, MessageManager.prefix + MessageManager.notOnline.replace("{Player}", args.get(1)));
                return true;
            }
            if (!loots().contains(args.get(2))){
                AdventureUtil.sendMessage(sender, MessageManager.prefix + MessageManager.itemNotExist);
                return true;
            }
            if (args.size() == 3){
                ItemStackUtil.givePlayerLoot(player, args.get(2), 1);
                super.giveItem(sender, args.get(1), args.get(2), 1);
            } else {
                if (Integer.parseInt(args.get(3)) < 1){
                    AdventureUtil.sendMessage(sender, MessageManager.prefix + MessageManager.wrongAmount);
                    return true;
                }
                ItemStackUtil.givePlayerLoot(player, args.get(2), Integer.parseInt(args.get(3)));
                super.giveItem(sender, args.get(1), args.get(2), Integer.parseInt(args.get(3)));
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, List<String> args) {
        if (args.size() == 1) {
            return List.of("get", "give");
        }
        if (args.size() == 2) {
            if (args.get(0).equals("get")) {
                List<String> arrayList = new ArrayList<>();
                for (String cmd : loots()) {
                    if (cmd.startsWith(args.get(1)))
                        arrayList.add(cmd);
                }
                return arrayList;
            }
            if (args.get(0).equals("give")) {
                List<String> arrayList = new ArrayList<>();
                for (String cmd : online_players()) {
                    if (cmd.startsWith(args.get(1)))
                        arrayList.add(cmd);
                }
                return arrayList;
            }
        }
        if (args.size() == 3) {
            if (args.get(0).equals("get")) {
                return List.of("1","2","4","8","16","32","64");
            }
            if (args.get(0).equals("give")) {
                List<String> arrayList = new ArrayList<>();
                for (String cmd : loots()) {
                    if (cmd.startsWith(args.get(2)))
                        arrayList.add(cmd);
                }
                return arrayList;
            }
        }
        if (args.size() == 4) {
            if (args.get(0).equals("give")) {
                return List.of("1","2","4","8","16","32","64");
            }
        }
        return super.onTabComplete(sender, args);
    }

    private List<String> loots() {
        List<String> loots = new ArrayList<>();
        for (Map.Entry<String, Loot> en : LootManager.WATERLOOTS.entrySet()) {
            if (en.getValue() instanceof DroppedItem) {
                loots.add(en.getKey());
            }
        }
        for (Map.Entry<String, Loot> en : LootManager.LAVALOOTS.entrySet()) {
            if (en.getValue() instanceof DroppedItem) {
                loots.add(en.getKey());
            }
        }
        return loots;
    }
}
