//////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2021-2023 Contributors to the GitHub community
//
// This program and the accompanying materials are made available
// under the terms of the MIT License which is available at
// https://opensource.org/licenses/MIT
//
// SPDX-License-Identifier: MIT
//////////////////////////////////////////////////////////////////////////////

package com.github.tno.gltsdiff.glts.lts;

import java.util.Set;
import java.util.function.Predicate;

import com.github.tno.gltsdiff.glts.GLTS;
import com.github.tno.gltsdiff.glts.State;

/**
 * A labeled transition system, a {@link GLTS} with initial state information for states.
 *
 * @param <S> The type of LTS state properties.
 * @param <T> The type of transition properties.
 */
public class LTS<S extends LTSStateProperty, T> extends GLTS<S, T> {
    /**
     * Returns the initial states of this LTS.
     *
     * @return The set of all initial states, all of which are non-{@code null}.
     */
    public Set<State<S>> getInitialStates() {
        return getStates(s -> s.getProperty().isInitial());
    }

    /**
     * Counts initial states of this LTS that satisfy a given predicate.
     *
     * @param predicate The predicate.
     * @return The number of initial states in this LTS satisfying {@code predicate}.
     */
    public long countInitialStates(Predicate<State<S>> predicate) {
        return getStates().stream().filter(s -> s.getProperty().isInitial()).filter(predicate).count();
    }
}
