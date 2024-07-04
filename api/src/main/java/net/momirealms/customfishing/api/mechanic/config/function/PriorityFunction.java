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

package net.momirealms.customfishing.api.mechanic.config.function;

import org.jetbrains.annotations.NotNull;

public class PriorityFunction<T> implements Comparable<PriorityFunction<T>> {

    private final int priority;
    private final T function;

    public PriorityFunction(int priority, T function) {
        this.priority = priority;
        this.function = function;
    }

    public T get() {
        return function;
    }

    @Override
    public int compareTo(@NotNull PriorityFunction<T> o) {
        return Integer.compare(this.priority, o.priority);
    }
}
