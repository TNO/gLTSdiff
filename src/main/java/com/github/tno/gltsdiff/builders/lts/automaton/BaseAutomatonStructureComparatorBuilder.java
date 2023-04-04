//////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2023 Contributors to the GitHub community
//
// This program and the accompanying materials are made available
// under the terms of the MIT License which is available at
// https://opensource.org/licenses/MIT
//
// SPDX-License-Identifier: MIT
//////////////////////////////////////////////////////////////////////////////

package com.github.tno.gltsdiff.builders.lts.automaton;

import com.github.tno.gltsdiff.builders.BaseStructureComparatorBuilder;
import com.github.tno.gltsdiff.builders.lts.BaseLTSStructureComparatorBuilder;
import com.github.tno.gltsdiff.glts.lts.automaton.Automaton;
import com.github.tno.gltsdiff.glts.lts.automaton.AutomatonStateProperty;
import com.github.tno.gltsdiff.writers.lts.automaton.AutomatonDotWriter;

/**
 * Builder to more easily configure the various settings for comparing, merging and (re)writing {@link Automaton
 * automata} and more specialized representations.
 *
 * @param <S> The type of state properties.
 * @param <T> The type of transition properties.
 * @param <U> The type of automata to compare, combine and (re)write.
 */
public abstract class BaseAutomatonStructureComparatorBuilder<S extends AutomatonStateProperty, T,
        U extends Automaton<S, T>> extends BaseLTSStructureComparatorBuilder<S, T, U>
{
    @Override
    public BaseStructureComparatorBuilder<S, T, U> setDefaultDotWriter() {
        return setDotWriter((sp, tp) -> new AutomatonDotWriter<>(sp, tp));
    }
}
