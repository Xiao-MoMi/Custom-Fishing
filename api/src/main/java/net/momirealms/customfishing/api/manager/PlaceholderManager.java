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

package net.momirealms.customfishing.api.manager;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public interface PlaceholderManager {

    String setPlaceholders(Player player, String text);

    String setPlaceholders(OfflinePlayer player, String text);

    List<String> detectPlaceholders(String text);

    String getSingleValue(@Nullable Player player, String placeholder, Map<String, String> placeholders);

    String parse(@Nullable OfflinePlayer player, String text, Map<String, String> placeholders);

    String parseCacheable(Player player, String text);

    List<String> parse(@Nullable OfflinePlayer player, List<String> list, Map<String, String> replacements);
}
