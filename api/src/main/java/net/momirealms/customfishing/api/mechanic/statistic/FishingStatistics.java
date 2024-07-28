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

package net.momirealms.customfishing.api.mechanic.statistic;

import net.momirealms.customfishing.common.util.Pair;

import java.util.Map;

/**
 * The FishingStatistics interface represents statistics related to fishing activities.
 * It provides methods to retrieve and manipulate statistics such as the amount of fish caught and the maximum size of fish.
 */
public interface FishingStatistics {

    /**
     * Retrieves the total amount of fish caught.
     *
     * @return the total amount of fish caught.
     */
    int amountOfFishCaught();

    /**
     * Sets the total amount of fish caught.
     *
     * @param amountOfFishCaught the new total amount of fish caught.
     */
    void amountOfFishCaught(int amountOfFishCaught);

    /**
     * Retrieves the amount of fish caught with the specified ID.
     *
     * @param id the ID of the fish.
     * @return the amount of fish caught with the specified ID. 0 if not exist.
     */
    int getAmount(String id);

    /**
     * Adds the specified amount to the fish caught with the specified ID and returns the updated amount.
     *
     * @param id     the ID of the fish.
     * @param amount the amount to add.
     * @return a Pair containing the previous amount and the updated amount.
     */
    Pair<Integer, Integer> addAmount(String id, int amount);

    /**
     * Sets the amount of fish caught with the specified ID.
     *
     * @param id     the ID of the fish.
     * @param amount the new amount to set.
     */
    void setAmount(String id, int amount);

    /**
     * Retrieves the maximum size of the fish with the specified ID.
     *
     * @param id the ID of the fish.
     * @return the maximum size of the fish with the specified ID. -1f if not exist.
     */
    float getMaxSize(String id);

    /**
     * Sets the maximum size of the fish with the specified ID.
     *
     * @param id      the ID of the fish.
     * @param maxSize the new maximum size to set.
     */
    void setMaxSize(String id, float maxSize);

    /**
     * Updates the maximum size of the fish with the specified ID and returns true if successful, false otherwise.
     *
     * @param id      the ID of the fish.
     * @param newSize the new maximum size.
     * @return true if the update is successful, false otherwise.
     */
    boolean updateSize(String id, float newSize);

    /**
     * Resets the fishing statistics, clearing all recorded data.
     */
    void reset();

    /**
     * Retrieves the map containing the amounts of fish caught.
     *
     * @return the map containing the amounts of fish caught.
     */
    Map<String, Integer> amountMap();

    /**
     * Retrieves the map containing the maximum sizes of fish.
     *
     * @return the map containing the maximum sizes of fish.
     */
    Map<String, Float> sizeMap();

    /**
     * Creates a new Builder instance for constructing FishingStatistics objects.
     *
     * @return a new Builder instance.
     */
    static Builder builder() {
        return new FishingStatisticsImpl.BuilderImpl();
    }

    /**
     * The Builder interface provides a fluent API for constructing FishingStatistics instances.
     */
    interface Builder {

        /**
         * Sets the map containing the amounts of fish caught.
         *
         * @param amountMap the map containing the amounts of fish caught.
         * @return the Builder instance.
         */
        Builder amountMap(Map<String, Integer> amountMap);

        /**
         * Sets the map containing the maximum sizes of fish.
         *
         * @param sizeMap the map containing the maximum sizes of fish.
         * @return the Builder instance.
         */
        Builder sizeMap(Map<String, Float> sizeMap);

        /**
         * Builds and returns the FishingStatistics instance.
         *
         * @return the constructed FishingStatistics instance.
         */
        FishingStatistics build();
    }

    enum Type {
        MAX_SIZE,
        AMOUNT_OF_FISH_CAUGHT
    }
}
