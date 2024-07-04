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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

public interface Either<U, V> {

    static <U, V> @NotNull Either<U, V> ofPrimary(final @NotNull U value) {
        return EitherImpl.of(requireNonNull(value, "value"), null);
    }

    static <U, V> @NotNull Either<U, V> ofFallback(final @NotNull V value) {
        return EitherImpl.of(null, requireNonNull(value, "value"));
    }

    @NotNull
    Optional<U> primary();

    @NotNull
    Optional<V> fallback();

    default @Nullable U primaryOrMapFallback(final @NotNull Function<V, U> mapFallback) {
        return this.primary().orElseGet(() -> mapFallback.apply(this.fallback().get()));
    }

    default @Nullable V fallbackOrMapPrimary(final @NotNull Function<U, V> mapPrimary) {
        return this.fallback().orElseGet(() -> mapPrimary.apply(this.primary().get()));
    }

    default @NotNull <R> R mapEither(
            final @NotNull Function<U, R> mapPrimary,
            final @NotNull Function<V, R> mapFallback
    ) {
        return this.primary()
                .map(mapPrimary)
                .orElseGet(() -> this.fallback().map(mapFallback).get());
    }
}
