//////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2023 Contributors to the GitHub community
//
// This program and the accompanying materials are made available
// under the terms of the MIT License which is available at
// https://opensource.org/licenses/MIT
//
// SPDX-License-Identifier: MIT
//////////////////////////////////////////////////////////////////////////////

package com.github.tno.gltsdiff.operators.inclusions;

import java.util.Objects;

import com.github.tno.gltsdiff.operators.combiners.Combiner;
import com.google.common.base.Preconditions;

/**
 * An inclusion operator that checks whether the combination of two properties is {@link Object#equals equal} to both
 * those properties.
 *
 * <p>
 * Assumptions:
 * </p>
 * <ul>
 * <li>Inclusion must only be checked for combinable properties.</li>
 * <li>The combination of the properties must either be equal to both properties, or be unequal to both properties.</li>
 * </ul>
 *
 * @param <T> The type of properties for which to check inclusion.
 */
public class EqualToCombinationInclusion<T> implements Inclusion<T> {
    /** The property combiner to use. */
    private final Combiner<T> combiner;

    /**
     * Instantiates a new equal to combination inclusion operator.
     *
     * @param combiner The property combiner to use.
     */
    public EqualToCombinationInclusion(Combiner<T> combiner) {
        this.combiner = combiner;
    }

    @Override
    public boolean isIncludedIn(T property1, T property2) {
        // Check that properties are non-null.
        Preconditions.checkNotNull(property1, "Expected the first property to be non-null.");
        Preconditions.checkNotNull(property2, "Expected the second property to be non-null.");

        // Combine the properties.
        Preconditions.checkArgument(combiner.areCombinable(property1, property2),
                "Expected properties to be combinable.");
        T combination = combiner.combine(property1, property2);

        // Check whether the properties are equal to their combination.
        boolean equal1 = Objects.equals(property1, combination);
        boolean equal2 = Objects.equals(property2, combination);
        Preconditions.checkArgument(equal1 == equal2,
                "Expected combination to be either equal to both properties or unequal to both properties.");
        return equal1;
    }
}
