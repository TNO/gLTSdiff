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

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;

/**
 * A labeled transition system.
 *
 * <p>
 * It is a {@link GLTS generalized labeled transition system} with the additional constraint that state properties must
 * include initial state information.
 * </p>
 *
 * @param <S> The type of state properties.
 * @param <T> The type of transition properties.
 */
public abstract class LTS<S, T> extends GLTS<S, T> {
    /**
     * Returns whether the given state property indicates that the associated state is initial.
     *
     * @param property The non-{@code null} state property.
     * @return {@code true} if the given state property describes an initial state, {@code false} otherwise.
     */
    public abstract boolean isInitial(S property);

    /**
     * Returns whether the specified state is initial.
     *
     * @param state The non-{@code null} state, which must exist in this LTS.
     * @return {@code true} if {@code state} is initial, {@code false} otherwise.
     */
    public boolean isInitialState(State<S> state) {
        Preconditions.checkNotNull(state, "Expected a non-null state.");
        return isInitial(state.getProperty());
    }

    /**
     * Returns the initial states of this LTS.
     *
     * @return The set of all initial states, all of which are non-{@code null}.
     */
    public Set<State<S>> getInitialStates() {
        return getStates().stream().filter(s -> isInitialState(s)).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * Counts initial states of this LTS that satisfy a given predicate.
     *
     * @param predicate The predicate.
     * @return The number of initial states in this LTS satisfying {@code predicate}.
     */
    public long countInitialStates(Predicate<State<S>> predicate) {
        return getInitialStates().stream().filter(predicate).count();
    }
}
