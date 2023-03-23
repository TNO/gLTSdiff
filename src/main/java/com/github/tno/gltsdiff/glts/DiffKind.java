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

/**
 * A difference kind.
 *
 * <p>
 * The intended use is to associate difference information to for example states, transitions and initial state arrows.
 * </p>
 */
public enum DiffKind {
    /** Unchanged data, that occurs in both the LHS and RHS automaton. */
    UNCHANGED,

    /** Added data, that occurs only in the RHS automaton. */
    ADDED,

    /** Removed data, that occurs only in the LHS automaton. */
    REMOVED,
}
