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

import com.github.tno.gltsdiff.operators.combiners.Combiner;
import com.google.common.base.Preconditions;

/**
 * Base class for property inclusion operators.
 *
 * @param <T> The type of properties, for which to determine inclusion.
 */
public abstract class BaseInclusion<T> implements Inclusion<T> {
    @Override
    public boolean isIncludedIn(T property1, T property2, Combiner<T> combiner) {
        // Check that properties are non-null.
        Preconditions.checkNotNull(property1, "Expected the first property to be non-null.");
        Preconditions.checkNotNull(property2, "Expected the second property to be non-null.");

        // Check that properties are combinable.
        Preconditions.checkArgument(combiner.areCombinable(property1, property2),
                "Expected properties to be combinable.");

        // Check the inclusion.
        return isIncludedInInternal(property1, property2, combiner);
    }

    /**
     * Binary operator for checking inclusion between two combinable properties.
     *
     * <p>
     * This method must satisfy the same properties as {@link #isIncludedIn}.
     * </p>
     *
     * @param property1 The first property, to check whether it is included in the second property. Is never
     *     {@code null}.
     * @param property2 The second property, to check whether it includes the first property. Is never {@code null}.
     * @param combiner The property combiner. Can be used to combine the two properties, as they are guaranteed to be
     *     combinable.
     * @return {@code true} if the first property is included in the second property, {@code false} otherwise.
     */
    protected abstract boolean isIncludedInInternal(T property1, T property2, Combiner<T> combiner);
}
