//////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2021-2023 Contributors to the GitHub community
//
// This program and the accompanying materials are made available
// under the terms of the MIT License which is available at
// https://opensource.org/licenses/MIT
//
// SPDX-License-Identifier: MIT
//////////////////////////////////////////////////////////////////////////////

package com.github.tno.gltsdiff.glts.lts.automaton;

import com.github.tno.gltsdiff.glts.lts.LTS;

/**
 * An automaton, an {@link LTS} with accepting state information for states.
 *
 * @param <S> The type of automaton state properties.
 * @param <T> The type of transition properties.
 */
public class Automaton<S extends AutomatonStateProperty, T> extends LTS<S, T> {
    // Nothing to add or override.
}
