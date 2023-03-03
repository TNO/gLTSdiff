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

import org.apache.commons.math3.linear.RealMatrix;

import com.github.tno.gltsdiff.glts.GLTS;
import com.github.tno.gltsdiff.operators.combiners.Combiner;

/**
 * Scorer that computes state similarity scores for {@link GLTS GLTSs} that makes a trade-off between computational
 * intensity and the quality of the computed scores.
 *
 * <p>
 * Different scoring algorithms can be used for different input GLTSs, e.g. based on their sizes (numbers of states)
 * from "heavyweight" (for smaller GLTSs) to "lightweight" (for larger GLTSs).
 * </p>
 *
 * @param <S> The type of state properties.
 * @param <T> The type of transition properties.
 * @param <U> The type of GLTSs.
 */
public class DynamicGLTSScorer<S, T, U extends GLTS<S, T>> implements SimilarityScorer<S, T, U> {
    /** The scoring algorithm. */
    private final SimilarityScorer<S, T, U> scorer;

    /**
     * Instantiates a new dynamic scoring algorithm for GLTSs, that uses a default configuration of scoring algorithms.
     *
     * @param statePropertyCombiner The combiner for state properties.
     * @param transitionPropertyCombiner The combiner for transition properties.
     */
    public DynamicGLTSScorer(Combiner<S> statePropertyCombiner, Combiner<T> transitionPropertyCombiner) {
        this(statePropertyCombiner, transitionPropertyCombiner, (s, t) -> defaultScoringAlgorithmCreator(s, t));
    }

    /**
     * Instantiates a new dynamic scoring algorithm for GLTSs.
     *
     * @param statePropertyCombiner The combiner for state properties.
     * @param transitionPropertyCombiner The combiner for transition properties.
     * @param scoringAlgorithmCreator The scoring algorithm creator. Given appropriate combiners, creates a suitable
     *     algorithm.
     */
    public DynamicGLTSScorer(Combiner<S> statePropertyCombiner, Combiner<T> transitionPropertyCombiner,
            BiFunction<Combiner<S>, Combiner<T>, SimilarityScorer<S, T, U>> scoringAlgorithmCreator)
    {
        this.scorer = scoringAlgorithmCreator.apply(statePropertyCombiner, transitionPropertyCombiner);
    }

    /**
     * Returns the default scorer.
     *
     * @param <S> The type of state properties.
     * @param <T> The type of transition properties.
     * @param <U> The type of GLTSs.
     * @param statePropertyCombiner The state property combiner.
     * @param transitionPropertyCombiner The transition property combiner.
     * @return The scorer.
     */
    private static final <S, T, U extends GLTS<S, T>> SimilarityScorer<S, T, U>
            defaultScoringAlgorithmCreator(Combiner<S> statePropertyCombiner, Combiner<T> transitionPropertyCombiner)
    {
        SimilarityScorer<S, T, U> globalScorer = new WalkinshawGlobalGLTSScorer<>(statePropertyCombiner,
                transitionPropertyCombiner);
        SimilarityScorer<S, T, U> local5Scorer = new WalkinshawLocalGLTSScorer<>(statePropertyCombiner,
                transitionPropertyCombiner, 5);
        SimilarityScorer<S, T, U> local1Scorer = new WalkinshawLocalGLTSScorer<>(statePropertyCombiner,
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

    @Override
    public RealMatrix compute(U lhs, U rhs) {
        return scorer.compute(lhs, rhs);
    }
}
