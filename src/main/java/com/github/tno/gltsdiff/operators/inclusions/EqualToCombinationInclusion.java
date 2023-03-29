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

import java.util.Set;

import com.github.tno.gltsdiff.glts.lts.automaton.diff.DiffKind;
import com.github.tno.gltsdiff.operators.combiners.Combiner;

/**
 * An inclusion operator that checks whether the {@link Combiner#combine combination} of two properties is
 * {@link Object#equals equal} to the second property.
 *
 * <p>
 * The intuition is that when you combine properties, you get their combination, which 'contains' both of them. If the
 * first property is included in the second property, combining the first property with the second property gives you
 * the second property, as the second property already 'contains' the first property. The second property must thus be
 * equal to the combination of the two properties for the first property to be included in the second property.
 * </p>
 *
 * <p>
 * For example:
 * </p>
 * <ul>
 * <li>When comparing {@link Set}s of integer numbers, a set A = {1, 2, 4} is included in set B = {1, 2, 3, 4, 5}, as
 * all the integer numbers that are in the first set are also in the second set. A {@link Combiner} to compute their
 * combination would compute the union of the sets. As set A is included in set B, their union A ∪ B = B. In general, if
 * the second set is equal to the combination of both sets, the first set is included in the second set. If the second
 * set is not equal to the combination of both sets, the first set is not included in the second set.</li>
 * <li>When comparing {@link DiffKind difference kinds}, if something is both added (in one input model) and removed (in
 * the other input model), then it is really unchanged. The unchanged difference kind is thus a combination of being
 * added and removed. Hence, {@link DiffKind#UNCHANGED} includes {@link DiffKind#ADDED} and {@link DiffKind#REMOVED}.
 * One can think of {@link DiffKind#UNCHANGED} as a set with both {@link DiffKind#ADDED} and {@link DiffKind#REMOVED},
 * and their combiner as computing the set union. Then a difference kind is included in another difference kind if their
 * combination is equal to the second difference kind, similar to the previous example of the sets of integer
 * numbers.</li>
 * </ul>
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
