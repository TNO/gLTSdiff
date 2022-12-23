//////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2021-2022 Contributors to the GitHub community
//
// This program and the accompanying materials are made available
// under the terms of the MIT License which is available at
// https://opensource.org/licenses/MIT
//
// SPDX-License-Identifier: MIT
//////////////////////////////////////////////////////////////////////////////

package com.github.tno.gltsdiff.matchers;

import java.util.function.BiFunction;

import com.github.tno.gltsdiff.glts.LTS;
import com.github.tno.gltsdiff.matchers.scorers.DynamicLTSScorer;
import com.github.tno.gltsdiff.matchers.scorers.SimilarityScorer;
import com.github.tno.gltsdiff.operators.combiners.Combiner;

/**
 * Matcher that computes state matchings for {@link LTS LTSs}, that makes a trade-off between computational intensity and
 * the quality of the computed matchings.
 *
 * @param <S> The type of state properties.
 * @param <T> The type of transition properties.
 * @param <U> The type of LTSs.
 */
public class DynamicLTSMatcher<S, T, U extends LTS<S, T>> extends DynamicGLTSMatcher<S, T, U> {
    /**
     * Instantiates a new dynamic matcher for LTSs, that uses a default configuration of matching algorithms.
     * 
     * @param lhs The left-hand-side LTS.
     * @param rhs The right-hand-side LTS.
     * @param statePropertyCombiner The combiner for state properties.
     * @param transitionPropertyCombiner The combiner for transition properties.
     */
    public DynamicLTSMatcher(U lhs, U rhs, Combiner<S> statePropertyCombiner, Combiner<T> transitionPropertyCombiner) {
        this(lhs, rhs, statePropertyCombiner, transitionPropertyCombiner,
                (l, r) -> (s, t) -> defaultMatchingAlgorithmCreator(l, r, s, t));
    }

    /**
     * Instantiates a new dynamic matcher for LTSs.
     * 
     * @param lhs The left-hand-side LTS.
     * @param rhs The right-hand-side LTS.
     * @param statePropertyCombiner The combiner for state properties.
     * @param transitionPropertyCombiner The combiner for transition properties.
     * @param matchingAlgorithmCreator The matching algorithm creator. Given the input LTSs and appropriate combiners,
     *     creates a suitable algorithm.
     */
    public DynamicLTSMatcher(U lhs, U rhs, Combiner<S> statePropertyCombiner, Combiner<T> transitionPropertyCombiner,
            BiFunction<U, U, BiFunction<Combiner<S>, Combiner<T>, Matcher<S, T, U>>> matchingAlgorithmCreator)
    {
        super(lhs, rhs, statePropertyCombiner, transitionPropertyCombiner, matchingAlgorithmCreator);
    }

    private static final <S, T, U extends LTS<S, T>> Matcher<S, T, U> defaultMatchingAlgorithmCreator(U lhs, U rhs,
            Combiner<S> statePropertyCombiner, Combiner<T> transitionPropertyCombiner)
    {
        SimilarityScorer<S, T, U> scorer = new DynamicLTSScorer<>(lhs, rhs, statePropertyCombiner,
                transitionPropertyCombiner);

        if (lhs.size() > 45 || rhs.size() > 45) {
            return new WalkinshawLTSMatcher<>(lhs, rhs, scorer, statePropertyCombiner, transitionPropertyCombiner);
        } else {
            return new KuhnMunkresMatcher<>(lhs, rhs, scorer, statePropertyCombiner);
        }
    }
}
