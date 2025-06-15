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

package net.momirealms.customfishing.api.mechanic;

import net.kyori.adventure.util.Index;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * Represents a type of mechanic.
 * This class provides predefined mechanic types and methods for managing and retrieving mechanic types by ID.
 */
public class MechanicType {

    private static final HashMap<String, List<MechanicType>> types = new HashMap<>();

    public static final MechanicType LOOT = of("loot");
    public static final MechanicType ROD = of("rod");
    public static final MechanicType UTIL = of("util");
    public static final MechanicType BAIT = of("bait");
    public static final MechanicType HOOK = of("hook");
    public static final MechanicType TOTEM = of("totem");
    public static final MechanicType ENCHANT = of("enchant");
    public static final MechanicType GEAR = of("gear");

    private final String type;

    /**
     * Constructs a new MechanicType.
     *
     * @param type the type identifier as a String
     */
    public MechanicType(String type) {
        this.type = type;
    }

    /**
     * Retrieves the type identifier.
     *
     * @return the type identifier as a String
     */
    public String getType() {
        return type;
    }

    /**
     * Creates a new MechanicType with the specified type identifier.
     *
     * @param type the type identifier as a String
     * @return a new {@link MechanicType} instance
     */
    private static MechanicType of(String type) {
        return new MechanicType(type);
    }

    private static final MechanicType[] VALUES = new MechanicType[]{LOOT, ROD, UTIL, BAIT, HOOK, TOTEM, ENCHANT};

    /**
     * Retrieves an array of all predefined mechanic types.
     *
     * @return an array of {@link MechanicType} instances
     */
    public static MechanicType[] values() {
        return VALUES;
    }

    private static final Index<String, MechanicType> INDEX = Index.create(MechanicType::getType, values());

    /**
     * Retrieves the index of mechanic types by their type identifier.
     *
     * @return an {@link Index} of mechanic types
     */
    public static Index<String, MechanicType> index() {
        return INDEX;
    }

    /**
     * Registers a new mechanic type with the specified ID.
     *
     * @param id   the identifier for the mechanic type
     * @param type the {@link MechanicType} to be registered
     */
    @ApiStatus.Internal
    public static void register(String id, MechanicType type) {
        List<MechanicType> previous = types.computeIfAbsent(id, k -> new ArrayList<>());
        previous.add(type);
    }

    /**
     * Retrieves a list of mechanic types by their ID.
     *
     * @param id the identifier for the mechanic types
     * @return a list of {@link MechanicType} instances, or null if none are found
     */
    @Nullable
    @ApiStatus.Internal
    public static List<MechanicType> getTypeByID(String id) {
        return types.get(id);
    }

    /**
     * Clears all registered mechanic types.
     */
    @ApiStatus.Internal
    public static void reset() {
        types.clear();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        MechanicType mechanicType = (MechanicType) object;
        return Objects.equals(type, mechanicType.type);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(type);
    }

    @Override
    public String toString() {
        return type;
    }
}
