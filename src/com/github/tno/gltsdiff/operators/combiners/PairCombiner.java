//////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2021-2022 Contributors to the GitHub community
//
// This program and the accompanying materials are made available
// under the terms of the MIT License which is available at
// https://opensource.org/licenses/MIT
//
// SPDX-License-Identifier: MIT
//////////////////////////////////////////////////////////////////////////////

package com.github.tno.gltsdiff.operators.combiners;

import org.apache.commons.math3.util.Pair;

/**
 * A combiner for pairs of combinable properties. Two pairs of properties are combinable if their left and right
 * properties are pairwise combinable. The combination of two pairs is a pair with a combined left and right property.
 *
 * @param <S> The type of left properties.
 * @param <T> The type of right properties.
 */
public class PairCombiner<S, T> extends Combiner<Pair<S, T>> {
    /** The combiner for left properties. */
    private final Combiner<S> leftCombiner;

    /** The combiner for right properties. */
    private final Combiner<T> rightCombiner;

    /**
     * Initializes a property pair combiner.
     * 
     * @param leftCombiner The combiner for left properties.
     * @param rightCombiner The combiner for right properties.
     */
    public PairCombiner(Combiner<S> leftCombiner, Combiner<T> rightCombiner) {
        this.leftCombiner = leftCombiner;
        this.rightCombiner = rightCombiner;
    }

    @Override
    protected boolean computeAreCombinable(Pair<S, T> left, Pair<S, T> right) {
        return leftCombiner.areCombinable(left.getFirst(), right.getFirst())
                && rightCombiner.areCombinable(left.getSecond(), right.getSecond());
    }

    @Override
    protected Pair<S, T> computeCombination(Pair<S, T> left, Pair<S, T> right) {
        return Pair.create(leftCombiner.combine(left.getFirst(), right.getFirst()),
                rightCombiner.combine(left.getSecond(), right.getSecond()));
    }
}
