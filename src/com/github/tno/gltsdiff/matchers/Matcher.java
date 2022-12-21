//////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2021-2022 Contributors to the GitHub community
//
// This program and the accompanying materials are made available
// under the terms of the MIT License which is available at
// https://opensource.org/licenses/MIT
//
// SPDX-License-Identifier: MIT
//////////////////////////////////////////////////////////////////////////////

package com.github.tno.gltsdiff.matchers;

import java.util.Map;

import com.github.tno.gltsdiff.lts.GLTS;
import com.github.tno.gltsdiff.lts.State;

/**
 * A matcher that computes a (graph theoretical) matching between the states of the LHS and RHS.
 *
 * @param <S> The type of state properties.
 * @param <T> The type of transition properties.
 * @param <U> The type of GLTSs.
 */
public interface Matcher<S, T, U extends GLTS<S, T>> {
    /** @return The left-hand-side (LHS) GLTS. */
    public U getLhs();

    /** @return The right-hand-side (RHS) GLTS. */
    public U getRhs();

    /**
     * Computes a matching from LHS states to RHS states.
     * 
     * @return A matching from LHS to RHS states that satisfies the following properties:
     *     <ul>
     *     <li>All states in the key set of the returned matching are LHS states.</li>
     *     <li>All states in the value set of the returned matching are RHS states.</li>
     *     <li>All state matchings are disjoint: there is no state that is involved in more than one matching.</li>
     *     <li>All matched states have combinable properties.</li>
     *     </ul>
     */
    public Map<State<S>, State<S>> compute();
}
