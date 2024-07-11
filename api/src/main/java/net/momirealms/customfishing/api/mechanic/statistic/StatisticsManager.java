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

package net.momirealms.customfishing.api.mechanic.statistic;

import net.momirealms.customfishing.common.plugin.feature.Reloadable;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Interface for managing statistics
 */
public interface StatisticsManager extends Reloadable {

    /**
     * Retrieves the members of a statistics category identified by the given key.
     *
     * @param key the key identifying the statistics category
     * @return a list of category member identifiers as strings
     */
    @NotNull
    List<String> getCategoryMembers(String key);
}
