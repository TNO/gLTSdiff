//////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2021-2023 Contributors to the GitHub community
//
// This program and the accompanying materials are made available
// under the terms of the MIT License which is available at
// https://opensource.org/licenses/MIT
//
// SPDX-License-Identifier: MIT
//////////////////////////////////////////////////////////////////////////////

package com.github.tno.gltsdiff.operators.combiners;

import java.util.function.Function;

/**
 * A combiner for {@code U}-typed properties based on a combiner for {@code T}-typed properties, with {@code U} a
 * subtype of {@code T}.
 *
 * @param <T> The type of properties.
 * @param <U> The subtype of properties.
 */
public class SubtypeCombiner<T, U extends T> extends Combiner<U> {
    /** The combiner for properties. */
    private final Combiner<T> combiner;

    /**
     * The converter function for combined properties. This function is only called with non-{@code null} properties,
     * and must always return a non-{@code null} property.
     */
    private final Function<T, U> converter;

    /**
     * Instantiates a new subtype property combiner.
     *
     * @param combiner The combiner for properties.
     * @param converter The converter function for combined properties. This function is only called with
     *     non-{@code null} properties, and must always return a non-{@code null} property.
     */
    public SubtypeCombiner(Combiner<T> combiner, Function<T, U> converter) {
        this.combiner = combiner;
        this.converter = converter;
    }

    @Override
    protected boolean computeAreCombinable(U left, U right) {
        return combiner.areCombinable(left, right);
    }

    @Override
    protected U computeCombination(U left, U right) {
        return converter.apply(combiner.combine(left, right));
    }
}
