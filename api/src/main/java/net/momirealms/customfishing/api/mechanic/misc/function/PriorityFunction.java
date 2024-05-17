package net.momirealms.customfishing.api.mechanic.misc.function;

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
