package net.momirealms.customfishing.commands.subcmd;

import net.momirealms.customfishing.commands.AbstractSubCommand;
import net.momirealms.customfishing.commands.SubCommand;
import net.momirealms.customfishing.competition.CompetitionSchedule;
import net.momirealms.customfishing.manager.CompetitionManager;
import net.momirealms.customfishing.manager.MessageManager;
import net.momirealms.customfishing.util.AdventureUtil;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CompetitionCommand extends AbstractSubCommand {

    public static final SubCommand INSTANCE = new CompetitionCommand();

    public CompetitionCommand() {
        super("competition", null);
    }

    @Override
    public boolean onCommand(CommandSender sender, List<String> args) {
        if (args.size() < 1){
            AdventureUtil.sendMessage(sender, MessageManager.prefix + MessageManager.lackArgs);
            return true;
        }
        if (args.get(0).equals("start")){
            if (args.size() < 2){
                AdventureUtil.sendMessage(sender, MessageManager.prefix + MessageManager.lackArgs);
                return true;
            }
            if (CompetitionSchedule.startCompetition(args.get(1))){
                AdventureUtil.sendMessage(sender, MessageManager.prefix + MessageManager.forceSuccess);
            } else {
                AdventureUtil.sendMessage(sender, MessageManager.prefix + MessageManager.forceFailure);
            }
        } else if (args.get(0).equals("end")) {
            CompetitionSchedule.endCompetition();
            AdventureUtil.sendMessage(sender, MessageManager.prefix + MessageManager.forceEnd);
        } else if (args.get(0).equals("cancel")) {
            CompetitionSchedule.cancelCompetition();
            AdventureUtil.sendMessage(sender, MessageManager.prefix + MessageManager.forceEnd);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, List<String> args) {
        if (args.size() == 1) {
            return List.of("start","end","cancel");
        }
        if (args.size() == 2 && args.get(0).equals("start")) {
            return competitions();
        }
        return super.onTabComplete(sender, args);
    }

    private List<String> competitions() {
        return new ArrayList<>(CompetitionManager.competitionsC.keySet());
    }
}
