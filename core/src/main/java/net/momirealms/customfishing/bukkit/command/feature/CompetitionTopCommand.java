/*
 *  Copyright (C) <2024> <XiaoMoMi>
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

package net.momirealms.customfishing.bukkit.command.feature;

import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.api.mechanic.competition.FishingCompetition;
import net.momirealms.customfishing.api.mechanic.competition.RankingProvider;
import net.momirealms.customfishing.api.mechanic.context.Context;
import net.momirealms.customfishing.api.mechanic.context.ContextKeys;
import net.momirealms.customfishing.api.mechanic.misc.value.DynamicText;
import net.momirealms.customfishing.bukkit.command.BukkitCommandFeature;
import net.momirealms.customfishing.common.command.CustomFishingCommandManager;
import net.momirealms.customfishing.common.helper.AdventureHelper;
import net.momirealms.customfishing.common.locale.MessageConstants;
import net.momirealms.customfishing.common.locale.TranslationManager;
import net.momirealms.customfishing.common.sender.Sender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;

public class CompetitionTopCommand extends BukkitCommandFeature<CommandSender> {

    public CompetitionTopCommand(CustomFishingCommandManager<CommandSender> commandManager) {
        super(commandManager);
    }

    @Override
    public Command.Builder<? extends CommandSender> assembleCommand(CommandManager<CommandSender> manager, Command.Builder<CommandSender> builder) {
        return builder
                .handler(context -> {
                    FishingCompetition competition = BukkitCustomFishingPlugin.getInstance().getCompetitionManager().getOnGoingCompetition();
                    if (competition == null) {
                        handleFeedback(context, MessageConstants.COMMAND_COMPETITION_FAILURE_NO_COMPETITION);
                        return;
                    }
                    String[] texts = competition.getConfig().leaderboardTexts();
                    if (texts.length == 0) {
                        handleFeedback(context, MessageConstants.COMMAND_COMPETITION_FAILURE_NO_LEADERBOARD);
                        return;
                    }
                    // the sender's own placeholders, the same the bossbar shows
                    Player player = context.sender() instanceof Player sender ? sender : null;
                    RankingProvider ranking = competition.getRanking();
                    double score = player == null ? 0 : ranking.getPlayerScore(player.getName());
                    int rank = player == null ? -1 : ranking.getPlayerRank(player.getName());
                    String fish = player == null ? null : ranking.getFish(player.getName());
                    double shownScore = competition.getGoal().isReversed() ? -score : score;
                    Context<Player> placeholders = Context.player(player);
                    placeholders.arg(ContextKeys.SCORE_FORMATTED, score == 0 ? TranslationManager.miniMessageTranslation(MessageConstants.COMPETITION_NO_SCORE.build().key()) : String.format("%.2f", shownScore));
                    placeholders.arg(ContextKeys.SCORE, shownScore);
                    placeholders.arg(ContextKeys.RANK, rank != -1 ? String.valueOf(rank) : TranslationManager.miniMessageTranslation(MessageConstants.COMPETITION_NO_RANK.build().key()));
                    placeholders.arg(ContextKeys.FISH, fish == null ? "" : fish);
                    placeholders.combine(competition.getPublicContext());

                    Sender audience = getSenderFactory().wrap(context.sender());
                    for (String line : texts) {
                        DynamicText text = new DynamicText(player, line);
                        text.update(placeholders.placeholderMap());
                        audience.sendMessage(AdventureHelper.miniMessage(text.getLatestValue()));
                    }
                });
    }

    @Override
    public String getFeatureID() {
        return "competition_top";
    }
}
