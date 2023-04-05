//////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2023 Contributors to the GitHub community
//
// This program and the accompanying materials are made available
// under the terms of the MIT License which is available at
// https://opensource.org/licenses/MIT
//
// SPDX-License-Identifier: MIT
//////////////////////////////////////////////////////////////////////////////

package com.github.tno.gltsdiff.utils;

import java.util.Objects;
import java.util.function.Function;

/**
 * Represents a function that accepts five arguments and produces a result. This is the five-arity specialization of
 * {@link Function}.
 *
 * @param <T1> The type of the 1st argument to the function.
 * @param <T2> The type of the 2nd argument to the function.
 * @param <T3> The type of the 3rd argument to the function.
 * @param <T4> The type of the 4th argument to the function.
 * @param <T5> The type of the 5th argument to the function.
 * @param <TR> The type of the result of the function.
 */
@FunctionalInterface
public interface QuintFunction<T1, T2, T3, T4, T5, TR> {
    /**
     * Applies this function to the given arguments.
     *
     * @param a1 The 1st function argument.
     * @param a2 The 2nd function argument.
     * @param a3 The 3rd function argument.
     * @param a4 The 4th function argument.
     * @param a5 The 5th function argument.
     * @return The function result.
     */
    TR apply(T1 a1, T2 a2, T3 a3, T4 a4, T5 a5);

    /**
     * Returns a composed function that first applies this function to its input, and then applies the {@code after}
     * function to the result. If evaluation of either function throws an exception, it is relayed to the caller of the
     * composed function.
     *
     * @param <V> The type of output of the {@code after} function, and of the composed function.
     * @param after The function to apply after this function is applied.
     * @return A composed function that first applies this function and then applies the {@code after} function.
     * @throws NullPointerException If {@code after} is {@code null}.
     */
    default <V> QuintFunction<T1, T2, T3, T4, T5, V> andThen(final Function<? super TR, ? extends V> after) {
        Objects.requireNonNull(after);
        return (final T1 a1, final T2 a2, final T3 a3, final T4 a4, final T5 a5) -> after
                .apply(apply(a1, a2, a3, a4, a5));
    }
}
