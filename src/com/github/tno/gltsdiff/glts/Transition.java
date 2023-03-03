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
 * A transition with a property, in a {@link GLTS}.
 *
 * @param <S> The type of state properties.
 * @param <T> The type of transition properties.
 */
public class Transition<S, T> {
    /** The non-{@code null} source state. */
    private final State<S> source;

    /** The non-{@code null} transition property. */
    private final T property;

    /** The non-{@code null} target state. */
    private final State<S> target;

    /**
     * Instantiates a new transition.
     *
     * @param source The non-{@code null} source state.
     * @param property The non-{@code null} transition property.
     * @param target The non-{@code null} target state.
     */
    public Transition(State<S> source, T property, State<S> target) {
        Preconditions.checkNotNull(source, "Expected a non-null source state.");
        Preconditions.checkNotNull(property, "Expected a non-null transition property.");
        Preconditions.checkNotNull(target, "Expected a non-null target state.");
        this.source = source;
        this.property = property;
        this.target = target;
    }

    /**
     * Returns the source state.
     *
     * @return The non-{@code null} source state.
     */
    public State<S> getSource() {
        return source;
    }

    /**
     * Returns the transition property.
     *
     * @return The non-{@code null} transition property.
     */
    public T getProperty() {
        return property;
    }

    /**
     * Returns the target state.
     *
     * @return The non-{@code null} target state.
     */
    public State<S> getTarget() {
        return target;
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, property, target);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof Transition)) {
            return false;
        }

        final Transition<?, ?> other = (Transition<?, ?>)object;

        return Objects.equals(this.source, other.source) && Objects.equals(this.property, other.property)
                && Objects.equals(this.target, other.target);
    }
}
