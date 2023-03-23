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

import com.github.tno.gltsdiff.glts.DiffKind;
import com.github.tno.gltsdiff.glts.DiffProperty;

/**
 * A combiner for {@link DiffProperty difference properties}.
 *
 * <p>
 * Any two difference properties are combinable if their inner properties are combinable and their difference kinds are
 * combinable. Combining two difference properties results in a difference property with a combined inner property and a
 * combined difference kind.
 * </p>
 *
 * @param <T> The type of inner properties.
 */
public class DiffPropertyCombiner<T> extends Combiner<DiffProperty<T>> {
    /** The combiner for inner properties. */
    private final Combiner<T> propertyCombiner;

    /** The combiner for difference kinds. */
    private final Combiner<DiffKind> diffKindCombiner;

    /**
     * Instantiates a difference property combiner that uses a {@link DiffKindCombiner} to combine difference kinds.
     *
     * @param propertyCombiner The combiner for inner properties.
     */
    public DiffPropertyCombiner(Combiner<T> propertyCombiner) {
        this(propertyCombiner, new DiffKindCombiner());
    }

    /**
     * Instantiates a difference property combiner.
     *
     * @param propertyCombiner The combiner for inner properties.
     * @param diffKindCombiner The combiner for difference kinds.
     */
    public DiffPropertyCombiner(Combiner<T> propertyCombiner, Combiner<DiffKind> diffKindCombiner) {
        this.propertyCombiner = propertyCombiner;
        this.diffKindCombiner = diffKindCombiner;
    }

    @Override
    protected boolean computeAreCombinable(DiffProperty<T> left, DiffProperty<T> right) {
        return propertyCombiner.areCombinable(left.getProperty(), right.getProperty())
                && diffKindCombiner.areCombinable(left.getDiffKind(), right.getDiffKind());
    }

    @Override
    protected DiffProperty<T> computeCombination(DiffProperty<T> left, DiffProperty<T> right) {
        return new DiffProperty<>(propertyCombiner.combine(left.getProperty(), right.getProperty()),
                diffKindCombiner.combine(left.getDiffKind(), right.getDiffKind()));
    }
}
