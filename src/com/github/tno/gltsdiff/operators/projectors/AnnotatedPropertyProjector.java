//////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2021-2022 Contributors to the GitHub community
//
// This program and the accompanying materials are made available
// under the terms of the MIT License which is available at
// https://opensource.org/licenses/MIT
//
// SPDX-License-Identifier: MIT
//////////////////////////////////////////////////////////////////////////////

package com.github.tno.gltsdiff.operators.projectors;

import java.util.Optional;
import java.util.Set;

import com.github.tno.gltsdiff.lts.AnnotatedProperty;
import com.google.common.base.Preconditions;

/**
 * A projector for projecting {@link AnnotatedProperty annotated properties}.
 *
 * @param <T> The type of properties.
 * @param <U> The type of annotations.
 * @param <V> The type of elements to project along.
 */
public class AnnotatedPropertyProjector<T, U, V> implements Projector<AnnotatedProperty<T, U>, V> {
    /** The projector for properties. */
    private final Projector<T, V> propertyProjector;

    /** The projector for sets of annotations. */
    private final Projector<Set<U>, V> annotationProjector;

    /**
     * Instantiates a new annotated property projector.
     * 
     * @param propertyProjector The projector for properties.
     * @param annotationProjector The projector for annotations.
     */
    public AnnotatedPropertyProjector(Projector<T, V> propertyProjector, Projector<U, V> annotationProjector) {
        this.propertyProjector = propertyProjector;
        this.annotationProjector = new SetProjector<>(annotationProjector);
    }

    @Override
    public Optional<AnnotatedProperty<T, U>> project(AnnotatedProperty<T, U> property, V along) {
        Preconditions.checkNotNull(property, "Expected a non-null property.");
        Preconditions.checkNotNull(along, "Expected a non-null element to project along.");

        Optional<T> projectedInnerProperty = propertyProjector.project(property.getProperty(), along);

        if (projectedInnerProperty.isPresent()) {
            Optional<Set<U>> projectedAnnotations = annotationProjector.project(property.getAnnotations(), along);

            if (projectedAnnotations.isPresent()) {
                return Optional.of(new AnnotatedProperty<>(projectedInnerProperty.get(), projectedAnnotations.get()));
            }
        }

        return Optional.empty();
    }
}
