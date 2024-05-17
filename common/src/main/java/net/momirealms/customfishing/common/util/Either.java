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
