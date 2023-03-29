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

import com.github.tno.gltsdiff.builders.StructureComparatorBuilder;
import com.github.tno.gltsdiff.builders.lts.automaton.AutomatonStructureComparatorBuilder;
import com.github.tno.gltsdiff.glts.lts.automaton.diff.DiffAutomaton;
import com.github.tno.gltsdiff.glts.lts.automaton.diff.DiffAutomatonStateProperty;
import com.github.tno.gltsdiff.glts.lts.automaton.diff.DiffProperty;
import com.github.tno.gltsdiff.operators.combiners.Combiner;
import com.github.tno.gltsdiff.operators.combiners.EqualityCombiner;
import com.github.tno.gltsdiff.operators.combiners.lts.automaton.diff.DiffAutomatonStatePropertyCombiner;
import com.github.tno.gltsdiff.operators.combiners.lts.automaton.diff.DiffPropertyCombiner;
import com.github.tno.gltsdiff.operators.hiders.DiffPropertyHider;
import com.github.tno.gltsdiff.operators.hiders.Hider;
import com.github.tno.gltsdiff.operators.printers.HtmlPrinter;
import com.github.tno.gltsdiff.operators.printers.StringHtmlPrinter;
import com.github.tno.gltsdiff.operators.printers.lts.automaton.diff.DiffPropertyHtmlPrinter;
import com.github.tno.gltsdiff.rewriters.lts.automaton.diff.EntanglementRewriter;
import com.github.tno.gltsdiff.rewriters.lts.automaton.diff.SkipForkPatternRewriter;
import com.github.tno.gltsdiff.rewriters.lts.automaton.diff.SkipJoinPatternRewriter;
import com.github.tno.gltsdiff.writers.lts.automaton.diff.DiffAutomatonDotWriter;

/**
 * Builder to more easily configure the various settings for comparing, merging and (re)writing {@link DiffAutomaton
 * difference automata}.
 *
 * @param <T> The type of transition properties.
 */
public class DiffAutomatonStructureComparatorBuilder<T>
        extends AutomatonStructureComparatorBuilder<DiffAutomatonStateProperty, DiffProperty<T>, DiffAutomaton<T>>
{
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
     * Set the transition property combiner, based on a combiner for the sub-properties of {@link DiffProperty}.
     *
     * @param subPropertyCombiner The {@link DiffProperty} sub-property combiner.
     * @return This builder, for chaining.
     */
    public StructureComparatorBuilder<DiffAutomatonStateProperty, DiffProperty<T>, DiffAutomaton<T>>
            setTransitionSubPropertyCombiner(Combiner<T> subPropertyCombiner)
    {
        return setTransitionPropertyCombiner(new DiffPropertyCombiner<>(subPropertyCombiner));
    }

    @Override
    public StructureComparatorBuilder<DiffAutomatonStateProperty, DiffProperty<T>, DiffAutomaton<T>>
            setDefaultTransitionPropertyCombiner()
    {
        return setTransitionSubPropertyCombiner(new EqualityCombiner<>());
    }

    /**
     * Set the transition property hider, based on a hider for the sub-properties of {@link DiffProperty}.
     *
     * @param subPropertyHider The {@link DiffProperty} sub-property hider.
     * @return This builder, for chaining.
     */
    public StructureComparatorBuilder<DiffAutomatonStateProperty, DiffProperty<T>, DiffAutomaton<T>>
            setTransitionSubPropertyHider(Hider<T> subPropertyHider)
    {
        return setTransitionPropertyHider(new DiffPropertyHider<>(subPropertyHider));
    }

    @Override
    public StructureComparatorBuilder<DiffAutomatonStateProperty, DiffProperty<T>, DiffAutomaton<T>>
            addDefaultRewriters()
    {
        addEntanglementRewriter();
        super.addDefaultRewriters();
        addSkipForkPatternRewriter();
        addSkipJoinPatternRewriter();
        return this;
    }

    /**
     * Add the {@link EntanglementRewriter} as rewriter.
     *
     * @return This builder, for chaining.
     */
    public StructureComparatorBuilder<DiffAutomatonStateProperty, DiffProperty<T>, DiffAutomaton<T>>
            addEntanglementRewriter()
    {
        return addRewriter((s, t, i, hp) -> new EntanglementRewriter<>());
    }

    /**
     * Add the {@link SkipForkPatternRewriter} as rewriter.
     *
     * @return This builder, for chaining.
     */
    public StructureComparatorBuilder<DiffAutomatonStateProperty, DiffProperty<T>, DiffAutomaton<T>>
            addSkipForkPatternRewriter()
    {
        return addRewriter((s, t, i, hp) -> new SkipForkPatternRewriter<>(t, hp.get(), i));
    }

    /**
     * Add the {@link SkipJoinPatternRewriter} as rewriter.
     *
     * @return This builder, for chaining.
     */
    public StructureComparatorBuilder<DiffAutomatonStateProperty, DiffProperty<T>, DiffAutomaton<T>>
            addSkipJoinPatternRewriter()
    {
        return addRewriter((s, t, i, hp) -> new SkipJoinPatternRewriter<>(t, hp.get(), i));
    }

    /**
     * Set the transition label HTML printer, based on a printer for the sub-properties of {@link DiffProperty}.
     *
     * @param subPropertyPrinter The {@link DiffProperty} sub-property HTML printer.
     * @return This builder, for chaining.
     */
    public StructureComparatorBuilder<DiffAutomatonStateProperty, DiffProperty<T>, DiffAutomaton<T>>
            setTransitionSubPropertyHtmlPrinter(HtmlPrinter<T> subPropertyPrinter)
    {
        return setTransitionPropertyHtmlPrinter(new DiffPropertyHtmlPrinter<>(subPropertyPrinter));
    }

    @Override
    public StructureComparatorBuilder<DiffAutomatonStateProperty, DiffProperty<T>, DiffAutomaton<T>>
            setDefaultTransitionLabelHtmlPrinter()
    {
        return setTransitionSubPropertyHtmlPrinter(new StringHtmlPrinter<>());
    }

    @Override
    public StructureComparatorBuilder<DiffAutomatonStateProperty, DiffProperty<T>, DiffAutomaton<T>>
            setDefaultDotWriter()
    {
        return setDotWriter((sp, tp) -> new DiffAutomatonDotWriter<>(sp, tp));
    }
}
