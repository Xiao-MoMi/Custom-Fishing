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

package net.momirealms.customfishing.api.mechanic.item;

import net.kyori.adventure.util.Index;
import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class MechanicType {

    private static final HashMap<String, List<MechanicType>> types = new HashMap<>();

    public static final MechanicType LOOT = of("loot");
    public static final MechanicType ROD = of("rod");
    public static final MechanicType UTIL = of("util");
    public static final MechanicType BAIT = of("bait");
    public static final MechanicType HOOK = of("hook");
    public static final MechanicType TOTEM = of("totem");
    public static final MechanicType ENTITY = of("entity");
    public static final MechanicType BLOCK = of("block");
    public static final MechanicType ENCHANT = of("enchant");

    private final String type;

    public MechanicType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    private static MechanicType of(String type) {
        return new MechanicType(type);
    }

    public static MechanicType[] values() {
        return new MechanicType[]{LOOT, ROD, UTIL, BAIT, HOOK, TOTEM, ENCHANT, ENTITY, BLOCK};
    }

    private static final Index<String, MechanicType> INDEX = Index.create(MechanicType::getType, values());

    public static Index<String, MechanicType> index() {
        return INDEX;
    }

    @ApiStatus.Internal
    public static void register(String id, MechanicType type) {
        List<MechanicType> previous = types.computeIfAbsent(id, k -> new ArrayList<>());
        previous.add(type);
    }

    @Nullable
    @ApiStatus.Internal
    public static List<MechanicType> getTypeByID(String id) {
        return types.get(id);
    }

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
