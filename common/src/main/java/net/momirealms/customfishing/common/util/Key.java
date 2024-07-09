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

package net.momirealms.customfishing.common.util;

public record Key(String namespace, String value) {

    public static Key of(String namespace, String value) {
        return new Key(namespace, value);
    }

    public static Key fromString(String key) {
        String[] split = key.split(":", 2);
        return of(split[0], split[1]);
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Key key)) return false;
        return this.namespace.equals(key.namespace()) && this.value.equals(key.value());
    }

    @Override
    public String toString() {
        return namespace + ":" + value;
    }
}