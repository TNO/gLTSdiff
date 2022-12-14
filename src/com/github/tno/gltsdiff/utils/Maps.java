
package com.github.tno.gltsdiff.utils;

import java.util.function.BinaryOperator;

public class PredictableCollectors {
    /**
     * A merger for values of a map that can be used by {@link Collector collectors}, which always returns a
     * {@link IllegalStateException}.
     * 
     * @param <T> The type of values to merge.
     * @return A binary operator that always throws a {@link IllegalStateException} when applied.
     */
    public static <T> BinaryOperator<T> throwingMerger() {
        return (u, v) -> { throw new IllegalStateException(String.format("Duplicate key %s", u)); };
    }
}
