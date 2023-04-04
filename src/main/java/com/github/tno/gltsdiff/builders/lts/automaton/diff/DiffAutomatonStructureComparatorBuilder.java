//////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2023 Contributors to the GitHub community
//
// This program and the accompanying materials are made available
// under the terms of the MIT License which is available at
// https://opensource.org/licenses/MIT
//
// SPDX-License-Identifier: MIT
//////////////////////////////////////////////////////////////////////////////

package com.github.tno.gltsdiff.builders.lts.automaton.diff;

import com.github.tno.gltsdiff.builders.BaseStructureComparatorBuilder;
import com.github.tno.gltsdiff.glts.lts.automaton.diff.DiffAutomaton;
import com.github.tno.gltsdiff.glts.lts.automaton.diff.DiffAutomatonStateProperty;
import com.github.tno.gltsdiff.glts.lts.automaton.diff.DiffProperty;
import com.github.tno.gltsdiff.operators.combiners.lts.automaton.diff.DiffAutomatonStatePropertyCombiner;

/**
 * {@link BaseStructureComparatorBuilder Structure comparator builder} to more easily configure the various settings for
 * comparing, merging and (re)writing {@link DiffAutomaton difference automata}.
 *
 * @param <T> The type of transition properties.
 */
public class DiffAutomatonStructureComparatorBuilder<T> extends
        BaseDiffAutomatonStructureComparatorBuilder<DiffAutomatonStateProperty, T, DiffAutomaton<DiffAutomatonStateProperty, T>>
{
    @Override
    public BaseStructureComparatorBuilder<DiffAutomatonStateProperty, DiffProperty<T>, DiffAutomaton<DiffAutomatonStateProperty, T>>
            setDefaultInstantiator()
    {
        return setInstantiator(() -> new DiffAutomaton<>());
    }

    @Override
    public BaseStructureComparatorBuilder<DiffAutomatonStateProperty, DiffProperty<T>, DiffAutomaton<DiffAutomatonStateProperty, T>>
            setDefaultStatePropertyCombiner()
    {
        return setStatePropertyCombiner(new DiffAutomatonStatePropertyCombiner());
    }

    @Override
    public BaseStructureComparatorBuilder<DiffAutomatonStateProperty, DiffProperty<T>, DiffAutomaton<DiffAutomatonStateProperty, T>>
            setDefaultDiffAutomatonStatePropertyTransformer()
    {
        return setDiffAutomatonStatePropertyTransformer(
                (sp, sd, id) -> new DiffAutomatonStateProperty(sp.isAccepting(), sd, id));
    }
}
