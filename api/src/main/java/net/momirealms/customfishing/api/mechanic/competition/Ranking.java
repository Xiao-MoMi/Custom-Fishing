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

import net.momirealms.customfishing.api.common.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

public interface Ranking {

    void clear();

    CompetitionPlayer getCompetitionPlayer(String player);

    Iterator<Pair<String, Double>> getIterator();

    int getSize();

    int getPlayerRank(String player);

    double getPlayerScore(String player);

    void refreshData(String player, double score);

    void setData(String player, double score);

    @Nullable
    String getPlayerAt(int rank);

    double getScoreAt(int rank);
}
