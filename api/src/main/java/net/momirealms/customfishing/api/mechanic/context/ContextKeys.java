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

package net.momirealms.customfishing.api.mechanic.context;

import net.momirealms.customfishing.api.mechanic.competition.CompetitionGoal;
import org.bukkit.Location;

import java.util.Objects;

public class ContextKeys<T> {

    public static final ContextKeys<Location> LOCATION = of("location", Location.class);
    public static final ContextKeys<Integer> X = of("x", Integer.class);
    public static final ContextKeys<Integer> Y = of("y", Integer.class);
    public static final ContextKeys<Integer> Z = of("z", Integer.class);
    public static final ContextKeys<String> WORLD = of("world", String.class);
    public static final ContextKeys<String> ID = of("id", String.class);
    public static final ContextKeys<Boolean> OPEN_WATER = of("open_water", Boolean.class);
    public static final ContextKeys<String> TYPE = of("type", String.class);
    public static final ContextKeys<Float> SIZE = of("SIZE", Float.class);
    public static final ContextKeys<String> SIZE_FORMATTED = of("size", String.class);
    public static final ContextKeys<Double> PRICE = of("PRICE", Double.class);
    public static final ContextKeys<String> PRICE_FORMATTED = of("price", String.class);
    public static final ContextKeys<String> SURROUNDING = of("surrounding", String.class);
    public static final ContextKeys<String> TEMP_NEAR_PLAYER = of("near", String.class);
    public static final ContextKeys<String> ROD = of("rod", String.class);
    public static final ContextKeys<String> BAIT = of("bait", String.class);
    public static final ContextKeys<String> HOOK = of("hook", String.class);
    public static final ContextKeys<Boolean> IN_BAG = of("in_bag", Boolean.class);
    public static final ContextKeys<CompetitionGoal> GOAL = of("goal", CompetitionGoal.class);
    public static final ContextKeys<String> HOUR = of("hour", String.class);
    public static final ContextKeys<String> MINUTE = of("minute", String.class);
    public static final ContextKeys<String> SECOND = of("second", String.class);
    public static final ContextKeys<Integer> SECONDS = of("seconds", Integer.class);
    public static final ContextKeys<String> PLAYER = of("player", String.class);
    public static final ContextKeys<String> SCORE = of("score", String.class);
    public static final ContextKeys<String> RANK = of("rank", String.class);
    public static final ContextKeys<Location> HOOK_LOCATION = of("hook_location", Location.class);
    public static final ContextKeys<Integer> HOOK_X = of("hook_x", Integer.class);
    public static final ContextKeys<Integer> HOOK_Y = of("hook_y", Integer.class);
    public static final ContextKeys<Integer> HOOK_Z = of("hook_z", Integer.class);
    public static final ContextKeys<String> MONEY = of("money", String.class);
    public static final ContextKeys<String> MONEY_FORMATTED = of("money_formatted", String.class);
    public static final ContextKeys<String> REST = of("rest", String.class);
    public static final ContextKeys<String> REST_FORMATTED = of("rest_formatted", String.class);
    public static final ContextKeys<Integer> SOLD_ITEM_AMOUNT = of("sold_item_amount", Integer.class);
    public static final ContextKeys<Double> WEIGHT = of("0", Double.class);

    private final String key;
    private final Class<T> type;

    protected ContextKeys(String key, Class<T> type) {
        this.key = key;
        this.type = type;
    }

    public String key() {
        return key;
    }

    public Class<T> type() {
        return type;
    }

    public static <T> ContextKeys<T> of(String key, Class<T> type) {
        return new ContextKeys<T>(key, type);
    }

    @Override
    public final boolean equals(final Object other) {
        if (this == other) {
            return true;
        } else if (other != null && this.getClass() == other.getClass()) {
            ContextKeys<?> that = (ContextKeys) other;
            return Objects.equals(this.key, that.key);
        } else {
            return false;
        }
    }

    @Override
    public final int hashCode() {
        return Objects.hashCode(this.key);
    }
}
