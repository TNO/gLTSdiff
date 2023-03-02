//////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2022-2023 Contributors to the GitHub community
//
// This program and the accompanying materials are made available
// under the terms of the MIT License which is available at
// https://opensource.org/licenses/MIT
//
// SPDX-License-Identifier: MIT
//////////////////////////////////////////////////////////////////////////////

package com.github.tno.gltsdiff.operators.combiners;

/** A combiner that allows any two properties to be combined, always producing the same fixed value. */
public class FixedValueCombiner<T> extends Combiner<T> {
    /** The fixed value. */
    private final T fixedValue;

    /**
     * Instantiates a fixed value combiner.
     * 
     * @param fixedValue The fixed value.
     */
    public FixedValueCombiner(T fixedValue) {
        this.fixedValue = fixedValue;
    }

    @Override
    protected boolean computeAreCombinable(T left, T right) {
        return true;
    }

    @Override
    protected T computeCombination(T left, T right) {
        return fixedValue;
    }
}
