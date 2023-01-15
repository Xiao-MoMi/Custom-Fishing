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

package net.momirealms.customfishing.competition.ranking;

import net.momirealms.customfishing.competition.CompetitionPlayer;

import java.util.Iterator;

public interface RankingInterface {

    void clear();
    CompetitionPlayer getCompetitionPlayer(String player);
    Iterator<String> getIterator();
    int getSize();
    String getPlayerRank(String player);
    float getPlayerScore(String player);
    CompetitionPlayer[] getTop3Player();
    void refreshData(String player, float score);
    void setData(String player, float score);
    float getFirstScore();
    float getSecondScore();
    float getThirdScore();
    String getFirstPlayer();
    String getSecondPlayer();
    String getThirdPlayer();
}
