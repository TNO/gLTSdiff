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
 * An automaton.
 *
 * @param <S> The type of state properties.
 * @param <T> The type of transition properties.
 */
public abstract class Automaton<S, T> extends LTS<S, T> {
    /**
     * Returns whether the given state property indicates that the associated state is accepting.
     *
     * @param property The non-{@code null} state property to check.
     * @return {@code true} if the given state property indicates state acceptance, {@code false} otherwise.
     */
    public abstract boolean isAccepting(S property);

    /**
     * Returns whether the given state is accepting.
     *
     * @param state The non-{@code null} state to check, which must exist in this automaton.
     * @return {@code true} if the given state is accepting, {@code false} otherwise.
     */
    public boolean isAcceptingState(State<S> state) {
        Preconditions.checkNotNull(state, "Expected a non-null state.");
        return isAccepting(state.getProperty());
    }
}
