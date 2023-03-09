package net.momirealms.customfishing.commands.subcmd;

import net.momirealms.customfishing.CustomFishing;
import net.momirealms.customfishing.commands.AbstractSubCommand;
import net.momirealms.customfishing.commands.SubCommand;
import net.momirealms.customfishing.manager.ConfigManager;
import net.momirealms.customfishing.manager.MessageManager;
import net.momirealms.customfishing.util.AdventureUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

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
            Player viewer = Bukkit.getPlayer(args.get(0));
            if (viewer == null) {
                AdventureUtil.sendMessage(sender, MessageManager.prefix + MessageManager.playerNotExist);
                return true;
            }
            viewer.closeInventory();
            CustomFishing.getInstance().getBagDataManager().openFishingBag(viewer, viewer, false);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, List<String> args) {
        if (!ConfigManager.enableFishingBag) return null;
        if (args.size() == 1) {
            return filterStartingWith(online_players(), args.get(0));
        }
        return super.onTabComplete(sender, args);
    }
}
