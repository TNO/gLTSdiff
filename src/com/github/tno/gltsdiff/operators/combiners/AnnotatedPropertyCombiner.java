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

import java.util.Set;

import com.github.tno.gltsdiff.glts.AnnotatedProperty;

/**
 * A combiner for {@link AnnotatedProperty annotated properties}. Annotated properties can be combined if their inner
 * properties and annotations can be combined. Combining two annotated properties results in an annotated property with
 * a combined inner property and combined annotations.
 *
 * @param <T> The type of properties.
 * @param <U> The type of annotations.
 */
public class AnnotatedPropertyCombiner<T, U> extends Combiner<AnnotatedProperty<T, U>> {
    /** The combiner for properties. */
    private final Combiner<T> propertyCombiner;

    /** The combiner for sets of annotations. */
    private final Combiner<Set<U>> annotationCombiner;

    /**
     * Instantiates a new annotated property combiner.
     * 
     * @param propertyCombiner The combiner for properties.
     * @param annotationCombiner The combiner for annotations.
     */
    public AnnotatedPropertyCombiner(Combiner<T> propertyCombiner, Combiner<U> annotationCombiner) {
        this.propertyCombiner = propertyCombiner;
        this.annotationCombiner = new SetCombiner<>(annotationCombiner);
    }

    @Override
    protected boolean computeAreCombinable(AnnotatedProperty<T, U> left, AnnotatedProperty<T, U> right) {
        return propertyCombiner.areCombinable(left.getProperty(), right.getProperty())
                && annotationCombiner.areCombinable(left.getAnnotations(), right.getAnnotations());
    }

    @Override
    protected AnnotatedProperty<T, U> computeCombination(AnnotatedProperty<T, U> left, AnnotatedProperty<T, U> right) {
        return new AnnotatedProperty<>(propertyCombiner.combine(left.getProperty(), right.getProperty()),
                annotationCombiner.combine(left.getAnnotations(), right.getAnnotations()));
    }
}
