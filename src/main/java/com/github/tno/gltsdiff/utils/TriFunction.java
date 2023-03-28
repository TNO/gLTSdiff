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
 * Represents a function that accepts three arguments and produces a result. This is the three-arity specialization of
 * {@link Function}.
 *
 * @param <T1> The type of the 1st argument to the function.
 * @param <T2> The type of the 2nd argument to the function.
 * @param <T3> The type of the 3rd argument to the function.
 * @param <TR> The type of the result of the function.
 */
@FunctionalInterface
public interface TriFunction<T1, T2, T3, TR> {
    /**
     * Applies this function to the given arguments.
     *
     * @param a1 The 1st function argument.
     * @param a2 The 2nd function argument.
     * @param a3 The 3rd function argument.
     * @return The function result.
     */
    TR apply(T1 a1, T2 a2, T3 a3);

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
    default <V> TriFunction<T1, T2, T3, V> andThen(final Function<? super TR, ? extends V> after) {
        Objects.requireNonNull(after);
        return (final T1 a1, final T2 a2, final T3 a3) -> after.apply(apply(a1, a2, a3));
    }
}
