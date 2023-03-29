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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.github.tno.gltsdiff.StructureComparator;
import com.github.tno.gltsdiff.glts.GLTS;
import com.github.tno.gltsdiff.glts.State;
import com.github.tno.gltsdiff.glts.Transition;
import com.github.tno.gltsdiff.matchers.BruteForceMatcher;
import com.github.tno.gltsdiff.matchers.DynamicMatcher;
import com.github.tno.gltsdiff.matchers.KuhnMunkresMatcher;
import com.github.tno.gltsdiff.matchers.Matcher;
import com.github.tno.gltsdiff.matchers.WalkinshawMatcher;
import com.github.tno.gltsdiff.mergers.DefaultMerger;
import com.github.tno.gltsdiff.mergers.Merger;
import com.github.tno.gltsdiff.operators.combiners.Combiner;
import com.github.tno.gltsdiff.operators.combiners.EqualityCombiner;
import com.github.tno.gltsdiff.operators.hiders.Hider;
import com.github.tno.gltsdiff.operators.inclusions.EqualToCombinationInclusion;
import com.github.tno.gltsdiff.operators.inclusions.Inclusion;
import com.github.tno.gltsdiff.operators.printers.HtmlPrinter;
import com.github.tno.gltsdiff.operators.printers.StateHtmlPrinter;
import com.github.tno.gltsdiff.operators.printers.StringHtmlPrinter;
import com.github.tno.gltsdiff.operators.printers.TransitionHtmlPrinter;
import com.github.tno.gltsdiff.rewriters.FixedPointRewriter;
import com.github.tno.gltsdiff.rewriters.LocalRedundancyRewriter;
import com.github.tno.gltsdiff.rewriters.Rewriter;
import com.github.tno.gltsdiff.rewriters.SequenceRewriter;
import com.github.tno.gltsdiff.scorers.DynamicScorer;
import com.github.tno.gltsdiff.scorers.SimilarityScorer;
import com.github.tno.gltsdiff.scorers.WalkinshawGlobalScorer;
import com.github.tno.gltsdiff.scorers.WalkinshawLocalScorer;
import com.github.tno.gltsdiff.utils.QuadFunction;
import com.github.tno.gltsdiff.utils.TriFunction;
import com.github.tno.gltsdiff.writers.DotWriter;
import com.google.common.base.Preconditions;

/**
 * Structure comparator builder to more easily configure the various settings for comparing, merging and (re)writing
 * {@link GLTS GLTSs} and more specialized representations.
 *
 * <p>
 * This class provides:
 * </p>
 * <ul>
 * <li>Configuration: Default configurations for various settings are provided, but they can all be changed.</li>
 * <li>Building: Building a {@link StructureComparator} and a {@link DotWriter}.</li>
 * </ul>
 *
 * <p>
 * This class is for comparing, merging and (re)writing of GLTSs. Derived classes provide such functionality tailored to
 * more specific GLTS representations. They may for instance provide their own defaults, as well as additional
 * configuration.
 * </p>
 *
 * @param <S> The type of state properties.
 * @param <T> The type of transition properties.
 * @param <U> The type of GLTSs to compare and combine.
 */
public abstract class StructureComparatorBuilder<S, T, U extends GLTS<S, T>> {
    /** GLTS instantiator. */
    private Supplier<U> instantiator;

    /** State property combiner. */
    private Combiner<S> statePropertyCombiner;

    /** Transition property combiner. */
    private Combiner<T> transitionPropertyCombiner;

    /** Scorer provider. */
    private BiFunction<Combiner<S>, Combiner<T>, SimilarityScorer<S, T, U>> scorerProvider;

    /** Matcher provider. */
    private TriFunction<Combiner<S>, Combiner<T>, SimilarityScorer<S, T, U>, Matcher<S, T, U>> matcherProvider;

    /** Merger provider. */
    private TriFunction<Combiner<S>, Combiner<T>, Supplier<U>, Merger<S, T, U>> mergerProvider;

    /** Transition property inclusion operator. */
    private Inclusion<T> inclusion;

    /** Transition property hider provider. */
    private Supplier<Hider<T>> hiderProvider;

    /** Rewriter providers. */
    private List<QuadFunction<Combiner<S>, Combiner<T>, Inclusion<T>, Supplier<Hider<T>>, Rewriter<S, T, U>>> rewriterProviders;

    /** State label HTML printer. */
    private HtmlPrinter<State<S>> stateLabelPrinter;

    /** Transition label HTML printer. */
    private HtmlPrinter<Transition<S, T>> transitionLabelPrinter;

    /** Writer provider. */
    private BiFunction<HtmlPrinter<State<S>>, HtmlPrinter<Transition<S, T>>, DotWriter<S, T, U>> writerProvider;

    /** Instantiates a new GLTS structure comparator builder. */
    public StructureComparatorBuilder() {
        setDefaultInstantiator();
        setDefaultStatePropertyCombiner();
        setDefaultTransitionPropertyCombiner();
        setDynamicScorer();
        setDynamicMatcher();
        setDefaultMerger();
        setEqualToCombinationInclusionOperator();
        setThrowingTransitionPropertyHider();
        addDefaultRewriters();
        setDefaultStateLabelHtmlPrinter();
        setDefaultTransitionLabelHtmlPrinter();
        setDefaultDotWriter();
    }

    /**
     * Set the GLTS instantiator to use to create new GLTSs.
     *
     * @param instantiator The GLTS instantiator.
     * @return This builder, for chaining.
     */
    public StructureComparatorBuilder<S, T, U> setInstantiator(Supplier<U> instantiator) {
        Preconditions.checkNotNull(instantiator, "Expected a non-null instantiator.");
        this.instantiator = instantiator;
        return this;
    }

    /**
     * Set default instantiator.
     *
     * @return This builder, for chaining.
     */
    public abstract StructureComparatorBuilder<S, T, U> setDefaultInstantiator();

    /**
     * Set the state property combiner.
     *
     * @param statePropertyCombiner The state property combiner.
     * @return This builder, for chaining.
     */
    public StructureComparatorBuilder<S, T, U> setStatePropertyCombiner(Combiner<S> statePropertyCombiner) {
        Preconditions.checkNotNull(statePropertyCombiner, "Expected a non-null state property combiner.");
        this.statePropertyCombiner = statePropertyCombiner;
        return this;
    }

    /**
     * Set {@link EqualityCombiner} as state property combiner.
     *
     * @return This builder, for chaining.
     */
    public StructureComparatorBuilder<S, T, U> setDefaultStatePropertyCombiner() {
        return setStatePropertyCombiner(new EqualityCombiner<>());
    }

    /**
     * Set the transition property combiner.
     *
     * @param transitionPropertyCombiner The transition property combiner.
     * @return This builder, for chaining.
     */
    public StructureComparatorBuilder<S, T, U> setTransitionPropertyCombiner(Combiner<T> transitionPropertyCombiner) {
        Preconditions.checkNotNull(transitionPropertyCombiner, "Expected a non-null transition property combiner.");
        this.transitionPropertyCombiner = transitionPropertyCombiner;
        return this;
    }

    /**
     * Set {@link EqualityCombiner} as transition property combiner.
     *
     * @return This builder, for chaining.
     */
    public StructureComparatorBuilder<S, T, U> setDefaultTransitionPropertyCombiner() {
        return setTransitionPropertyCombiner(new EqualityCombiner<>());
    }

    /**
     * Set the similarity scorer.
     *
     * @param scorerProvider The scorer provider that creates a similarity scorer, given a state property combiner and a
     *     transition property combiner.
     * @return This builder, for chaining.
     */
    public StructureComparatorBuilder<S, T, U>
            setScorer(BiFunction<Combiner<S>, Combiner<T>, SimilarityScorer<S, T, U>> scorerProvider)
    {
        Preconditions.checkNotNull(scorerProvider, "Expected a non-null scorer provider.");
        this.scorerProvider = scorerProvider;
        return this;
    }

    /**
     * Set the {@link DynamicScorer} as scorer.
     *
     * @return This builder, for chaining.
     */
    public StructureComparatorBuilder<S, T, U> setDynamicScorer() {
        return setScorer((s, t) -> new DynamicScorer<>(s, t));
    }

    /**
     * Set the {@link WalkinshawGlobalScorer} as scorer.
     *
     * @return This builder, for chaining.
     */
    public StructureComparatorBuilder<S, T, U> setWalkinshawGlobalScorer() {
        return setScorer((s, t) -> new WalkinshawGlobalScorer<>(s, t));
    }

    /**
     * Set the {@link WalkinshawGlobalScorer} as scorer.
     *
     * @param attenuationFactor The attenuation factor, the ratio in the range [0,1] that determines how much the
     *     similarity scores of far-away states influence the final similarity scores. This factor can be tweaked a bit
     *     if the comparison results come out unsatisfactory. A ratio of 0 would mean that only local similarity scores
     *     are used. Note that, if one is only interested in local similarity, {@link WalkinshawLocalScorer} should be
     *     used instead, which gives the same result but is much faster. A ratio of 1 would mean that far-away state
     *     similarities contribute equally much as local ones.
     * @return This builder, for chaining.
     */
    public StructureComparatorBuilder<S, T, U> setWalkinshawGlobalScorer(double attenuationFactor) {
        return setScorer((s, t) -> new WalkinshawGlobalScorer<>(s, t, attenuationFactor));
    }

    /**
     * Set the {@link WalkinshawLocalScorer} as scorer.
     *
     * @return This builder, for chaining.
     */
    public StructureComparatorBuilder<S, T, U> setWalkinshawLocalScorer() {
        return setScorer((s, t) -> new WalkinshawLocalScorer<>(s, t));
    }

    /**
     * Set the {@link WalkinshawLocalScorer} as scorer.
     *
     * @param nrOfRefinements The number of refinements to perform, which must be at least 1.
     * @return This builder, for chaining.
     */
    public StructureComparatorBuilder<S, T, U> setWalkinshawLocalScorer(int nrOfRefinements) {
        return setScorer((s, t) -> new WalkinshawLocalScorer<>(s, t, nrOfRefinements));
    }

    /**
     * Set the {@link WalkinshawLocalScorer} as scorer.
     *
     * @param nrOfRefinements The number of refinements to perform, which must be at least 1.
     * @param attenuationFactor The attenuation factor, the ratio in the range [0,1] that determines how much the
     *     similarity scores of far-away states influence the final similarity scores. This factor can be tweaked a bit
     *     if the comparison results come out unsatisfactory. A ratio of 0 would mean that only local similarity scores
     *     are used. Note that, if one is only interested in local similarity, {@link WalkinshawLocalScorer} should be
     *     used instead, which gives the same result but is much faster. A ratio of 1 would mean that far-away state
     *     similarities contribute equally much as local ones.
     * @return This builder, for chaining.
     */
    public StructureComparatorBuilder<S, T, U> setWalkinshawLocalScorer(int nrOfRefinements, double attenuationFactor) {
        return setScorer((s, t) -> new WalkinshawLocalScorer<>(s, t, nrOfRefinements, attenuationFactor));
    }

    /**
     * Set the matcher.
     *
     * @param matcherProvider The matcher provider that creates a matcher, given a state property combiner and a
     *     transition property combiner.
     *
     * @return This builder, for chaining.
     */
    public StructureComparatorBuilder<S, T, U> setMatcher(
            TriFunction<Combiner<S>, Combiner<T>, SimilarityScorer<S, T, U>, Matcher<S, T, U>> matcherProvider)
    {
        Preconditions.checkNotNull(matcherProvider, "Expected a non-null matcher provider.");
        this.matcherProvider = matcherProvider;
        return this;
    }

    /**
     * Set the {@link BruteForceMatcher} as matcher.
     *
     * @return This builder, for chaining.
     */
    public StructureComparatorBuilder<S, T, U> setBruteForceMatcher() {
        return setMatcher((s, t, sc) -> new BruteForceMatcher<>(s, t));
    }

    /**
     * Set the {@link DynamicMatcher} as matcher.
     *
     * @return This builder, for chaining.
     */
    public StructureComparatorBuilder<S, T, U> setDynamicMatcher() {
        return setMatcher((s, t, sc) -> new DynamicMatcher<>(s, t));
    }

    /**
     * Set the {@link KuhnMunkresMatcher} as matcher.
     *
     * @return This builder, for chaining.
     */
    public StructureComparatorBuilder<S, T, U> setKuhnMunkresMatcher() {
        return setMatcher((s, t, sc) -> new KuhnMunkresMatcher<>(sc, s));
    }

    /**
     * Set the {@link WalkinshawMatcher} as matcher.
     *
     * @return This builder, for chaining.
     */
    public StructureComparatorBuilder<S, T, U> setWalkinshawMatcher() {
        return setMatcher((s, t, sc) -> new WalkinshawMatcher<>(sc, s, t));
    }

    /**
     * Set the {@link WalkinshawMatcher} as matcher.
     *
     * @param landmarkThreshold The landmark threshold value, i.e., the fraction of best scoring state pairs to consider
     *     as landmarks. That is, of all the possible pairs of (LHS, RHS)-states, only the top so-many scoring pairs are
     *     considered. For example, 0.25 means the top 25%. This threshold can be tweaked a bit if the state matchings
     *     appear too arbitrary, but should stay within the interval [0,1]. A threshold of 0 would mean that no
     *     landmarks will be picked. A threshold of 1.0 would would mean that all state combinations are potential
     *     landmarks. Any value lower than 0.1 or higher than 0.5 will likely give undesired results.
     * @param landmarkRatio The landmark ratio, indicating the ratio that a candidate landmark should be better than
     *     another one, to be considered. That is, if during state matching, there are multiple (conflicting) candidate
     *     matches to be considered, continue with the highest of these candidate matches, but only if it is
     *     significantly better than any other candidate, where the significance is determined by this ratio. This
     *     factor can be tweaked a bit if the matching results turn out unsatisfactory, or if there happen to be many
     *     conflicting matches. In such a scenario, lowering this ratio might help. It does not make sense to have it
     *     lower than 1.0.
     * @return This builder, for chaining.
     */
    public StructureComparatorBuilder<S, T, U> setWalkinshawMatcher(double landmarkThreshold, double landmarkRatio) {
        return setMatcher((s, t, sc) -> new WalkinshawMatcher<>(sc, s, t, landmarkThreshold, landmarkRatio));
    }

    /**
     * Set the merger.
     *
     * @param mergerProvider The merger provider that creates a merger, given a state property combiner, a transition
     *     property combiner and a GLTS instantiator.
     * @return This builder, for chaining.
     */
    public StructureComparatorBuilder<S, T, U>
            setMerger(TriFunction<Combiner<S>, Combiner<T>, Supplier<U>, Merger<S, T, U>> mergerProvider)
    {
        Preconditions.checkNotNull(mergerProvider, "Expected a non-null merger provider.");
        this.mergerProvider = mergerProvider;
        return this;
    }

    /**
     * Set the {@link DefaultMerger} as merger.
     *
     * @return This builder, for chaining.
     */
    public StructureComparatorBuilder<S, T, U> setDefaultMerger() {
        return setMerger((s, t, i) -> new DefaultMerger<>(s, t, i));
    }

    /**
     * Set the transition property inclusion.
     *
     * @param inclusion The transition property inclusion operator.
     * @return This builder, for chaining.
     */
    public StructureComparatorBuilder<S, T, U> setInclusionOperator(Inclusion<T> inclusion) {
        Preconditions.checkNotNull(inclusion, "Expected a non-null transition property inclusion operator.");
        this.inclusion = inclusion;
        return this;
    }

    /**
     * Set the {@link EqualToCombinationInclusion} operator as transition property inclusion operator.
     *
     * @return This builder, for chaining.
     */
    public StructureComparatorBuilder<S, T, U> setEqualToCombinationInclusionOperator() {
        return setInclusionOperator(new EqualToCombinationInclusion<>());
    }

    /**
     * Set the transition property hider.
     *
     * @param hiderProvider The hider provider that creates a hider.
     * @return This builder, for chaining.
     */
    public StructureComparatorBuilder<S, T, U> setTransitionPropertyHider(Supplier<Hider<T>> hiderProvider) {
        Preconditions.checkNotNull(hiderProvider, "Expected a non-null transition property hider provider.");
        this.hiderProvider = hiderProvider;
        return this;
    }

    /**
     * Set the transition property hider.
     *
     * @param hider The hider.
     * @return This builder, for chaining.
     */
    public StructureComparatorBuilder<S, T, U> setTransitionPropertyHider(Hider<T> hider) {
        return setTransitionPropertyHider(() -> hider);
    }

    /**
     * Set an exception-throwing transition property hider as hider.
     *
     * @return This builder, for chaining.
     */
    protected StructureComparatorBuilder<S, T, U> setThrowingTransitionPropertyHider() {
        return setTransitionPropertyHider(() -> {
            throw new IllegalStateException("The transition property hider is not yet configured. "
                    + "Configure it before invoking any of the builder's creation methods.");
        });
    }

    /**
     * Set the rewriters.
     *
     * @param rewriterProviders The rewriter providers that creates rewriters, given a state property combiner, a
     *     transition property combiner, a transition property inclusion operator and a transition property hider
     *     provider.
     * @return This builder, for chaining.
     */
    public StructureComparatorBuilder<S, T, U> setRewriters(
            Collection<QuadFunction<Combiner<S>, Combiner<T>, Inclusion<T>, Supplier<Hider<T>>, Rewriter<S, T, U>>> rewriterProviders)
    {
        Preconditions.checkNotNull(rewriterProviders, "Expected a non-null collection of rewriter providers.");
        Preconditions.checkArgument(rewriterProviders.stream().allMatch(p -> p != null),
                "Expected a collection of non-null rewriter providers.");
        this.rewriterProviders = new ArrayList<>(rewriterProviders);
        return this;
    }

    /**
     * Add a rewriters.
     *
     * @param rewriterProvider The rewriter provider that creates a rewriter, given a state property combiner, a
     *     transition property combiner, a transition property inclusion operator and a transition property hider
     *     provider.
     * @return This builder, for chaining.
     */
    public StructureComparatorBuilder<S, T, U> addRewriter(
            QuadFunction<Combiner<S>, Combiner<T>, Inclusion<T>, Supplier<Hider<T>>, Rewriter<S, T, U>> rewriterProvider)
    {
        Preconditions.checkNotNull(rewriterProvider, "Expected a non-null rewriter provider.");
        if (this.rewriterProviders == null) {
            this.rewriterProviders = new ArrayList<>();
        }
        this.rewriterProviders.add(rewriterProvider);
        return this;
    }

    /**
     * Add default rewriters.
     *
     * @return This builder, for chaining.
     */
    public StructureComparatorBuilder<S, T, U> addDefaultRewriters() {
        return addLocalRedundancyRewriter();
    }

    /**
     * Add the {@link LocalRedundancyRewriter} as rewriter.
     *
     * @return This builder, for chaining.
     */
    public StructureComparatorBuilder<S, T, U> addLocalRedundancyRewriter() {
        return addRewriter((s, t, i, hp) -> new LocalRedundancyRewriter<>(t));
    }

    /**
     * Set the state label HTML printer.
     *
     * @param stateLabelPrinter The state label HTML printer.
     * @return This builder, for chaining.
     */
    public StructureComparatorBuilder<S, T, U> setStateLabelHtmlPrinter(HtmlPrinter<State<S>> stateLabelPrinter) {
        Preconditions.checkNotNull(stateLabelPrinter, "Expected a non-null state label HTML printer.");
        this.stateLabelPrinter = stateLabelPrinter;
        return this;
    }

    /**
     * Set {@link StateHtmlPrinter} as state label HTML printer.
     *
     * @return This builder, for chaining.
     */
    public StructureComparatorBuilder<S, T, U> setDefaultStateLabelHtmlPrinter() {
        return setStateLabelHtmlPrinter(new StateHtmlPrinter<>());
    }

    /**
     * Set the transition label HTML printer.
     *
     * @param transitionLabelPrinter The transition label HTML printer.
     * @return This builder, for chaining.
     */
    public StructureComparatorBuilder<S, T, U>
            setTransitionLabelHtmlPrinter(HtmlPrinter<Transition<S, T>> transitionLabelPrinter)
    {
        Preconditions.checkNotNull(transitionLabelPrinter, "Expected a non-null transition label HTML printer.");
        this.transitionLabelPrinter = transitionLabelPrinter;
        return this;
    }

    /**
     * Set the transition label HTML printer, based on a printer for transition properties.
     *
     * @param transitionPropertyPrinter The transition property HTML printer.
     * @return This builder, for chaining.
     */
    public StructureComparatorBuilder<S, T, U>
            setTransitionPropertyHtmlPrinter(HtmlPrinter<T> transitionPropertyPrinter)
    {
        return setTransitionLabelHtmlPrinter(new TransitionHtmlPrinter<>(transitionPropertyPrinter));
    }

    /**
     * Set {@link StringHtmlPrinter} as transition label HTML printer.
     *
     * @return This builder, for chaining.
     */
    public StructureComparatorBuilder<S, T, U> setDefaultTransitionLabelHtmlPrinter() {
        return setTransitionPropertyHtmlPrinter(new StringHtmlPrinter<>());
    }

    /**
     * Set the DOT writer.
     *
     * @param writerProvider The DOT writer provider that creates a DOT writer, given a state property HTML printer and
     *     a transition property HTML printer.
     *
     * @return This builder, for chaining.
     */
    public StructureComparatorBuilder<S, T, U> setDotWriter(
            BiFunction<HtmlPrinter<State<S>>, HtmlPrinter<Transition<S, T>>, DotWriter<S, T, U>> writerProvider)
    {
        Preconditions.checkNotNull(writerProvider, "Expected a non-null writer provider.");
        this.writerProvider = writerProvider;
        return this;
    }

    /**
     * Set the default {@link DotWriter} as DOT writer.
     *
     * @return This builder, for chaining.
     */
    public StructureComparatorBuilder<S, T, U> setDefaultDotWriter() {
        return setDotWriter((sp, tp) -> new DotWriter<>(sp, tp));
    }

    /**
     * Get the configured GLTS instantiator.
     *
     * @return The GLTS instantiator.
     */
    protected Supplier<U> getInstantiator() {
        if (instantiator == null) {
            throw new IllegalStateException("The instantiator is not yet configured. "
                    + "Configure it before invoking any of the builder's creation methods.");
        }
        return instantiator;
    }

    /**
     * Get the configured state property combiner.
     *
     * @return The state property combiner.
     */
    protected Combiner<S> getStatePropertyCombiner() {
        if (statePropertyCombiner == null) {
            throw new IllegalStateException("The state property combiner is not yet configured. "
                    + "Configure it before invoking any of the builder's creation methods.");
        }
        return statePropertyCombiner;
    }

    /**
     * Get the configured transition property combiner.
     *
     * @return The transition property combiner.
     */
    protected Combiner<T> getTransitionPropertyCombiner() {
        if (transitionPropertyCombiner == null) {
            throw new IllegalStateException("The transition property combiner is not yet configured. "
                    + "Configure it before invoking any of the builder's creation methods.");
        }
        return transitionPropertyCombiner;
    }

    /**
     * Get the configured similarity scorer provider.
     *
     * @return The similarity scorer provider.
     */
    protected BiFunction<Combiner<S>, Combiner<T>, SimilarityScorer<S, T, U>> getScorerProvider() {
        if (scorerProvider == null) {
            throw new IllegalStateException("The similarity scorer is not yet configured. "
                    + "Configure it before invoking any of the builder's creation methods.");
        }
        return scorerProvider;
    }

    /**
     * Get the configured matcher provider.
     *
     * @return The matcher provider.
     */
    protected TriFunction<Combiner<S>, Combiner<T>, SimilarityScorer<S, T, U>, Matcher<S, T, U>> getMatcherProvider() {
        if (matcherProvider == null) {
            throw new IllegalStateException("The matcher is not yet configured. "
                    + "Configure it before invoking any of the builder's creation methods.");
        }
        return matcherProvider;
    }

    /**
     * Get the configured merger provider.
     *
     * @return The merger provider.
     */
    protected TriFunction<Combiner<S>, Combiner<T>, Supplier<U>, Merger<S, T, U>> getMergerProvider() {
        if (mergerProvider == null) {
            throw new IllegalStateException("The merger is not yet configured. "
                    + "Configure it before invoking any of the builder's creation methods.");
        }
        return mergerProvider;
    }

    /**
     * Get the configured transition property inclusion operator.
     *
     * @return The transition property inclusion operator.
     */
    protected Inclusion<T> getInclusion() {
        if (inclusion == null) {
            throw new IllegalStateException("The transition property inclusion operator is not yet configured. "
                    + "Configure it before invoking any of the builder's creation methods.");
        }
        return inclusion;
    }

    /**
     * Get the configured transition property hider provider.
     *
     * @return The transition property inclusion operator.
     */
    protected Supplier<Hider<T>> getTransitionPropertyHiderProvider() {
        if (hiderProvider == null) {
            throw new IllegalStateException("The transition property hider is not yet configured. "
                    + "Configure it before invoking any of the builder's creation methods.");
        }
        return hiderProvider;
    }

    /**
     * Get the configured state label printer.
     *
     * @return The state label printer.
     */
    protected HtmlPrinter<State<S>> getStateLabelPrinter() {
        if (stateLabelPrinter == null) {
            throw new IllegalStateException("The state label printer is not yet configured. "
                    + "Configure it before invoking any of the builder's creation methods.");
        }
        return stateLabelPrinter;
    }

    /**
     * Get the configured transition label printer.
     *
     * @return The transition label printer.
     */
    protected HtmlPrinter<Transition<S, T>> getTransitionLabelPrinter() {
        if (transitionLabelPrinter == null) {
            throw new IllegalStateException("The transition label printer is not yet configured. "
                    + "Configure it before invoking any of the builder's creation methods.");
        }
        return transitionLabelPrinter;
    }

    /**
     * Get the configured rewriter providers.
     *
     * @return The rewriter providers.
     */
    protected List<QuadFunction<Combiner<S>, Combiner<T>, Inclusion<T>, Supplier<Hider<T>>, Rewriter<S, T, U>>>
            getRewriterProviders()
    {
        return (rewriterProviders == null) ? Collections.emptyList() : rewriterProviders;
    }

    /**
     * Get the configured DOT writer provider.
     *
     * @return The DOT writer provider.
     */
    protected BiFunction<HtmlPrinter<State<S>>, HtmlPrinter<Transition<S, T>>, DotWriter<S, T, U>> getWriterProvider() {
        if (writerProvider == null) {
            throw new IllegalStateException("The DOT writer is not yet configured. "
                    + "Configure it before invoking any of the builder's creation methods.");
        }
        return writerProvider;
    }

    /**
     * Create a similarity scorer for the current configuration.
     *
     * @return The similarity scorer.
     */
    private SimilarityScorer<S, T, U> createScorer() {
        return getScorerProvider().apply(getStatePropertyCombiner(), getTransitionPropertyCombiner());
    }

    /**
     * Create a matcher for the current configuration.
     *
     * @return The matcher.
     */
    private Matcher<S, T, U> createMatcher() {
        return getMatcherProvider().apply(getStatePropertyCombiner(), getTransitionPropertyCombiner(), createScorer());
    }

    /**
     * Create a merger for the current configuration.
     *
     * @return The merger.
     */
    private Merger<S, T, U> createMerger() {
        return getMergerProvider().apply(getStatePropertyCombiner(), getTransitionPropertyCombiner(),
                getInstantiator());
    }

    /**
     * Create a rewriter for the current configuration.
     *
     * @return The rewriter.
     */
    private Rewriter<S, T, U> createRewriter() {
        // Create rewriters.
        List<Rewriter<S, T, U>> rewriters = getRewriterProviders().stream().map(p -> p.apply(getStatePropertyCombiner(),
                getTransitionPropertyCombiner(), getInclusion(), getTransitionPropertyHiderProvider()))
                .collect(Collectors.toList());

        // Repeatedly applies the rewriters in sequence, until they no longer change the GLTS.
        return new FixedPointRewriter<>(new SequenceRewriter<>(rewriters));
    }

    /**
     * Create a structure comparator for the current configuration.
     *
     * @return The structure comparator.
     */
    public StructureComparator<S, T, U> createComparator() {
        return new StructureComparator<>(createMatcher(), createMerger(), createRewriter());
    }

    /**
     * Create a writer for the current configuration.
     *
     * @return The writer.
     */
    public DotWriter<S, T, U> createWriter() {
        return getWriterProvider().apply(getStateLabelPrinter(), getTransitionLabelPrinter());
    }
}
