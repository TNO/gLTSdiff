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

import com.google.common.base.Preconditions;

/**
 * A state in a {@link GLTS}.
 *
 * @param <S> The type of state properties.
 */
public class State<S> {
    /** The state identifier. */
    int id;

    /** The non-{@code null} state property. */
    private S property;

    /**
     * Instantiates a new state.
     *
     * @param id The state identifier.
     * @param property The non-{@code null} state property.
     */
    public State(int id, S property) {
        Preconditions.checkNotNull(property, "Expected a non-null state property.");

        this.id = id;
        this.property = property;
    }

    /**
     * Returns the state identifier.
     *
     * @return The state identifier.
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the state property.
     *
     * @return The non-{@code null} state property.
     */
    public S getProperty() {
        return property;
    }

    /**
     * Replace the state property.
     *
     * @param property The new non-{@code null} state property.
     */
    void setProperty(S property) {
        Preconditions.checkNotNull(property, "Expected a non-null state property.");
        this.property = property;
    }

    @Override
    public String toString() {
        return Integer.toString(id);
    }
}
