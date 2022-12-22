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

import java.util.Objects;

import com.google.common.base.Preconditions;

/**
 * Essentially a pair of an element of type {@code T} together with an element of type {@link DiffKind}, which are used
 * as transition properties of {@link DiffAutomaton difference automata}.
 * 
 * @param <T> The type of inner properties.
 */
public final class DiffProperty<T> {
    /** The non-{@code null} enclosed property. */
    private final T property;

    /** The non-{@code null} enclosed difference kind. */
    private final DiffKind diffKind;

    /**
     * Constructs a new difference property.
     * 
     * @param property The non-{@code null} enclosed property.
     * @param diffKind The non-{@code null} enclosed difference kind.
     */
    public DiffProperty(T property, DiffKind diffKind) {
        Preconditions.checkNotNull(property, "Expected a non-null enclosed property.");
        Preconditions.checkNotNull(diffKind, "Expected a non-null difference kind.");

        this.property = property;
        this.diffKind = diffKind;
    }

    /** @return The non-{@code null} enclosed property. */
    public T getProperty() {
        return property;
    }

    /** @return The non-{@code null} enclosed difference kind. */
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
