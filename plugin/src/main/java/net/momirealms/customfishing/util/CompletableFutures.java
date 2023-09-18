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

package net.momirealms.customfishing.util;

import com.google.common.collect.ImmutableList;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collector;
import java.util.stream.Stream;

public final class CompletableFutures {
    private CompletableFutures() {}

    public static <T extends CompletableFuture<?>> Collector<T, ImmutableList.Builder<T>, CompletableFuture<Void>> collector() {
        return Collector.of(
                ImmutableList.Builder::new,
                ImmutableList.Builder::add,
                (l, r) -> l.addAll(r.build()),
                builder -> allOf(builder.build())
        );
    }

    public static CompletableFuture<Void> allOf(Stream<? extends CompletableFuture<?>> futures) {
        CompletableFuture<?>[] arr = futures.toArray(CompletableFuture[]::new);
        return CompletableFuture.allOf(arr);
    }

    public static CompletableFuture<Void> allOf(Collection<? extends CompletableFuture<?>> futures) {
        CompletableFuture<?>[] arr = futures.toArray(new CompletableFuture[0]);
        return CompletableFuture.allOf(arr);
    }
}