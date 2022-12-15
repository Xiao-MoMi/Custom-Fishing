package net.momirealms.customfishing.commands.subcmd;

import net.momirealms.customfishing.CustomFishing;
import net.momirealms.customfishing.commands.AbstractSubCommand;
import net.momirealms.customfishing.commands.SubCommand;
import net.momirealms.customfishing.manager.ConfigManager;
import net.momirealms.customfishing.manager.MessageManager;
import net.momirealms.customfishing.util.AdventureUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OpenBagCommand extends AbstractSubCommand {

    public static final SubCommand INSTANCE = new OpenBagCommand();

    private OpenBagCommand() {
        super("forceopenbag", null);
    }

    @Override
    public boolean onCommand(CommandSender sender, List<String> args) {
        if (!ConfigManager.enableFishingBag) return true;
        if (args.size() < 1){
            AdventureUtil.sendMessage(sender, MessageManager.prefix + MessageManager.lackArgs);
            return true;
        }
        if (args.size() == 1) {
            if (!sender.hasPermission("customfishing.admin")) {
                AdventureUtil.sendMessage(sender, MessageManager.prefix + MessageManager.noPerm);
                return true;
            }
            Player viewer = Bukkit.getPlayer(args.get(0));
            if (viewer == null) {
                AdventureUtil.sendMessage(sender, MessageManager.prefix + MessageManager.playerNotExist);
                return true;
            }
            viewer.closeInventory();
            CustomFishing.plugin.getBagDataManager().openFishingBag(viewer, viewer);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, List<String> args) {
        if (!ConfigManager.enableFishingBag) return null;
        if (!sender.hasPermission("customfishing.admin")) return null;
        if (args.size() == 1) {
            List<String> arrayList = new ArrayList<>();
            for (String cmd : online_players()) {
                if (cmd.startsWith(args.get(0)))
                    arrayList.add(cmd);
            }
            return arrayList;
        }
        return super.onTabComplete(sender, args);
    }
}
