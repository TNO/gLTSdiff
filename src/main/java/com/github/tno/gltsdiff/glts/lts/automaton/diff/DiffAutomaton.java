//////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2021-2023 Contributors to the GitHub community
//
// This program and the accompanying materials are made available
// under the terms of the MIT License which is available at
// https://opensource.org/licenses/MIT
//
// SPDX-License-Identifier: MIT
//////////////////////////////////////////////////////////////////////////////

package com.github.tno.gltsdiff.glts.lts.automaton.diff;

import com.github.tno.gltsdiff.glts.lts.automaton.Automaton;

/**
 * A difference automaton, a {@link Automaton automaton} with difference information for states, initial states and
 * transitions.
 *
 * @param <T> The type of transition properties.
 */
public class DiffAutomaton<T> extends BaseDiffAutomaton<DiffAutomatonStateProperty, T> {
    // Nothing to add or override.
}
