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

import java.util.Set;
import java.util.function.BiFunction;

import org.apache.commons.math3.util.Pair;

import com.github.tno.gltsdiff.glts.LTS;
import com.github.tno.gltsdiff.glts.State;
import com.github.tno.gltsdiff.matchers.scorers.SimilarityScorer;
import com.github.tno.gltsdiff.operators.combiners.Combiner;
import com.google.common.collect.ImmutableSet;

/**
 * Functionality for computing (LHS, RHS)-state matchings for {@link LTS LTSs} based on heuristics proposed by
 * Walkinshaw et al. (TOSEM 2013; see Section 4.3.1), thereby taking initial state information into account.
 *
 * @param <S> The type of state properties.
 * @param <T> The type of transition properties.
 * @param <U> The type of LTSs.
 */
public class WalkinshawLTSMatcher<S, T, U extends LTS<S, T>> extends WalkinshawGLTSMatcher<S, T, U> {
    /**
     * Instantiates a new Walkinshaw matcher for LTSs.
     * 
     * @param lhs The left-hand-side LTS.
     * @param rhs The right-hand-side LTS.
     * @param scoring The algorithm for computing state similarity scores.
     * @param statePropertyCombiner The combiner for state properties.
     * @param transitionPropertyCombiner The combiner for transition properties.
     */
    public WalkinshawLTSMatcher(U lhs, U rhs, SimilarityScorer<S, T, U> scoring, Combiner<S> statePropertyCombiner,
            Combiner<T> transitionPropertyCombiner)
    {
        super(lhs, rhs, scoring, statePropertyCombiner, transitionPropertyCombiner);
    }

    @Override
    protected Set<Pair<State<S>, State<S>>> getFallbackLandmarks(BiFunction<State<S>, State<S>, Double> scores) {
        Pair<State<S>, State<S>> bestCurrentPair = null;

        // Iterate over all combinations of initial states, and keep track of the highest scoring combination.
        for (State<S> leftInitialState: lhs.getInitialStates()) {
            for (State<S> rightInitialState: rhs.getInitialStates()) {
                Pair<State<S>, State<S>> pair = Pair.create(leftInitialState, rightInitialState);

                if (!isCompatible(pair, scores)) {
                    continue;
                }

                if (bestCurrentPair == null) {
                    bestCurrentPair = pair;
                }

                if (scores.apply(pair.getFirst(), pair.getSecond()) > scores.apply(bestCurrentPair.getFirst(),
                        bestCurrentPair.getSecond()))
                {
                    bestCurrentPair = pair;
                }
            }
        }

        // If there is a highest scoring pair of compatible initial (LHS, RHS)-states, then that will be our landmark.
        if (bestCurrentPair != null) {
            return ImmutableSet.of(bestCurrentPair);
        }

        return super.getFallbackLandmarks(scores);
    }
}
