//////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2021-2023 Contributors to the GitHub community
//
// This program and the accompanying materials are made available
// under the terms of the MIT License which is available at
// https://opensource.org/licenses/MIT
//
// SPDX-License-Identifier: MIT
//////////////////////////////////////////////////////////////////////////////

package com.github.tno.gltsdiff.matchers;

import java.util.Map;
import java.util.function.BiFunction;

import com.github.tno.gltsdiff.glts.GLTS;
import com.github.tno.gltsdiff.glts.State;
import com.github.tno.gltsdiff.matchers.scorers.DynamicGLTSScorer;
import com.github.tno.gltsdiff.matchers.scorers.SimilarityScorer;
import com.github.tno.gltsdiff.operators.combiners.Combiner;

/**
 * Matcher that computes state matchings for {@link GLTS GLTSs}, making a trade-off between computational intensity and
 * the quality of the computed matchings.
 *
 * <p>
 * Different matching algorithms can be used for different input GLTSs, e.g. based on their sizes (numbers of states)
 * from "heavyweight" (for smaller GLTSs) to "lightweight" (for larger GLTSs).
 * </p>
 *
 * @param <S> The type of state properties.
 * @param <T> The type of transition properties.
 * @param <U> The type of GLTSs.
 */
public class DynamicGLTSMatcher<S, T, U extends GLTS<S, T>> implements Matcher<S, T, U> {
    /** The matching algorithm to use. */
    private final Matcher<S, T, U> matcher;

    /**
     * Instantiates a new dynamic matcher for GLTSs, that uses a default configuration of matching algorithms.
     *
     * @param statePropertyCombiner The combiner for state properties.
     * @param transitionPropertyCombiner The combiner for transition properties.
     */
    public DynamicGLTSMatcher(Combiner<S> statePropertyCombiner, Combiner<T> transitionPropertyCombiner) {
        this(statePropertyCombiner, transitionPropertyCombiner, (s, t) -> defaultMatchingAlgorithmCreator(s, t));
    }

    /**
     * Instantiates a new dynamic matcher for GLTSs.
     *
     * @param statePropertyCombiner The combiner for state properties.
     * @param transitionPropertyCombiner The combiner for transition properties.
     * @param matchingAlgorithmCreator The matching algorithm creator. Given appropriate combiners, creates a suitable
     *     algorithm.
     */
    public DynamicGLTSMatcher(Combiner<S> statePropertyCombiner, Combiner<T> transitionPropertyCombiner,
            BiFunction<Combiner<S>, Combiner<T>, Matcher<S, T, U>> matchingAlgorithmCreator)
    {
        this.matcher = matchingAlgorithmCreator.apply(statePropertyCombiner, transitionPropertyCombiner);
    }

    /**
     * Returns the default matcher.
     *
     * @param <S> The type of state properties.
     * @param <T> The type of transition properties.
     * @param <U> The type of GLTSs.
     * @param statePropertyCombiner The state property combiner.
     * @param transitionPropertyCombiner The transition property combiner.
     * @return The matcher.
     */
    private static final <S, T, U extends GLTS<S, T>> Matcher<S, T, U>
            defaultMatchingAlgorithmCreator(Combiner<S> statePropertyCombiner, Combiner<T> transitionPropertyCombiner)
    {
        SimilarityScorer<S, T, U> scorer = new DynamicGLTSScorer<>(statePropertyCombiner, transitionPropertyCombiner);
        Matcher<S, T, U> walkinshawMatcher = new WalkinshawGLTSMatcher<>(scorer, statePropertyCombiner,
                transitionPropertyCombiner);
        Matcher<S, T, U> kuhnMunkresMatcher = new KuhnMunkresMatcher<>(scorer, statePropertyCombiner);
        return (lhs, rhs) -> {
            if (lhs.size() > 45 || rhs.size() > 45) {
                return walkinshawMatcher.compute(lhs, rhs);
            } else {
                return kuhnMunkresMatcher.compute(lhs, rhs);
            }
        };
    }

    @Override
    public Map<State<S>, State<S>> compute(U lhs, U rhs) {
        return matcher.compute(lhs, rhs);
    }
}
