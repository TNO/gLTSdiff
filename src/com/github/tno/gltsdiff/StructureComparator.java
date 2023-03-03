//////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2021-2023 Contributors to the GitHub community
//
// This program and the accompanying materials are made available
// under the terms of the MIT License which is available at
// https://opensource.org/licenses/MIT
//
// SPDX-License-Identifier: MIT
//////////////////////////////////////////////////////////////////////////////

package com.github.tno.gltsdiff;

import java.util.Map;

import com.github.tno.gltsdiff.glts.GLTS;
import com.github.tno.gltsdiff.glts.State;
import com.github.tno.gltsdiff.matchers.Matcher;
import com.github.tno.gltsdiff.mergers.Merger;

/**
 * Functionality for comparing two given input GLTSs, which we refer to as the left-hand-side (LHS) and the
 * right-hand-side (RHS), and combining them into a single GLTS.
 *
 * @param <S> The type of state properties.
 * @param <T> The type of transition properties.
 * @param <U> The type of GLTSs to compare and combine.
 */
public class StructureComparator<S, T, U extends GLTS<S, T>> {
    /** The component that determines which (LHS, RHS)-state pairs should be merged into a single state. */
    private final Matcher<S, T, U> matcher;

    /** The component that merges LHS and RHS into a single GLTS. */
    private final Merger<S, T, U> merger;

    /**
     * Initializes a comparator.
     *
     * @param matcher The component that determines which (LHS, RHS)-state pairs should be merged into a single state.
     * @param merger The component that merges LHS and RHS into a single GLTS.
     */
    public StructureComparator(Matcher<S, T, U> matcher, Merger<S, T, U> merger) {
        this.matcher = matcher;
        this.merger = merger;
    }

    /**
     * Compare and merge two GLTSs.
     *
     * @param lhs The left-hand-side (LHS) GLTS.
     * @param rhs The right-hand-side (RHS) GLTS.
     * @return The GLTS describing the combination of the LHS and RHS.
     */
    public U compare(U lhs, U rhs) {
        Map<State<S>, State<S>> matching = matcher.compute(lhs, rhs);
        return merger.merge(lhs, rhs, matching);
    }
}
