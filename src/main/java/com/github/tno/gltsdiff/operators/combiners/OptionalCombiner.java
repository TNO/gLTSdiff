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

import java.util.Optional;

import com.google.common.base.Preconditions;

/**
 * A combiner for optional properties.
 *
 * <p>
 * Two optionals are always combinable. It is assumed that their inner properties are always combinable as well. The
 * combination of two optionals is defined to be an optional with a combined inner property, or {@link Optional#empty()}
 * if there are no inner properties.
 * </p>
 *
 * @param <T> The type of inner properties.
 */
public class OptionalCombiner<T> extends Combiner<Optional<T>> {
    /** The combiner for properties that are present. */
    private final Combiner<T> combiner;

    /**
     * Instantiates a combiner for optional properties based on a combiner for properties that are present.
     *
     * @param combiner The combiner for properties that are present.
     */
    public OptionalCombiner(Combiner<T> combiner) {
        this.combiner = combiner;
    }

    @Override
    protected boolean computeAreCombinable(Optional<T> left, Optional<T> right) {
        return true;
    }

    @Override
    protected Optional<T> computeCombination(Optional<T> left, Optional<T> right) {
        if (left.isPresent() && right.isPresent()) {
            Preconditions.checkArgument(combiner.areCombinable(left.get(), right.get()));
            return Optional.of(combiner.combine(left.get(), right.get()));
        } else if (left.isPresent()) {
            return left;
        } else if (right.isPresent()) {
            return right;
        } else {
            return Optional.empty();
        }
    }
}
