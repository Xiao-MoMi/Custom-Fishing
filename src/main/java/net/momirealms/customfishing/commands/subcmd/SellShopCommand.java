package net.momirealms.customfishing.commands.subcmd;

import net.momirealms.customfishing.CustomFishing;
import net.momirealms.customfishing.commands.AbstractSubCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class SellShopCommand extends AbstractSubCommand {

    public static final SellShopCommand INSTANCE = new SellShopCommand();

    public SellShopCommand() {
        super("sellshop");
    }

    @Override
    public boolean onCommand(CommandSender sender, List<String> args) {
        if (super.lackArgs(sender, 1, args.size())
                || playerNotOnline(sender, args.get(0))
        ) return true;
        Player player = Bukkit.getPlayer(args.get(0));
        assert player != null;
        player.closeInventory();
        CustomFishing.getInstance().getSellManager().openGuiForPlayer(player);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, List<String> args) {
        if (args.size() == 1) {
            return filterStartingWith(online_players(), args.get(0));
        }
        return null;
    }
}
