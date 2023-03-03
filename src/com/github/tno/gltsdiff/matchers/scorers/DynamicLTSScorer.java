//////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2021-2023 Contributors to the GitHub community
//
// This program and the accompanying materials are made available
// under the terms of the MIT License which is available at
// https://opensource.org/licenses/MIT
//
// SPDX-License-Identifier: MIT
//////////////////////////////////////////////////////////////////////////////

package com.github.tno.gltsdiff.matchers.scorers;

import java.util.function.BiFunction;

import com.github.tno.gltsdiff.glts.LTS;
import com.github.tno.gltsdiff.operators.combiners.Combiner;

/**
 * Scorer that computes state similarity scores for {@link LTS LTSs} that makes a trade-off between computational
 * intensity and the quality of the computed scores.
 *
 * @param <S> The type of state properties.
 * @param <T> The type of transition properties.
 * @param <U> The type of LTSs.
 */
public class DynamicLTSScorer<S, T, U extends LTS<S, T>> extends DynamicGLTSScorer<S, T, U> {
    /**
     * Instantiates a new dynamic scoring algorithm for LTSs, that uses a default configuration of scoring algorithms.
     *
     * @param statePropertyCombiner The combiner for state properties.
     * @param transitionPropertyCombiner The combiner for transition properties.
     */
    public DynamicLTSScorer(Combiner<S> statePropertyCombiner, Combiner<T> transitionPropertyCombiner) {
        this(statePropertyCombiner, transitionPropertyCombiner, (s, t) -> defaultScoringAlgorithmCreator(s, t));
    }

    /**
     * Instantiates a new dynamic scoring algorithm for LTSs.
     *
     * @param statePropertyCombiner The combiner for state properties.
     * @param transitionPropertyCombiner The combiner for transition properties.
     * @param scoringAlgorithmCreator The scoring algorithm creator. Given the input LTSs and appropriate combiners,
     *     creates a suitable algorithm.
     */
    public DynamicLTSScorer(Combiner<S> statePropertyCombiner, Combiner<T> transitionPropertyCombiner,
            BiFunction<Combiner<S>, Combiner<T>, SimilarityScorer<S, T, U>> scoringAlgorithmCreator)
    {
        super(statePropertyCombiner, transitionPropertyCombiner, scoringAlgorithmCreator);
    }

    /**
     * Returns the default scorer.
     *
     * @param <S> The type of state properties.
     * @param <T> The type of transition properties.
     * @param <U> The type of LTSs.
     * @param statePropertyCombiner The state property combiner.
     * @param transitionPropertyCombiner The transition property combiner.
     * @return The scorer.
     */
    private static final <S, T, U extends LTS<S, T>> SimilarityScorer<S, T, U>
            defaultScoringAlgorithmCreator(Combiner<S> statePropertyCombiner, Combiner<T> transitionPropertyCombiner)
    {
        SimilarityScorer<S, T, U> globalScorer = new WalkinshawGlobalLTSScorer<>(statePropertyCombiner,
                transitionPropertyCombiner);
        SimilarityScorer<S, T, U> local5Scorer = new WalkinshawLocalLTSScorer<>(statePropertyCombiner,
                transitionPropertyCombiner, 5);
        SimilarityScorer<S, T, U> local1Scorer = new WalkinshawLocalLTSScorer<>(statePropertyCombiner,
                transitionPropertyCombiner, 1);
        return (lhs, rhs) -> {
            int nrOfStates = Math.max(lhs.size(), rhs.size());

            if (nrOfStates <= 45) {
                return globalScorer.compute(lhs, rhs);
            } else if (nrOfStates <= 500) {
                return local5Scorer.compute(lhs, rhs);
            } else {
                return local1Scorer.compute(lhs, rhs);
            }
        };
    }
}
