//////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2021-2022 Contributors to the GitHub community
//
// This program and the accompanying materials are made available
// under the terms of the MIT License which is available at
// https://opensource.org/licenses/MIT
//
// SPDX-License-Identifier: MIT
//////////////////////////////////////////////////////////////////////////////

package com.github.tno.gltsdiff.mergers;

import java.util.Map;

import com.github.tno.gltsdiff.lts.LTS;
import com.github.tno.gltsdiff.lts.State;

/**
 * A merger that combines two input LTSs based on a matching between their states.
 *
 * @param <S> The type of state properties.
 * @param <T> The type of transition properties.
 * @param <U> The type of LTSs to merge.
 */
public interface Merger<S, T, U extends LTS<S, T>> {
    /** @return The left-hand-side LTS. */
    public U getLhs();

    /** @return The right-hand-side LTS. */
    public U getRhs();

    /**
     * Merges the LHS and RHS into a single LTS. The given (LHS, RHS)-state matching determines which LHS states are to
     * be merged with which RHS states.
     * 
     * @param matching A matching from LHS states to RHS states. This matching should be proper in the sense that:
     *     <ul>
     *     <li>All keys are states in the LHS.</li>
     *     <li>All values are states in the RHS.</li>
     *     <li>All mappings are disjoint: no LHS or RHS state is part of more than one match.</li>
     *     <li>All matched states must have combinable properties.</li>
     *     </ul>
     * @return The LTS that is the merge of the LHS and the RHS.
     */
    public U merge(Map<State<S>, State<S>> matching);
}
