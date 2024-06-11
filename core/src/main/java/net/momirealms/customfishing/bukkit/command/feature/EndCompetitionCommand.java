package net.momirealms.customfishing.bukkit.command.feature;

import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.api.mechanic.competition.FishingCompetition;
import net.momirealms.customfishing.bukkit.command.BukkitCommandFeature;
import net.momirealms.customfishing.common.command.CustomFishingCommandManager;
import net.momirealms.customfishing.common.locale.MessageConstants;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;

public class EndCompetitionCommand extends BukkitCommandFeature<CommandSender> {

    public EndCompetitionCommand(CustomFishingCommandManager<CommandSender> commandManager) {
        super(commandManager);
    }

    @Override
    public Command.Builder<? extends CommandSender> assembleCommand(CommandManager<CommandSender> manager, Command.Builder<CommandSender> builder) {
        return builder
                .flag(manager.flagBuilder("silent").withAliases("s").build())
                .handler(context -> {
                    FishingCompetition competition = BukkitCustomFishingPlugin.getInstance().getCompetitionManager().getOnGoingCompetition();
                    if (competition == null) {
                        handleFeedback(context, MessageConstants.COMMAND_COMPETITION_FAILURE_NO_COMPETITION);
                    } else {
                        competition.end(true);
                        handleFeedback(context, MessageConstants.COMMAND_COMPETITION_END_SUCCESS);
                    }
                });
    }

    @Override
    public String getFeatureID() {
        return "end_competition";
    }
}
