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

package net.momirealms.customfishing.api.mechanic.competition;

import net.kyori.adventure.util.Index;
import net.momirealms.customfishing.common.locale.MessageConstants;
import net.momirealms.customfishing.common.locale.TranslationManager;
import net.momirealms.customfishing.common.util.RandomUtils;
import net.momirealms.customfishing.common.util.TriConsumer;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Represents different goals for a fishing competition.
 */
public final class CompetitionGoal {

    public static final CompetitionGoal CATCH_AMOUNT = new CompetitionGoal(
            "catch_amount", false,
            ((rankingProvider, player, score) -> rankingProvider.refreshData(player, 1)),
            () -> Optional.ofNullable(TranslationManager.miniMessageTranslation(MessageConstants.GOAL_CATCH_AMOUNT.build().key())).orElse("catch_amount")
    );
    public static final CompetitionGoal TOTAL_SCORE = new CompetitionGoal(
          "total_score", false,
            (RankingProvider::refreshData),
            () -> Optional.ofNullable(TranslationManager.miniMessageTranslation(MessageConstants.GOAL_TOTAL_SCORE.build().key())).orElse("total_score")
    );
    public static final CompetitionGoal MAX_SIZE = new CompetitionGoal(
            "max_size", false,
            ((rankingProvider, player, score) -> {
                if (rankingProvider.getPlayerScore(player) < score) {
                    rankingProvider.setData(player, score);
                }
            }),
            () -> Optional.ofNullable(TranslationManager.miniMessageTranslation(MessageConstants.GOAL_MAX_SIZE.build().key())).orElse("max_size")
    );
    public static final CompetitionGoal MIN_SIZE = new CompetitionGoal(
            "min_size", true,
            ((rankingProvider, player, score) -> {
                if (-rankingProvider.getPlayerScore(player) > score) {
                    rankingProvider.setData(player, -score);
                }
            }),
            () -> Optional.ofNullable(TranslationManager.miniMessageTranslation(MessageConstants.GOAL_MIN_SIZE.build().key())).orElse("min_size")
    );
    public static final CompetitionGoal TOTAL_SIZE = new CompetitionGoal(
           "total_size", false,
            (RankingProvider::refreshData),
            () -> Optional.ofNullable(TranslationManager.miniMessageTranslation(MessageConstants.GOAL_TOTAL_SIZE.build().key())).orElse("total_size")
    );
    public static final CompetitionGoal RANDOM = new CompetitionGoal(
           "random", false,
            (rankingProvider, player, score) -> {},
            () -> "random"
    );

    private static final CompetitionGoal[] values = new CompetitionGoal[] {
        CATCH_AMOUNT, TOTAL_SCORE, MAX_SIZE, MIN_SIZE, TOTAL_SIZE, RANDOM
    };

    private static final Index<String, CompetitionGoal> index = Index.create(CompetitionGoal::key, values());

    /**
     * Gets an array containing all defined competition goals.
     *
     * @return An array of all competition goals.
     */
    public static CompetitionGoal[] values() {
        return values;
    }

    /**
     * Gets the index of competition goals by their keys.
     *
     * @return An index mapping keys to competition goals.
     */
    public static Index<String, CompetitionGoal> index() {
        return index;
    }

    /**
     * Gets a randomly selected competition goal.
     *
     * @return A randomly selected competition goal.
     */
    public static CompetitionGoal getRandom() {
        return CompetitionGoal.values()[RandomUtils.generateRandomInt(0, values.length - 1)];
    }

    private final String key;
    private final TriConsumer<RankingProvider, String, Double> scoreConsumer;
    private final Supplier<String> nameSupplier;
    private final boolean reversed;

    private CompetitionGoal(String key, boolean reversed, TriConsumer<RankingProvider, String, Double> scoreConsumer, Supplier<String> nameSupplier) {
        this.key = key;
        this.reversed = reversed;
        this.scoreConsumer = scoreConsumer;
        this.nameSupplier = nameSupplier;
    }

    /**
     * Gets the key representing this competition goal.
     *
     * @return The key of the competition goal.
     */
    public String key() {
        return key;
    }

    /**
     * Is the score reversed
     *
     * @return reversed or not
     */
    public boolean isReversed() {
        return reversed;
    }

    /**
     * Refreshes the score for the player in the ranking provider.
     *
     * @param ranking the ranking provider.
     * @param player the player.
     * @param score the score.
     */
    public void refreshScore(RankingProvider ranking, Player player, Double score) {
        scoreConsumer.accept(ranking, player.getName(), score);
    }

    @Override
    public String toString() {
        return nameSupplier.get();
    }
}
