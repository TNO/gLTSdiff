//////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2021-2022 Contributors to the GitHub community
//
// This program and the accompanying materials are made available
// under the terms of the MIT License which is available at
// https://opensource.org/licenses/MIT
//
// SPDX-License-Identifier: MIT
//////////////////////////////////////////////////////////////////////////////

package com.github.tno.gltsdiff.glts;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

/**
 * A state or transition property with an associated set of annotations.
 *
 * @param <T> The type of properties.
 * @param <U> The type of annotations.
 */
public class AnnotatedProperty<T, U> {
    /** The non-{@code null} enclosed property. */
    private final T property;

    /** The non-{@code null} set of non-{@code null} annotations. */
    private final Set<U> annotations;

    /**
     * Instantiates a new annotated property without annotations.
     * 
     * @param property The non-{@code null} enclosed property.
     */
    public AnnotatedProperty(T property) {
        this(property, ImmutableSet.of());
    }

    /**
     * Instantiates a new annotated property.
     * 
     * @param property The non-{@code null} enclosed property.
     * @param annotations The non-{@code null} set of non-{@code null} annotations.
     */
    public AnnotatedProperty(T property, Set<U> annotations) {
        Preconditions.checkNotNull(property, "Expected a non-null property.");
        Preconditions.checkNotNull(annotations, "Expected a non-null set of annotations.");
        annotations.forEach(annotation -> Preconditions.checkNotNull(annotation, "Expected non-null annotations."));

        this.property = property;
        this.annotations = annotations;
    }

    /** @return The non-{@code null} enclosed property. */
    public T getProperty() {
        return property;
    }

    /** @return The non-{@code null} set of non-{@code null} annotations. */
    public Set<U> getAnnotations() {
        return Collections.unmodifiableSet(annotations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(property, annotations);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof AnnotatedProperty)) {
            return false;
        }

        final AnnotatedProperty<?, ?> other = (AnnotatedProperty<?, ?>)object;

        return Objects.equals(this.property, other.property) && Objects.equals(this.annotations, other.annotations);
    }
}
