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

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.tno.gltsdiff.utils.EquivalenceClasses;
import com.google.common.collect.Sets;

/**
 * A combiner for sets of combinable properties.
 *
 * <p>
 * Sets can always be combined, and combining any two sets results in the union of these sets in which all combinable
 * properties are combined.
 * </p>
 *
 * @param <T> The type of properties.
 */
public class SetCombiner<T> extends Combiner<Set<T>> {
    /** The combiner for properties. */
    private final Combiner<T> combiner;

    /**
     * Instantiates a new property set combiner.
     *
     * @param combiner The combiner for properties.
     */
    public SetCombiner(Combiner<T> combiner) {
        this.combiner = combiner;
    }

    @Override
    protected boolean computeAreCombinable(Set<T> left, Set<T> right) {
        return true;
    }

    @Override
    protected Set<T> computeCombination(Set<T> left, Set<T> right) {
        return EquivalenceClasses.split(Sets.union(left, right), combiner::areCombinable).stream()
                .map(combiner::combine).collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
