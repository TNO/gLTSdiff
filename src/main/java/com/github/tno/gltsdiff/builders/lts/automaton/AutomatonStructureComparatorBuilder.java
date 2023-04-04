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
import com.github.tno.gltsdiff.glts.lts.automaton.Automaton;
import com.github.tno.gltsdiff.glts.lts.automaton.AutomatonStateProperty;
import com.github.tno.gltsdiff.operators.combiners.lts.automaton.AutomatonStatePropertyCombiner;

/**
 * {@link BaseStructureComparatorBuilder Structure comparator builder} to more easily configure the various settings for
 * comparing, merging and (re)writing {@link Automaton automata}.
 *
 * @param <T> The type of transition properties.
 */
public class AutomatonStructureComparatorBuilder<T>
        extends BaseAutomatonStructureComparatorBuilder<AutomatonStateProperty, T, Automaton<T>>
{
    @Override
    public BaseStructureComparatorBuilder<AutomatonStateProperty, T, Automaton<T>> setDefaultInstantiator() {
        return setInstantiator(() -> new Automaton<>());
    }

    @Override
    public BaseStructureComparatorBuilder<AutomatonStateProperty, T, Automaton<T>> setDefaultStatePropertyCombiner() {
        return setStatePropertyCombiner(new AutomatonStatePropertyCombiner());
    }
}
