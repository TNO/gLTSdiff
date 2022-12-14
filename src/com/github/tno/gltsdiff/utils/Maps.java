
package com.github.tno.gltsdiff.utils;

import java.util.function.BinaryOperator;
import java.util.stream.Collector;

public class Maps {
    /**
     * A binary operator to merge values that can be used by {@link Collector collectors}, which always returns an
     * {@link IllegalStateException}.
     * 
     * @param <T> The type of values to merge.
     * @return A binary operator that always throws a {@link IllegalStateException} when applied.
     */
    public static <T> BinaryOperator<T> throwingMerger() {
        return (u, v) -> { throw new IllegalStateException(String.format("Attempted to merge values %s and %s", u, v)); };
    }
}
