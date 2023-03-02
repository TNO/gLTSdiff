//////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2021-2023 Contributors to the GitHub community
//
// This program and the accompanying materials are made available
// under the terms of the MIT License which is available at
// https://opensource.org/licenses/MIT
//
// SPDX-License-Identifier: MIT
//////////////////////////////////////////////////////////////////////////////

package com.github.tno.gltsdiff.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiPredicate;

import com.google.common.base.Preconditions;

/** Utilities for working with equivalence classes of equivalent values. */
public class EquivalenceClasses {
    /** Constructor for the {@link EquivalenceClasses} class. */
    private EquivalenceClasses() {
        // Static class.
    }

    /**
     * Computes the equivalence classes (quotient space) of a collection under equivalence.
     *
     * <p>
     * In other words, this operation splits the given collection into <i>equivalence classes</i>, which are sets of
     * elements of the collection such that two elements belong to the same equivalence class set if and only if they
     * are equivalent according to the specified equivalence relation.
     * </p>
     *
     * @param <T> The type of elements.
     * @param collection The collection of element to split into equivalence classes.
     * @param equivalence The binary predicate to split on. This predicate must be an <u>equivalence relation</u>. In
     *     other words it must be <u>reflexive</u>, <u>symmetric</u> and <u>transitive</u>.
     * @return All equivalence classes of the collection under the given equivalence relation.
     */
    public static final <T> Collection<Set<T>> split(Collection<T> collection, BiPredicate<T, T> equivalence) {
        Collection<Set<T>> equivClasses = new ArrayList<>();

        for (T element: collection) {
            Optional<Set<T>> equivClass = equivClasses.stream()
                    .filter(c -> equivalence.test(element, getAnyElementOfSet(c))).findFirst();

            if (equivClass.isPresent()) {
                equivClass.get().add(element);
            } else {
                Set<T> newEquivClass = new LinkedHashSet<>();
                newEquivClass.add(element);
                equivClasses.add(newEquivClass);
            }
        }

        return equivClasses;
    }

    /**
     * Gives back any element of a given specified set, which must be non-empty.
     *
     * @param <T> The type of elements.
     * @param set The set.
     * @return An element of the set.
     */
    private static final <T> T getAnyElementOfSet(Set<T> set) {
        Preconditions.checkArgument(!set.isEmpty(), "Expected a non-empty set.");
        return set.iterator().next();
    }
}
