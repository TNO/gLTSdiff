//////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2021-2022 Contributors to the GitHub community
//
// This program and the accompanying materials are made available
// under the terms of the MIT License which is available at
// https://opensource.org/licenses/MIT
//
// SPDX-License-Identifier: MIT
//////////////////////////////////////////////////////////////////////////////

package com.github.tno.gltsdiff.lts;

/**
 * The intended use of {@link DiffKind} is to associate difference information to states, transitions and initial state
 * arrows.
 */
public enum DiffKind {

    /**
     * Unchanged data, that occurs on both the LHS and RHS automaton.
     */
    UNCHANGED,

    /**
     * Added data, that occurs only on the RHS automaton.
     */
    ADDED,

    /**
     * Removed data, that occurs only on the LHS automaton.
     */
    REMOVED
}
