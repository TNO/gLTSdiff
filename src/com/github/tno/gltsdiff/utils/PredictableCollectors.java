
package com.github.tno.gltsdiff.utils;

import java.util.LinkedHashMap;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class PredictableCollectors {
    /**
     * A version of {@link Collectors#toMap} that writes to a {@link LinkedHashMap} instead of an {@link HashMap},
     * thereby letting the returned map have predictable iteration order.
     * 
     * @param <T> the type of the input elements.
     * @param <K> the output type of the key mapping function.
     * @param <U> the output type of the value mapping function.
     * @param keyMapper a mapping function to produce keys.
     * @param valueMapper a mapping function to produce values.
     * @return A collector as given by {@link Collectors#toMap}, which collects to a {@link LinkedHashMap}.
     */
    public static <T, K, U> Collector<T, ?, LinkedHashMap<K, U>> toMap(Function<? super T, ? extends K> keyMapper,
            Function<? super T, ? extends U> valueMapper)
    {
        return Collectors.toMap(keyMapper, valueMapper,
                (l, r) ->
                { throw new IllegalArgumentException("Expected no duplicate values."); }, LinkedHashMap::new);
    }
}
