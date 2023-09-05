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

package net.momirealms.customfishing.api.util;

import net.momirealms.customfishing.api.CustomFishingPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

public final class LogUtils {

    public static void info(@NotNull String s) {
        CustomFishingPlugin.getInstance().getLogger().info(s);
    }

    public static void warn(@NotNull String s) {
        CustomFishingPlugin.getInstance().getLogger().warning(s);
    }

    public static void severe(@NotNull String s) {
        CustomFishingPlugin.getInstance().getLogger().severe(s);
    }

    public static void warn(@NotNull String s, Throwable t) {
        CustomFishingPlugin.getInstance().getLogger().log(Level.WARNING, s, t);
    }

    public static void severe(@NotNull String s, Throwable t) {
        CustomFishingPlugin.getInstance().getLogger().log(Level.SEVERE, s, t);
    }

    private LogUtils() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }

}
