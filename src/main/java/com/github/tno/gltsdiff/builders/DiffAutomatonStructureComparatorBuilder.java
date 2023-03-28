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

import com.github.tno.gltsdiff.glts.lts.automaton.diff.DiffAutomaton;
import com.github.tno.gltsdiff.glts.lts.automaton.diff.DiffAutomatonStateProperty;
import com.github.tno.gltsdiff.glts.lts.automaton.diff.DiffProperty;
import com.github.tno.gltsdiff.operators.combiners.lts.automaton.diff.DiffAutomatonStatePropertyCombiner;
import com.github.tno.gltsdiff.operators.printers.StringHtmlPrinter;
import com.github.tno.gltsdiff.operators.printers.TransitionHtmlPrinter;
import com.github.tno.gltsdiff.operators.printers.lts.automaton.diff.DiffPropertyHtmlPrinter;
import com.github.tno.gltsdiff.rewriters.lts.automaton.diff.EntanglementRewriter;
import com.github.tno.gltsdiff.rewriters.lts.automaton.diff.SkipForkPatternRewriter;
import com.github.tno.gltsdiff.rewriters.lts.automaton.diff.SkipJoinPatternRewriter;
import com.github.tno.gltsdiff.writers.lts.automaton.diff.DiffAutomatonDotWriter;

/**
 * Builder to more easily configure the various settings for comparing, merging and writing {@link DiffAutomaton
 * difference automata}.
 *
 * @param <T> The type of transition properties.
 */
public abstract class DiffAutomatonStructureComparatorBuilder<T>
        extends AutomatonStructureComparatorBuilder<DiffAutomatonStateProperty, DiffProperty<T>, DiffAutomaton<T>>
{
    /** Instantiates a new difference automaton structure comparator builder. */
    public DiffAutomatonStructureComparatorBuilder() {
        super();
        addEntanglementRewriter();
        addSkipForkPatternRewriter();
        addSkipJoinPatternRewriter();
    }

    @Override
    public StructureComparatorBuilder<DiffAutomatonStateProperty, DiffProperty<T>, DiffAutomaton<T>>
            setDefaultInstantiator()
    {
        return setInstantiator(() -> new DiffAutomaton<>());
    }

    @Override
    public StructureComparatorBuilder<DiffAutomatonStateProperty, DiffProperty<T>, DiffAutomaton<T>>
            setDefaultStatePropertyCombiner()
    {
        return setStatePropertyCombiner(new DiffAutomatonStatePropertyCombiner());
    }

    /**
     * Add the {@link EntanglementRewriter} as rewriter.
     *
     * @return This helper, for chaining.
     */
    public StructureComparatorBuilder<DiffAutomatonStateProperty, DiffProperty<T>, DiffAutomaton<T>>
            addEntanglementRewriter()
    {
        return addRewriter((s, t, i, hp) -> new EntanglementRewriter<>());
    }

    /**
     * Add the {@link SkipForkPatternRewriter} as rewriter.
     *
     * @return This helper, for chaining.
     */
    public StructureComparatorBuilder<DiffAutomatonStateProperty, DiffProperty<T>, DiffAutomaton<T>>
            addSkipForkPatternRewriter()
    {
        return addRewriter((s, t, i, hp) -> new SkipForkPatternRewriter<>(t, hp.get(), i));
    }

    /**
     * Add the {@link SkipJoinPatternRewriter} as rewriter.
     *
     * @return This helper, for chaining.
     */
    public StructureComparatorBuilder<DiffAutomatonStateProperty, DiffProperty<T>, DiffAutomaton<T>>
            addSkipJoinPatternRewriter()
    {
        return addRewriter((s, t, i, hp) -> new SkipJoinPatternRewriter<>(t, hp.get(), i));
    }

    @Override
    public StructureComparatorBuilder<DiffAutomatonStateProperty, DiffProperty<T>, DiffAutomaton<T>>
            setDefaultTransitionLabelHtmlPrinter()
    {
        return setTransitionLabelHtmlPrinter(
                new TransitionHtmlPrinter<>(new DiffPropertyHtmlPrinter<>(new StringHtmlPrinter<>())));
    }

    @Override
    public StructureComparatorBuilder<DiffAutomatonStateProperty, DiffProperty<T>, DiffAutomaton<T>>
            setDefaultDotWriter()
    {
        return setDotWriter((sp, tp) -> new DiffAutomatonDotWriter<>(sp, tp));
    }
}
