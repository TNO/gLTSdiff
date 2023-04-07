//////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2021-2023 Contributors to the GitHub community
//
// This program and the accompanying materials are made available
// under the terms of the MIT License which is available at
// https://opensource.org/licenses/MIT
//
// SPDX-License-Identifier: MIT
//////////////////////////////////////////////////////////////////////////////

package com.github.tno.gltsdiff.matchers.lts;

import java.util.function.BiFunction;

import com.github.tno.gltsdiff.glts.lts.BaseLTS;
import com.github.tno.gltsdiff.glts.lts.LTSStateProperty;
import com.github.tno.gltsdiff.matchers.DynamicMatcher;
import com.github.tno.gltsdiff.matchers.KuhnMunkresMatcher;
import com.github.tno.gltsdiff.matchers.Matcher;
import com.github.tno.gltsdiff.operators.combiners.Combiner;
import com.github.tno.gltsdiff.scorers.DynamicScorer;
import com.github.tno.gltsdiff.scorers.SimilarityScorer;
import com.github.tno.gltsdiff.scorers.lts.DynamicLTSScorer;

/**
 * Matcher that computes state matchings for {@link BaseLTS LTSs}, that makes a trade-off between computational
 * intensity and the quality of the computed matchings.
 *
 * @param <S> The type of LTS state properties.
 * @param <T> The type of transition properties.
 * @param <U> The type of LTSs.
 */
public class DynamicLTSMatcher<S extends LTSStateProperty, T, U extends BaseLTS<S, T>> extends DynamicMatcher<S, T, U> {
    /**
     * Instantiates a new dynamic matcher for LTSs, that uses a default configuration of matching algorithms, with a
     * {@link DynamicScorer dynamic scorer}.
     *
     * @param statePropertyCombiner The combiner for state properties.
     * @param transitionPropertyCombiner The combiner for transition properties.
     */
    public DynamicLTSMatcher(Combiner<S> statePropertyCombiner, Combiner<T> transitionPropertyCombiner) {
        this(statePropertyCombiner, transitionPropertyCombiner,
                new DynamicLTSScorer<>(statePropertyCombiner, transitionPropertyCombiner));
    }

    /**
     * Instantiates a new dynamic matcher for LTSs, that uses a default configuration of matching algorithms, with a
     * given scorer.
     *
     * @param statePropertyCombiner The combiner for state properties.
     * @param transitionPropertyCombiner The combiner for transition properties.
     * @param scorer The scorer, which must have been constructed with the same state and transition property combiners
     *     as provided to this dynamic matcher.
     */
    public DynamicLTSMatcher(Combiner<S> statePropertyCombiner, Combiner<T> transitionPropertyCombiner,
            SimilarityScorer<S, T, U> scorer)
    {
        this(statePropertyCombiner, transitionPropertyCombiner,
                (BiFunction<Combiner<S>, Combiner<T>, Matcher<S, T, U>>)(s, t) -> defaultMatchingAlgorithmCreator(s, t,
                        scorer));
    }

    /**
     * Instantiates a new dynamic matcher for LTSs.
     *
     * @param statePropertyCombiner The combiner for state properties.
     * @param transitionPropertyCombiner The combiner for transition properties.
     * @param matchingAlgorithmCreator The matching algorithm creator. Given the input LTSs and appropriate combiners,
     *     creates a suitable algorithm.
     */
    public DynamicLTSMatcher(Combiner<S> statePropertyCombiner, Combiner<T> transitionPropertyCombiner,
            BiFunction<Combiner<S>, Combiner<T>, Matcher<S, T, U>> matchingAlgorithmCreator)
    {
        super(statePropertyCombiner, transitionPropertyCombiner, matchingAlgorithmCreator);
    }

    /**
     * Returns the default matcher.
     *
     * @param <S> The type of LTS state properties.
     * @param <T> The type of transition properties.
     * @param <U> The type of LTSs.
     * @param statePropertyCombiner The state property combiner.
     * @param transitionPropertyCombiner The transition property combiner.
     * @param scorer The scorer.
     * @return The matcher.
     */
    private static final <S extends LTSStateProperty, T, U extends BaseLTS<S, T>> Matcher<S, T, U>
            defaultMatchingAlgorithmCreator(Combiner<S> statePropertyCombiner, Combiner<T> transitionPropertyCombiner,
                    SimilarityScorer<S, T, U> scorer)
    {
        Matcher<S, T, U> walkinshawMatcher = new WalkinshawLTSMatcher<>(scorer, statePropertyCombiner,
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
}
