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

import com.github.tno.gltsdiff.glts.lts.automaton.AutomatonStateProperty;
import com.github.tno.gltsdiff.glts.lts.automaton.SimpleAutomaton;
import com.github.tno.gltsdiff.operators.combiners.lts.automaton.AutomatonStatePropertyCombiner;

/**
 * {@link StructureComparatorBuilder Structure comparator builder} to more easily configure the various settings for
 * comparing, merging and writing {@link SimpleAutomaton simple automata}.
 *
 * @param <T> The type of transition properties.
 */
public class SimpleAutomatonStructureComparatorBuilder<T>
        extends AutomatonStructureComparatorBuilder<AutomatonStateProperty, T, SimpleAutomaton<T>>
{
    @Override
    public StructureComparatorBuilder<AutomatonStateProperty, T, SimpleAutomaton<T>> setDefaultInstantiator() {
        return setInstantiator(() -> new SimpleAutomaton<>());
    }

    @Override
    public StructureComparatorBuilder<AutomatonStateProperty, T, SimpleAutomaton<T>> setDefaultStatePropertyCombiner() {
        return setStatePropertyCombiner(new AutomatonStatePropertyCombiner());
    }
}
