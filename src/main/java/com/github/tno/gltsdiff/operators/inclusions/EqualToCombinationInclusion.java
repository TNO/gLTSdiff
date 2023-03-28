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

/**
 * An inclusion operator that checks whether the combination of two properties is {@link Object#equals equal} to the
 * second property.
 *
 * @param <T> The type of properties for which to check inclusion.
 */
public class EqualToCombinationInclusion<T> extends BaseInclusion<T> {
    @Override
    public boolean isIncludedInInternal(T property1, T property2, Combiner<T> combiner) {
        T combination = combiner.combine(property1, property2);
        return property2.equals(combination);
    }
}
