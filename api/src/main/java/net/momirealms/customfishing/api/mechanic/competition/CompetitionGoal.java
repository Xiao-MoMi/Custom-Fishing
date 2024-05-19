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
import net.momirealms.customfishing.common.util.RandomUtils;
import org.apache.logging.log4j.util.TriConsumer;
import org.bukkit.entity.Player;

public final class CompetitionGoal {

    public static final CompetitionGoal CATCH_AMOUNT = new CompetitionGoal(
            "catch_amount",
            ((rankingProvider, player, score) -> rankingProvider.refreshData(player, 1))
    );
    public static final CompetitionGoal TOTAL_SCORE = new CompetitionGoal(
          "total_score",
            (RankingProvider::refreshData)
    );
    public static final CompetitionGoal MAX_SIZE = new CompetitionGoal(
            "max_size",
            ((rankingProvider, player, score) -> {
                if (rankingProvider.getPlayerScore(player) < score) {
                    rankingProvider.setData(player, score);
                }
            })
    );
    public static final CompetitionGoal TOTAL_SIZE = new CompetitionGoal(
           "total_size",
            (RankingProvider::refreshData)
    );
    public static final CompetitionGoal RANDOM = new CompetitionGoal(
           "random",
            (rankingProvider, player, score) -> {}
    );

    private static final CompetitionGoal[] values = new CompetitionGoal[] {
        CATCH_AMOUNT, TOTAL_SCORE, MAX_SIZE, TOTAL_SIZE, RANDOM
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

    private CompetitionGoal(String key, TriConsumer<RankingProvider, String, Double> scoreConsumer) {
        this.key = key;
        this.scoreConsumer = scoreConsumer;
    }

    /**
     * Gets the key representing this competition goal.
     *
     * @return The key of the competition goal.
     */
    public String key() {
        return key;
    }

    public void refreshScore(RankingProvider ranking, Player player, Double score) {
        scoreConsumer.accept(ranking, player.getName(), score);
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
