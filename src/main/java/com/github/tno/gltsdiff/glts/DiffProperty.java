//////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2021-2023 Contributors to the GitHub community
//
// This program and the accompanying materials are made available
// under the terms of the MIT License which is available at
// https://opensource.org/licenses/MIT
//
// SPDX-License-Identifier: MIT
//////////////////////////////////////////////////////////////////////////////

package com.github.tno.gltsdiff.glts;

import java.util.Objects;

import com.google.common.base.Preconditions;

/**
 * A difference property.
 *
 * <p>
 * It is a pair, with an element of type {@code T} together with an element of type {@link DiffKind}. The pair is used
 * as transition properties of {@link DiffAutomaton difference automata}.
 * </p>
 *
 * @param <T> The type of inner properties.
 */
public final class DiffProperty<T> {
    /** The non-{@code null} property. */
    private final T property;

    /** The non-{@code null} difference kind. */
    private final DiffKind diffKind;

    /**
     * Constructs a new difference property.
     *
     * @param property The non-{@code null} property.
     * @param diffKind The non-{@code null} difference kind.
     */
    public DiffProperty(T property, DiffKind diffKind) {
        Preconditions.checkNotNull(property, "Expected a non-null property.");
        Preconditions.checkNotNull(diffKind, "Expected a non-null difference kind.");

        this.property = property;
        this.diffKind = diffKind;
    }

    /**
     * Returns the property.
     *
     * @return The non-{@code null} property.
     */
    public T getProperty() {
        return property;
    }

    /**
     * Returns the difference kind.
     *
     * @return The non-{@code null} difference kind.
     */
    public DiffKind getDiffKind() {
        return diffKind;
    }

    @Override
    public int hashCode() {
        return Objects.hash(property, diffKind);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof DiffProperty)) {
            return false;
        }

        final DiffProperty<?> other = (DiffProperty<?>)object;

        return Objects.equals(this.property, other.property) && Objects.equals(this.diffKind, other.diffKind);
    }
}
