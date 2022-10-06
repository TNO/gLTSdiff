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
import java.util.function.Function;

import org.apache.commons.math3.linear.RealMatrix;

import com.github.tno.gltsdiff.lts.LTS;
import com.github.tno.gltsdiff.operators.combiners.Combiner;

/**
 * Contains functionality for computing state similarity scores that makes a trade-off between computational intensity
 * and the quality of the computed scores. Different scoring algorithms can be used for different input LTSs, e.g. based
 * on their sizes (numbers of states) from "heavyweight" (for smaller LTSs) to "lightweight" (for larger LTSs).
 *
 * @param <S> The type of state properties.
 * @param <T> The type of transition properties.
 * @param <U> The type of LTSs.
 */
public class DynamicScorer<S, T, U extends LTS<S, T>> implements SimilarityScorer<S, T, U> {
    /** The left-hand-side LTS. */
    protected final U lhs;

    /** The right-hand-side LTS. */
    protected final U rhs;

    /** The combiner for transition properties. */
    protected final Combiner<T> transitionPropertyCombiner;

    /** The scoring algorithm creator. Given the input LTSs and a combiner, creates a suitable algorithm. */
    private final BiFunction<U, U, Function<Combiner<T>, SimilarityScorer<S, T, U>>> scoringAlgorithmCreator;

    /**
     * Instantiates a new dynamic scoring algorithm, that uses a default configuration of scoring algorithms.
     * 
     * @param lhs The left-hand-side LTS.
     * @param rhs The right-hand-side LTS.
     * @param transitionPropertyCombiner The combiner for transition properties.
     */
    public DynamicScorer(U lhs, U rhs, Combiner<T> transitionPropertyCombiner) {
        this(lhs, rhs, transitionPropertyCombiner, (l, r) -> c -> defaultScoringAlgorithmCreator(l, r, c));
    }

    /**
     * Instantiates a new dynamic scoring algorithm.
     * 
     * @param lhs The left-hand-side LTS.
     * @param rhs The right-hand-side LTS.
     * @param transitionPropertyCombiner The combiner for transition properties.
     * @param scoringAlgorithmCreator The scoring algorithm creator. Given the input LTSs and a combiner, creates a
     *     suitable algorithm.
     */
    public DynamicScorer(U lhs, U rhs, Combiner<T> transitionPropertyCombiner,
            BiFunction<U, U, Function<Combiner<T>, SimilarityScorer<S, T, U>>> scoringAlgorithmCreator)
    {
        this.lhs = lhs;
        this.rhs = rhs;
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

    private static final <S, T, U extends LTS<S, T>> SimilarityScorer<S, T, U> defaultScoringAlgorithmCreator(U lhs,
            U rhs, Combiner<T> transitionPropertyCombiner)
    {
        int nrOfStates = Math.max(lhs.size(), rhs.size());

        if (nrOfStates <= 45) {
            return new WalkinshawGlobalScorer<>(lhs, rhs, transitionPropertyCombiner);
        } else if (nrOfStates <= 500) {
            return new WalkinshawLocalScorer<>(lhs, rhs, transitionPropertyCombiner, 5);
        } else {
            return new WalkinshawLocalScorer<>(lhs, rhs, transitionPropertyCombiner, 1);
        }
    }

    @Override
    public RealMatrix compute() {
        SimilarityScorer<S, T, U> algorithm = scoringAlgorithmCreator.apply(lhs, rhs).apply(transitionPropertyCombiner);
        return algorithm.compute();
    }
}
