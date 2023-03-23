//////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2022-2023 Contributors to the GitHub community
//
// This program and the accompanying materials are made available
// under the terms of the MIT License which is available at
// https://opensource.org/licenses/MIT
//
// SPDX-License-Identifier: MIT
//////////////////////////////////////////////////////////////////////////////

package com.github.tno.gltsdiff.utils;

import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.stream.Collector;

/** Utilities for working with {@link Map maps}. */
public class Maps {
    /** Constructor for the {@link Maps} class. */
    private Maps() {
        // Static class.
    }

    /**
     * A binary operator to merge values that can be used by {@link Collector collectors}, which always returns an
     * {@link IllegalStateException}.
     *
     * @param <T> The type of values to merge.
     * @return A binary operator that always throws a {@link IllegalStateException} when applied.
     */
    public static <T> BinaryOperator<T> throwingMerger() {
        return (u, v) -> {
            throw new IllegalStateException(String.format("Attempted to merge values %s and %s.", u, v));
        };
    }
}
