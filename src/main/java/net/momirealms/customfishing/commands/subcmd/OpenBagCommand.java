package net.momirealms.customfishing.commands.subcmd;

import net.momirealms.customfishing.CustomFishing;
import net.momirealms.customfishing.commands.AbstractSubCommand;
import net.momirealms.customfishing.manager.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class OpenBagCommand extends AbstractSubCommand {

    public static final OpenBagCommand INSTANCE = new OpenBagCommand();

    private OpenBagCommand() {
        super("forceopenbag");
    }

    @Override
    public boolean onCommand(CommandSender sender, List<String> args) {
        if (!ConfigManager.enableFishingBag
                || super.lackArgs(sender, 1, args.size())
                || playerNotOnline(sender, args.get(0))
        ) return true;
        Player viewer = Bukkit.getPlayer(args.get(0));
        assert viewer != null;
        viewer.closeInventory();
        CustomFishing.getInstance().getBagDataManager().openFishingBag(viewer, viewer, false);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, List<String> args) {
        if (ConfigManager.enableFishingBag && args.size() == 1) {
            return filterStartingWith(online_players(), args.get(0));
        }
        return null;
    }
}