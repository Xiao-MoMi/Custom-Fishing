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

package net.momirealms.customfishing.api.mechanic.effect;

import java.util.Objects;

public class EffectProperties<T> {

    public static final EffectProperties<Boolean> LAVA_FISHING = of("lava", Boolean.class);
    public static final EffectProperties<Boolean> VOID_FISHING = of("void", Boolean.class);

    private final String key;
    private final Class<T> type;

    private EffectProperties(String key, Class<T> type) {
        this.key = key;
        this.type = type;
    }

    public String key() {
        return key;
    }

    public Class<T> type() {
        return type;
    }

    public static <T> EffectProperties<T> of(String key, Class<T> type) {
        return new EffectProperties<T>(key, type);
    }

    @Override
    public final boolean equals(final Object other) {
        if (this == other) {
            return true;
        } else if (other != null && this.getClass() == other.getClass()) {
            EffectProperties<?> that = (EffectProperties) other;
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
