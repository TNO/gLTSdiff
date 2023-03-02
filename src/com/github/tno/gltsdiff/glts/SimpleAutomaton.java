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
 * A simple automaton, which is a concrete LTS with accepting states.
 *
 * @param <T> The type of transition properties.
 */
public class SimpleAutomaton<T> extends Automaton<AutomatonStateProperty, T> implements Cloneable {
    @Override
    public boolean isInitial(AutomatonStateProperty property) {
        Preconditions.checkNotNull(property, "Expected a non-null state property.");
        return property.isInitial();
    }

    @Override
    public boolean isAccepting(AutomatonStateProperty property) {
        Preconditions.checkNotNull(property, "Expected a non-null state property.");
        return property.isAccepting();
    }

    /**
     * Adds a new initial state to this automaton.
     *
     * @param isAccepting Whether or not the new initial state is accepting.
     * @return The non-{@code null} newly added initial state.
     */
    public State<AutomatonStateProperty> addInitialState(boolean isAccepting) {
        return addState(new AutomatonStateProperty(true, isAccepting));
    }

    /**
     * Adds a new non-initial state to this automaton.
     *
     * @param isAccepting Whether or not the new state is accepting.
     * @return The non-{@code null} newly added non-initial state.
     */
    public State<AutomatonStateProperty> addState(boolean isAccepting) {
        return addState(new AutomatonStateProperty(false, isAccepting));
    }

    @Override
    public SimpleAutomaton<T> clone() {
        return map(SimpleAutomaton::new, property -> property, property -> property);
    }
}
