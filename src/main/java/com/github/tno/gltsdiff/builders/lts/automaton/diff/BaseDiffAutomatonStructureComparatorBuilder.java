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

import java.util.Optional;
import java.util.function.Supplier;

import com.github.tno.gltsdiff.builders.BaseStructureComparatorBuilder;
import com.github.tno.gltsdiff.builders.lts.automaton.BaseAutomatonStructureComparatorBuilder;
import com.github.tno.gltsdiff.glts.lts.automaton.diff.BaseDiffAutomaton;
import com.github.tno.gltsdiff.glts.lts.automaton.diff.DiffAutomatonStateProperty;
import com.github.tno.gltsdiff.glts.lts.automaton.diff.DiffKind;
import com.github.tno.gltsdiff.glts.lts.automaton.diff.DiffProperty;
import com.github.tno.gltsdiff.operators.combiners.Combiner;
import com.github.tno.gltsdiff.operators.combiners.EqualityCombiner;
import com.github.tno.gltsdiff.operators.combiners.lts.automaton.diff.DiffPropertyCombiner;
import com.github.tno.gltsdiff.operators.hiders.Hider;
import com.github.tno.gltsdiff.operators.hiders.lts.automaton.diff.DiffPropertyHider;
import com.github.tno.gltsdiff.operators.inclusions.Inclusion;
import com.github.tno.gltsdiff.operators.printers.HtmlPrinter;
import com.github.tno.gltsdiff.operators.printers.StringHtmlPrinter;
import com.github.tno.gltsdiff.operators.printers.lts.automaton.diff.DiffPropertyHtmlPrinter;
import com.github.tno.gltsdiff.rewriters.Rewriter;
import com.github.tno.gltsdiff.rewriters.lts.automaton.diff.EntanglementRewriter;
import com.github.tno.gltsdiff.rewriters.lts.automaton.diff.SkipForkPatternRewriter;
import com.github.tno.gltsdiff.rewriters.lts.automaton.diff.SkipJoinPatternRewriter;
import com.github.tno.gltsdiff.utils.QuintFunction;
import com.github.tno.gltsdiff.utils.TriFunction;
import com.github.tno.gltsdiff.writers.lts.automaton.diff.DiffAutomatonDotWriter;
import com.google.common.base.Preconditions;

/**
 * {@link BaseStructureComparatorBuilder Structure comparator builder} to more easily configure the various settings for
 * comparing, merging and (re)writing {@link BaseDiffAutomaton difference automata} and more specialized
 * representations.
 *
 * @param <S> The type of difference automaton state properties.
 * @param <T> The type of transition properties.
 * @param <U> The type of difference automata to compare, combine and (re)write.
 */
public abstract class BaseDiffAutomatonStructureComparatorBuilder<S extends DiffAutomatonStateProperty, T,
        U extends BaseDiffAutomaton<S, T>> extends BaseAutomatonStructureComparatorBuilder<S, DiffProperty<T>, U>
{
    /** Difference automaton state property transformer. */
    private TriFunction<S, DiffKind, Optional<DiffKind>, S> statePropertyTransformer;

    /** Instantiates a new base difference automaton structure comparator builder. */
    public BaseDiffAutomatonStructureComparatorBuilder() {
        setDefaultDiffAutomatonStatePropertyTransformer();
    }

    /**
     * Set the transition property combiner, based on a combiner for {@link DiffProperty} sub-properties.
     *
     * @param subPropertyCombiner The {@link DiffProperty} sub-property combiner.
     * @return This builder, for chaining.
     */
    public BaseStructureComparatorBuilder<S, DiffProperty<T>, U>
            setDiffAutomatonTransitionPropertyCombiner(Combiner<T> subPropertyCombiner)
    {
        return setTransitionPropertyCombiner(new DiffPropertyCombiner<>(subPropertyCombiner));
    }

    @Override
    public BaseStructureComparatorBuilder<S, DiffProperty<T>, U> setDefaultTransitionPropertyCombiner() {
        return setDiffAutomatonTransitionPropertyCombiner(new EqualityCombiner<>());
    }

    /**
     * Set the transition property hider, based on a hider for {@link DiffProperty} sub-properties.
     *
     * @param subPropertyHider The {@link DiffProperty} sub-property hider.
     * @return This builder, for chaining.
     */
    public BaseStructureComparatorBuilder<S, DiffProperty<T>, U>
            setDiffAutomatonTransitionPropertyHider(Hider<T> subPropertyHider)
    {
        return setTransitionPropertyHider(new DiffPropertyHider<>(subPropertyHider));
    }

    /**
     * Set the difference automaton state property transformer.
     *
     * @param statePropertyTransformer The difference automaton state property transformer .
     * @return This builder, for chaining.
     */
    public BaseStructureComparatorBuilder<S, DiffProperty<T>, U> setDiffAutomatonStatePropertyTransformer(
            TriFunction<S, DiffKind, Optional<DiffKind>, S> statePropertyTransformer)
    {
        Preconditions.checkNotNull(statePropertyTransformer,
                "Expected a non-null difference automaton state property transformer.");
        this.statePropertyTransformer = statePropertyTransformer;
        return this;
    }

    /**
     * Set the default difference automaton state property transformer.
     *
     * @return This builder, for chaining.
     */
    public abstract BaseStructureComparatorBuilder<S, DiffProperty<T>, U>
            setDefaultDiffAutomatonStatePropertyTransformer();

    @Override
    public BaseStructureComparatorBuilder<S, DiffProperty<T>, U> addDefaultRewriters() {
        addEntanglementRewriter();
        super.addDefaultRewriters();
        addSkipForkPatternRewriter();
        addSkipJoinPatternRewriter();
        return this;
    }

    /**
     * Add a difference automaton rewriter.
     *
     * @param rewriterProvider The rewriter provider that creates a rewriter, given a state property combiner, a
     *     transition property combiner, a transition property inclusion operator, a transition property hider provider
     *     and a transition property transformer.
     * @return This builder, for chaining.
     */
    public BaseStructureComparatorBuilder<S, DiffProperty<T>, U> addDiffAutomatonRewriter(
            QuintFunction<Combiner<S>, Combiner<DiffProperty<T>>, Inclusion<DiffProperty<T>>, Supplier<Hider<DiffProperty<T>>>, TriFunction<S, DiffKind, Optional<DiffKind>, S>, Rewriter<S, DiffProperty<T>, U>> rewriterProvider)
    {
        return addRewriter((s, t, i, hp) -> rewriterProvider.apply(s, t, i, hp, getStatePropertyTransformer()));
    }

    /**
     * Add the {@link EntanglementRewriter} as rewriter.
     *
     * @return This builder, for chaining.
     */
    public BaseStructureComparatorBuilder<S, DiffProperty<T>, U> addEntanglementRewriter() {
        return addDiffAutomatonRewriter((s, t, i, hp, tf) -> new EntanglementRewriter<>(tf));
    }

    /**
     * Add the {@link SkipForkPatternRewriter} as rewriter.
     *
     * @return This builder, for chaining.
     */
    public BaseStructureComparatorBuilder<S, DiffProperty<T>, U> addSkipForkPatternRewriter() {
        return addDiffAutomatonRewriter((s, t, i, hp, tf) -> new SkipForkPatternRewriter<>(s, t, hp.get(), i, tf));
    }

    /**
     * Add the {@link SkipJoinPatternRewriter} as rewriter.
     *
     * @return This builder, for chaining.
     */
    public BaseStructureComparatorBuilder<S, DiffProperty<T>, U> addSkipJoinPatternRewriter() {
        return addDiffAutomatonRewriter((s, t, i, hp, tf) -> new SkipJoinPatternRewriter<>(s, t, hp.get(), i, tf));
    }

    /**
     * Set the transition label HTML printer, based on a printer for {@link DiffProperty} sub-properties.
     *
     * @param subPropertyPrinter The {@link DiffProperty} sub-property HTML printer.
     * @return This builder, for chaining.
     */
    public BaseStructureComparatorBuilder<S, DiffProperty<T>, U>
            setDiffAutomatonTransitionPropertyHtmlPrinter(HtmlPrinter<T> subPropertyPrinter)
    {
        return setTransitionPropertyHtmlPrinter(new DiffPropertyHtmlPrinter<>(subPropertyPrinter));
    }

    @Override
    public BaseStructureComparatorBuilder<S, DiffProperty<T>, U> setDefaultTransitionLabelHtmlPrinter() {
        return setDiffAutomatonTransitionPropertyHtmlPrinter(new StringHtmlPrinter<>());
    }

    @Override
    public BaseStructureComparatorBuilder<S, DiffProperty<T>, U> setDefaultDotWriter() {
        return setDotWriter((sp, tp) -> new DiffAutomatonDotWriter<>(sp, tp));
    }

    /**
     * Get the configured difference automaton state property transformer.
     *
     * @return The difference automaton state property transformer.
     */
    protected TriFunction<S, DiffKind, Optional<DiffKind>, S> getStatePropertyTransformer() {
        if (statePropertyTransformer == null) {
            throw new IllegalStateException(
                    "The difference automaton state property transformer is not yet configured. "
                            + "Configure it before invoking any of the builder's creation methods.");
        }
        return statePropertyTransformer;
    }
}
