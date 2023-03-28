//////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2023 Contributors to the GitHub community
//
// This program and the accompanying materials are made available
// under the terms of the MIT License which is available at
// https://opensource.org/licenses/MIT
//
// SPDX-License-Identifier: MIT
//////////////////////////////////////////////////////////////////////////////

package com.github.tno.gltsdiff.builders;

import com.github.tno.gltsdiff.glts.lts.automaton.Automaton;
import com.github.tno.gltsdiff.glts.lts.automaton.AutomatonStateProperty;
import com.github.tno.gltsdiff.writers.lts.automaton.AutomatonDotWriter;

/**
 * Builder to more easily configure the various settings for comparing, merging and writing {@link Automaton automata}
 * and more specialized representations.
 *
 * @param <S> The type of state properties.
 * @param <T> The type of transition properties.
 * @param <U> The type of automata to compare and combine.
 */
public abstract class AutomatonStructureComparatorBuilder<S extends AutomatonStateProperty, T,
        U extends Automaton<S, T>> extends LTSStructureComparatorBuilder<S, T, U>
{
    @Override
    public StructureComparatorBuilder<S, T, U> setDefaultDotWriter() {
        return setDotWriter((sp, tp) -> new AutomatonDotWriter<>(sp, tp));
    }
}