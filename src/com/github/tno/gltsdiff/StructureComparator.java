//////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2021-2022 Contributors to the GitHub community
//
// This program and the accompanying materials are made available
// under the terms of the MIT License which is available at
// https://opensource.org/licenses/MIT
//
// SPDX-License-Identifier: MIT
//////////////////////////////////////////////////////////////////////////////

package com.github.tno.gltsdiff;

import java.util.Map;

import com.github.tno.gltsdiff.lts.GLTS;
import com.github.tno.gltsdiff.lts.State;
import com.github.tno.gltsdiff.matchers.Matcher;
import com.github.tno.gltsdiff.mergers.Merger;

/**
 * Functionality for comparing two given input LTSs, which we refer to as the left-hand-side (LHS) and the
 * right-hand-side (RHS), and combining them into a single LTS.
 *
 * @param <S> The type of state properties.
 * @param <T> The type of transition properties.
 * @param <U> The type of LTSs to compare and combine.
 */
public class StructureComparator<S, T, U extends GLTS<S, T>> {
    /** The left-hand-side LTS. */
    private final U lhs;

    /** The right-hand-side LTS. */
    private final U rhs;

    /** The component that determines which (LHS, RHS)-state pairs should be merged into a single state. */
    private final Matcher<S, T, U> matcher;

    /** The component that merges LHS and RHS into a single LTS. */
    private final Merger<S, T, U> merger;

    /**
     * Initializes a comparator for the graph structures of the specified LHS and RHS.
     * 
     * @param lhs The left-hand-side LTS.
     * @param rhs The right-hand-side LTS.
     * @param matcher The component that determines which (LHS, RHS)-state pairs should be merged into a single state.
     * @param merger The component that merges LHS and RHS into a single LTS.
     */
    public StructureComparator(U lhs, U rhs, Matcher<S, T, U> matcher, Merger<S, T, U> merger) {
        this.lhs = lhs;
        this.rhs = rhs;
        this.matcher = matcher;
        this.merger = merger;
    }

    /** @return The enclosed left-hand-side LTS. */
    public U getLhs() {
        return lhs;
    }

    /** @return The enclosed right-hand-side LTS. */
    public U getRhs() {
        return rhs;
    }

    /** @return The LTS describing the combination of the LHS and RHS. */
    public U compare() {
        Map<State<S>, State<S>> matching = matcher.compute();
        return merger.merge(matching);
    }
}
