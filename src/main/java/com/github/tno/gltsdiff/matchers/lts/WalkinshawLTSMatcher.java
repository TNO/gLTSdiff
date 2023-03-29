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

import java.util.Set;
import java.util.function.BiFunction;

import org.apache.commons.math3.util.Pair;

import com.github.tno.gltsdiff.glts.State;
import com.github.tno.gltsdiff.glts.lts.LTS;
import com.github.tno.gltsdiff.matchers.WalkinshawMatcher;
import com.github.tno.gltsdiff.operators.combiners.Combiner;
import com.github.tno.gltsdiff.scorers.SimilarityScorer;
import com.google.common.collect.ImmutableSet;

/**
 * Matcher for {@link LTS LTSs} based on landmarks, 'obviously' matching state pairs, as proposed by Walkinshaw et al,
 * thereby taking initial state information into account.
 *
 * @param <S> The type of state properties.
 * @param <T> The type of transition properties.
 * @param <U> The type of LTSs.
 */
public class WalkinshawLTSMatcher<S, T, U extends LTS<S, T>> extends WalkinshawMatcher<S, T, U> {
    /**
     * Instantiates a new Walkinshaw matcher for LTSs. Uses a landmark threshold of 0.25 and a landmark ratio of 1.5.
     *
     * @param scorer The algorithm for computing state similarity scores.
     * @param statePropertyCombiner The combiner for state properties.
     * @param transitionPropertyCombiner The combiner for transition properties.
     */
    public WalkinshawLTSMatcher(SimilarityScorer<S, T, U> scorer, Combiner<S> statePropertyCombiner,
            Combiner<T> transitionPropertyCombiner)
    {
        super(scorer, statePropertyCombiner, transitionPropertyCombiner);
    }

    /**
     * Instantiates a new Walkinshaw matcher for GLTSs.
     *
     * @param scorer The algorithm for computing state similarity scores.
     * @param statePropertyCombiner The combiner for state properties.
     * @param transitionPropertyCombiner The combiner for transition properties.
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
     */
    public WalkinshawLTSMatcher(SimilarityScorer<S, T, U> scorer, Combiner<S> statePropertyCombiner,
            Combiner<T> transitionPropertyCombiner, double landmarkThreshold, double landmarkRatio)
    {
        super(scorer, statePropertyCombiner, transitionPropertyCombiner, landmarkThreshold, landmarkRatio);
    }

    @Override
    protected Set<Pair<State<S>, State<S>>> getFallbackLandmarks(U lhs, U rhs,
            BiFunction<State<S>, State<S>, Double> scores)
    {
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

        return super.getFallbackLandmarks(lhs, rhs, scores);
    }
}
