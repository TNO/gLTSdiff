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

/**
 * A combiner that combines any two properties only if they are equal to one another.
 *
 * <p>
 * So any two properties are combinable if and only if they are {@link Object#equals equal}, and the result of combining
 * them is equal to both the input properties.
 * </p>
 *
 * @param <T> The type of properties to combine.
 */
public class EqualityCombiner<T> extends Combiner<T> {
    @Override
    protected boolean computeAreCombinable(T left, T right) {
        return left.equals(right);
    }

    @Override
    protected T computeCombination(T left, T right) {
        return left;
    }
}
