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

import java.util.Map;
import java.util.function.BiFunction;

import com.github.tno.gltsdiff.glts.LTS;
import com.github.tno.gltsdiff.glts.State;
import com.github.tno.gltsdiff.matchers.scorers.DynamicScorer;
import com.github.tno.gltsdiff.matchers.scorers.SimilarityScorer;
import com.github.tno.gltsdiff.operators.combiners.Combiner;

/**
 * Contains functionality for computing state matchings that makes a trade-off between computational intensity and the
 * quality of the computed matchings. Different matching algorithms can be used for different input LTSs, e.g. based on
 * their sizes (numbers of states) from "heavyweight" (for smaller LTSs) to "lightweight" (for larger LTSs).
 *
 * @param <S> The type of state properties.
 * @param <T> The type of transition properties.
 * @param <U> The type of LTSs.
 */
public class DynamicMatcher<S, T, U extends LTS<S, T>> implements Matcher<S, T, U> {
    /** The left-hand-side LTS. */
    private final U lhs;

    /** The right-hand-side LTS. */
    private final U rhs;

    /** The combiner for state properties. */
    protected final Combiner<S> statePropertyCombiner;

    /** The combiner for transition properties. */
    protected final Combiner<T> transitionPropertyCombiner;

    /** The matching algorithm creator. Given the input LTSs and appropriate combiners, creates a suitable algorithm. */
    private final BiFunction<U, U, BiFunction<Combiner<S>, Combiner<T>, Matcher<S, T, U>>> matchingAlgorithmCreator;

    /**
     * Instantiates a new dynamic matching algorithm, that uses a default configuration of matching algorithms.
     * 
     * @param lhs The left-hand-side LTS.
     * @param rhs The right-hand-side LTS.
     * @param statePropertyCombiner The combiner for state properties.
     * @param transitionPropertyCombiner The combiner for transition properties.
     */
    public DynamicMatcher(U lhs, U rhs, Combiner<S> statePropertyCombiner, Combiner<T> transitionPropertyCombiner) {
        this(lhs, rhs, statePropertyCombiner, transitionPropertyCombiner,
                (l, r) -> (s, t) -> defaultMatchingAlgorithmCreator(l, r, s, t));
    }

    /**
     * Instantiates a new dynamic matching algorithm.
     * 
     * @param lhs The left-hand-side LTS.
     * @param rhs The right-hand-side LTS.
     * @param statePropertyCombiner The combiner for state properties.
     * @param transitionPropertyCombiner The combiner for transition properties.
     * @param matchingAlgorithmCreator The matching algorithm creator. Given the input LTSs and appropriate combiners,
     *     creates a suitable algorithm.
     */
    public DynamicMatcher(U lhs, U rhs, Combiner<S> statePropertyCombiner, Combiner<T> transitionPropertyCombiner,
            BiFunction<U, U, BiFunction<Combiner<S>, Combiner<T>, Matcher<S, T, U>>> matchingAlgorithmCreator)
    {
        this.lhs = lhs;
        this.rhs = rhs;
        this.statePropertyCombiner = statePropertyCombiner;
        this.transitionPropertyCombiner = transitionPropertyCombiner;
        this.matchingAlgorithmCreator = matchingAlgorithmCreator;
    }

    @Override
    public U getLhs() {
        return lhs;
    }

    @Override
    public U getRhs() {
        return rhs;
    }

    private static final <S, T, U extends LTS<S, T>> Matcher<S, T, U> defaultMatchingAlgorithmCreator(U lhs, U rhs,
            Combiner<S> statePropertyCombiner, Combiner<T> transitionPropertyCombiner)
    {
        SimilarityScorer<S, T, U> scorer = new DynamicScorer<>(lhs, rhs, statePropertyCombiner,
                transitionPropertyCombiner);

        if (lhs.size() > 45 || rhs.size() > 45) {
            return new WalkinshawLTSMatcher<>(lhs, rhs, scorer, statePropertyCombiner, transitionPropertyCombiner);
        } else {
            return new KuhnMunkresMatcher<>(lhs, rhs, scorer, statePropertyCombiner);
        }
    }

    @Override
    public Map<State<S>, State<S>> compute() {
        Matcher<S, T, U> algorithm = matchingAlgorithmCreator.apply(lhs, rhs).apply(statePropertyCombiner,
                transitionPropertyCombiner);
        return algorithm.compute();
    }
}
