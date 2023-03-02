//////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2021-2023 Contributors to the GitHub community
//
// This program and the accompanying materials are made available
// under the terms of the MIT License which is available at
// https://opensource.org/licenses/MIT
//
// SPDX-License-Identifier: MIT
//////////////////////////////////////////////////////////////////////////////

package com.github.tno.gltsdiff.mergers;

import java.util.Map;

import com.github.tno.gltsdiff.glts.GLTS;
import com.github.tno.gltsdiff.glts.State;

/**
 * A merger that combines two input GLTSs based on a matching between their states.
 *
 * @param <S> The type of state properties.
 * @param <T> The type of transition properties.
 * @param <U> The type of GLTSs to merge.
 */
@FunctionalInterface
public interface Merger<S, T, U extends GLTS<S, T>> {
    /**
     * Merges the LHS and RHS into a single GLTS. The given (LHS, RHS)-state matching determines which LHS states are to
     * be merged with which RHS states.
     *
     * @param lhs The left-hand-side (LHS) GLTS.
     * @param rhs The right-hand-side (RHS) GLTS.
     * @param matching A matching from LHS states to RHS states. This matching should be proper in the sense that:
     *     <ul>
     *     <li>All keys are states in the LHS.</li>
     *     <li>All values are states in the RHS.</li>
     *     <li>All mappings are disjoint: no LHS or RHS state is part of more than one match.</li>
     *     <li>All matched states must have combinable properties.</li>
     *     </ul>
     * @return The GLTS that is the merge of the LHS and the RHS.
     */
    public U merge(U lhs, U rhs, Map<State<S>, State<S>> matching);
}
