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

package net.momirealms.customfishing.api.mechanic.context;

import net.momirealms.customfishing.api.mechanic.competition.CompetitionGoal;
import net.momirealms.customfishing.api.mechanic.effect.Effect;
import net.momirealms.customfishing.api.mechanic.loot.LootType;
import net.momirealms.customfishing.api.mechanic.totem.ActiveTotemList;
import org.bukkit.Location;
import org.bukkit.entity.FishHook;
import org.bukkit.inventory.EquipmentSlot;

import java.util.Objects;

/**
 * Represents keys for accessing context values with specific types.
 *
 * @param <T> the type of the value associated with the context key.
 */
public class ContextKeys<T> {

    public static final ContextKeys<Location> LOCATION = of("location", Location.class);
    public static final ContextKeys<Integer> X = of("x", Integer.class);
    public static final ContextKeys<Integer> Y = of("y", Integer.class);
    public static final ContextKeys<Integer> Z = of("z", Integer.class);
    public static final ContextKeys<String> WORLD = of("world", String.class);
    public static final ContextKeys<String> ID = of("id", String.class);
    public static final ContextKeys<LootType> LOOT = of("loot", LootType.class);
    public static final ContextKeys<String> NICK = of("nick", String.class);
    public static final ContextKeys<Boolean> OPEN_WATER = of("open_water", Boolean.class);
    public static final ContextKeys<Boolean> IS_NEW_SIZE_RECORD = of("is_new_size_record", Boolean.class);
    public static final ContextKeys<Float> SIZE = of("size", Float.class);
    public static final ContextKeys<Double> SIZE_MULTIPLIER = of("size_multiplier", Double.class);
    public static final ContextKeys<Double> SIZE_ADDER = of("size_adder", Double.class);
    public static final ContextKeys<String> SIZE_FORMATTED = of("size_formatted", String.class);
    public static final ContextKeys<Double> PRICE = of("price", Double.class);
    public static final ContextKeys<String> PRICE_FORMATTED = of("price_formatted", String.class);
    public static final ContextKeys<String> SURROUNDING = of("surrounding", String.class);
    public static final ContextKeys<String> TEMP_NEAR_PLAYER = of("near", String.class);
    public static final ContextKeys<String> ROD = of("rod", String.class);
    public static final ContextKeys<String> BAIT = of("bait", String.class);
    public static final ContextKeys<String> HOOK = of("hook", String.class);
    public static final ContextKeys<FishHook> HOOK_ENTITY = of("hook_entity", FishHook.class);
    public static final ContextKeys<Boolean> IN_BAG = of("in_bag", Boolean.class);
    public static final ContextKeys<CompetitionGoal> GOAL = of("goal", CompetitionGoal.class);
    public static final ContextKeys<String> HOUR = of("hour", String.class);
    public static final ContextKeys<String> MINUTE = of("minute", String.class);
    public static final ContextKeys<String> SECOND = of("second", String.class);
    public static final ContextKeys<Integer> SECONDS = of("seconds", Integer.class);
    public static final ContextKeys<String> PLAYER = of("player", String.class);
    public static final ContextKeys<String> SCORE_FORMATTED = of("score_formatted", String.class);
    public static final ContextKeys<Double> SCORE = of("score", Double.class);
    public static final ContextKeys<Double> CUSTOM_SCORE = of("custom_score", Double.class);
    public static final ContextKeys<Double> MIN_SIZE = of("min_size", Double.class);
    public static final ContextKeys<Double> MAX_SIZE = of("max_size", Double.class);
    public static final ContextKeys<String> RANK = of("rank", String.class);
    public static final ContextKeys<Location> OTHER_LOCATION = of("other_location", Location.class);
    public static final ContextKeys<ActiveTotemList> TOTEMS = of("totems", ActiveTotemList.class);
    public static final ContextKeys<Integer> OTHER_X = of("other_x", Integer.class);
    public static final ContextKeys<Integer> OTHER_Y = of("other_y", Integer.class);
    public static final ContextKeys<Integer> OTHER_Z = of("other_z", Integer.class);
    public static final ContextKeys<String> MONEY = of("money", String.class);
    public static final ContextKeys<String> MONEY_FORMATTED = of("money_formatted", String.class);
    public static final ContextKeys<String> REST = of("rest", String.class);
    public static final ContextKeys<String> REST_FORMATTED = of("rest_formatted", String.class);
    public static final ContextKeys<Integer> SOLD_ITEM_AMOUNT = of("sold_item_amount", Integer.class);
    public static final ContextKeys<Integer> AMOUNT = of("amount", Integer.class);
    public static final ContextKeys<Integer> TOTAL_AMOUNT = of("total_amount", Integer.class);
    public static final ContextKeys<Double> WEIGHT = of("0", Double.class);
    public static final ContextKeys<Double> TOTAL_WEIGHT = of("1", Double.class);
    public static final ContextKeys<String> TIME_LEFT = of("time_left", String.class);
    public static final ContextKeys<String> PROGRESS = of("progress", String.class);
    public static final ContextKeys<Float> RECORD = of("record", Float.class);
    public static final ContextKeys<Float> PREVIOUS_RECORD = of("previous_record", Float.class);
    public static final ContextKeys<String> RECORD_FORMATTED = of("record_formatted", String.class);
    public static final ContextKeys<String> PREVIOUS_RECORD_FORMATTED = of("previous_record_formatted", String.class);
    public static final ContextKeys<Integer> CLICKS_LEFT = of("left_clicks", Integer.class);
    public static final ContextKeys<Integer> REQUIRED_TIMES = of("clicks", Integer.class);
    public static final ContextKeys<EquipmentSlot> SLOT = of("hand", EquipmentSlot.class);
    public static final ContextKeys<Double> BONUS = of("bonus", Double.class);
    public static final ContextKeys<Double> BASE = of("base", Double.class);
    public static final ContextKeys<Integer> LOOT_ORDER = of("loot_order", Integer.class);
    public static final ContextKeys<Effect> EFFECT = of("effect", Effect.class);
    public static final ContextKeys<Boolean> FIRST_CAPTURE = of("first_capture", Boolean.class);

    private final String key;
    private final Class<T> type;

    protected ContextKeys(String key, Class<T> type) {
        this.key = key;
        this.type = type;
    }

    /**
     * Gets the key.
     *
     * @return the key.
     */
    public String key() {
        return key;
    }

    /**
     * Gets the type associated with the key.
     *
     * @return the type.
     */
    public Class<T> type() {
        return type;
    }

    /**
     * Creates a new context key.
     *
     * @param key the key.
     * @param type the type.
     * @param <T> the type of the value.
     * @return a new ContextKeys instance.
     */
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

    @Override
    public String toString() {
        return "ContextKeys{" +
                "key='" + key + '\'' +
                '}';
    }
}
