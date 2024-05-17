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

import net.kyori.adventure.key.Key;
import net.kyori.adventure.util.Index;

import java.util.concurrent.ThreadLocalRandom;

public final class CompetitionGoal {

    public static final CompetitionGoal CATCH_AMOUNT = new CompetitionGoal(Key.key("customfishing", "catch_amount"));
    public static final CompetitionGoal TOTAL_SCORE = new CompetitionGoal(Key.key("customfishing", "total_score"));
    public static final CompetitionGoal MAX_SIZE = new CompetitionGoal(Key.key("customfishing", "max_size"));
    public static final CompetitionGoal TOTAL_SIZE = new CompetitionGoal(Key.key("customfishing", "total_size"));
    public static final CompetitionGoal RANDOM = new CompetitionGoal(Key.key("customfishing", "random"));

    private static final CompetitionGoal[] values = new CompetitionGoal[] {
        CATCH_AMOUNT, TOTAL_SCORE, MAX_SIZE, TOTAL_SIZE, RANDOM
    };

    private static final Index<Key, CompetitionGoal> index = Index.create(CompetitionGoal::key, values());

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
    public static Index<Key, CompetitionGoal> index() {
        return index;
    }

    /**
     * Gets a randomly selected competition goal.
     *
     * @return A randomly selected competition goal.
     */
    public static CompetitionGoal getRandom() {
        return CompetitionGoal.values()[ThreadLocalRandom.current().nextInt(CompetitionGoal.values().length - 1)];
    }

    private final Key key;

    private CompetitionGoal(Key key) {
        this.key = key;
    }

    /**
     * Gets the key representing this competition goal.
     *
     * @return The key of the competition goal.
     */
    public Key key() {
        return key;
    }
}
