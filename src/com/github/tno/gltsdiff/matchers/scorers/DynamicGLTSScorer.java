//////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2021-2022 Contributors to the GitHub community
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
 * Scorer that computes state similarity scores that makes a trade-off between computational intensity and the quality
 * of the computed scores. Different scoring algorithms can be used for different input GLTSs, e.g. based on their sizes
 * (numbers of states) from "heavyweight" (for smaller GLTSs) to "lightweight" (for larger GLTSs).
 *
 * @param <S> The type of state properties.
 * @param <T> The type of transition properties.
 * @param <U> The type of GLTSs.
 */
public class DynamicScorer<S, T, U extends GLTS<S, T>> implements SimilarityScorer<S, T, U> {
    /** The left-hand-side GLTS, which has at least one state. */
    protected final U lhs;

    /** The right-hand-side GLTS, which has at least one state. */
    protected final U rhs;

    /** The combiner for state properties. */
    protected final Combiner<S> statePropertyCombiner;

    /** The combiner for transition properties. */
    protected final Combiner<T> transitionPropertyCombiner;

    /** The scoring algorithm creator. Given the input GLTSs and appropriate combiners, creates a suitable algorithm. */
    private final BiFunction<U, U, BiFunction<Combiner<S>, Combiner<T>, SimilarityScorer<S, T, U>>> scoringAlgorithmCreator;

    /**
     * Instantiates a new dynamic scoring algorithm, that uses a default configuration of scoring algorithms.
     * 
     * @param lhs The left-hand-side GLTS, which has at least one state.
     * @param rhs The right-hand-side GLTS, which has at least one state.
     * @param statePropertyCombiner The combiner for state properties.
     * @param transitionPropertyCombiner The combiner for transition properties.
     */
    public DynamicScorer(U lhs, U rhs, Combiner<S> statePropertyCombiner, Combiner<T> transitionPropertyCombiner) {
        this(lhs, rhs, statePropertyCombiner, transitionPropertyCombiner,
                (l, r) -> (s, t) -> defaultScoringAlgorithmCreator(l, r, s, t));
    }

    /**
     * Instantiates a new dynamic scoring algorithm.
     * 
     * @param lhs The left-hand-side GLTS, which has at least one state.
     * @param rhs The right-hand-side GLTS, which has at least one state.
     * @param statePropertyCombiner The combiner for state properties.
     * @param transitionPropertyCombiner The combiner for transition properties.
     * @param scoringAlgorithmCreator The scoring algorithm creator. Given the input GLTSs and appropriate combiners,
     *     creates a suitable algorithm.
     */
    public DynamicScorer(U lhs, U rhs, Combiner<S> statePropertyCombiner, Combiner<T> transitionPropertyCombiner,
            BiFunction<U, U, BiFunction<Combiner<S>, Combiner<T>, SimilarityScorer<S, T, U>>> scoringAlgorithmCreator)
    {
        this.lhs = lhs;
        this.rhs = rhs;
        this.statePropertyCombiner = statePropertyCombiner;
        this.transitionPropertyCombiner = transitionPropertyCombiner;
        this.scoringAlgorithmCreator = scoringAlgorithmCreator;
    }

    @Override
    public U getLhs() {
        return lhs;
    }

    @Override
    public U getRhs() {
        return rhs;
    }

    private static final <S, T, U extends GLTS<S, T>> SimilarityScorer<S, T, U> defaultScoringAlgorithmCreator(U lhs,
            U rhs, Combiner<S> statePropertyCombiner, Combiner<T> transitionPropertyCombiner)
    {
        int nrOfStates = Math.max(lhs.size(), rhs.size());

        if (nrOfStates <= 45) {
            return new WalkinshawGlobalGLTSScorer<>(lhs, rhs, statePropertyCombiner, transitionPropertyCombiner);
        } else if (nrOfStates <= 500) {
            return new WalkinshawLocalGLTSScorer<>(lhs, rhs, statePropertyCombiner, transitionPropertyCombiner, 5);
        } else {
            return new WalkinshawLocalGLTSScorer<>(lhs, rhs, statePropertyCombiner, transitionPropertyCombiner, 1);
        }
    }

    @Override
    public RealMatrix compute() {
        SimilarityScorer<S, T, U> algorithm = scoringAlgorithmCreator.apply(lhs, rhs).apply(statePropertyCombiner,
                transitionPropertyCombiner);
        return algorithm.compute();
    }
}
