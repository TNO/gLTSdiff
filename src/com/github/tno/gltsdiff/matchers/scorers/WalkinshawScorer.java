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

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

import com.github.tno.gltsdiff.lts.LTS;
import com.github.tno.gltsdiff.operators.combiners.Combiner;
import com.google.common.base.Preconditions;

/**
 * Contains common functionality for the state similarity scoring approaches that are described in the article by
 * Walkinshaw et al. (TOSEM 2014). However, this implementation generalizes the approaches described in the article by a
 * more general concept of combinability (see {@link Combiner}).
 *
 * @param <S> The type of state properties.
 * @param <T> The type of transition properties.
 * @param <U> The type of LTSs.
 */
public abstract class WalkinshawScorer<S, T, U extends LTS<S, T>> implements SimilarityScorer<S, T, U> {
    /**
     * This is the ratio in the range [0,1] that determines how much the similarity scores of far-away states influence
     * the final similarity scores.
     * <p>
     * A ratio of 0 would mean that only local similarity scores are used. Note that, if one is only interested in local
     * similarity, {@link WalkinshawLocalScorer} should be used instead, which gives the same result but is much cheaper
     * in terms of computation. A ratio of 1 would mean that far-away state similarities contribute equally much as
     * local ones.
     * </p>
     * <p>
     * This factor can be tweaked a bit if the comparison results come out unsatisfactory.
     * </p>
     */
    protected final double attenuationFactor = 0.6d;

    /** The left-hand-side LTS, which has at least one state. */
    protected final U lhs;

    /** The right-hand-side LTS, which has at least one state. */
    protected final U rhs;

    /** The combiner for state properties. */
    protected final Combiner<S> statePropertyCombiner;

    /** The combiner for transition properties. */
    protected final Combiner<T> transitionPropertyCombiner;

    /**
     * Instantiates a new Walkinshaw similarity scorer.
     * 
     * @param lhs The left-hand-side LTS, which has at least one state.
     * @param rhs The right-hand-side LTS, which has at least one state.
     * @param statePropertyCombiner The combiner for state properties.
     * @param transitionPropertyCombiner The combiner for transition properties.
     */
    public WalkinshawScorer(U lhs, U rhs, Combiner<S> statePropertyCombiner, Combiner<T> transitionPropertyCombiner) {
        Preconditions.checkArgument(lhs.size() > 0, "Expected the LHS to have at least one state.");
        Preconditions.checkArgument(rhs.size() > 0, "Expected the RHS to have at least one state.");

        this.lhs = lhs;
        this.rhs = rhs;
        this.statePropertyCombiner = statePropertyCombiner;
        this.transitionPropertyCombiner = transitionPropertyCombiner;
    }

    @Override
    public U getLhs() {
        return lhs;
    }

    @Override
    public U getRhs() {
        return rhs;
    }

    @Override
    public RealMatrix compute() {
        // Calculate forward and backward similarity scores.
        RealMatrix forwardScores = computeForwardSimilarityScores();
        RealMatrix backwardScores = computeBackwardSimilarityScores();

        // Ensure that the scoring matrices have the right dimensions.
        Preconditions.checkArgument(forwardScores.getRowDimension() == lhs.size(),
                "Expected the number of rows in the forward score matrix to equal the number of LHS states.");
        Preconditions.checkArgument(forwardScores.getColumnDimension() == rhs.size(),
                "Expected the number of columns in the forward score matrix to equal the number of RHS states.");
        Preconditions.checkArgument(backwardScores.getRowDimension() == lhs.size(),
                "Expected the number of rows in the backward score matrix to equal the number of LHS states.");
        Preconditions.checkArgument(backwardScores.getColumnDimension() == rhs.size(),
                "Expected the number of columns in the backward score matrix to equal the number of RHS states.");

        // Calculate average similarity scores out of the forward and backward scores.
        // Note: here 'BlockRealMatrix' is used since 'OpenMapRealMatrix' does not deal well with infinite doubles.
        RealMatrix averageScores = new BlockRealMatrix(lhs.size(), rhs.size());

        for (int leftIndex = 0; leftIndex < lhs.size(); leftIndex++) {
            for (int rightIndex = 0; rightIndex < rhs.size(); rightIndex++) {
                double forwardScore = forwardScores.getEntry(leftIndex, rightIndex);
                double backwardScore = backwardScores.getEntry(leftIndex, rightIndex);

                // Walkinshaw et al. guarantee that the computed state similarity scores are within the range [-1,1].
                // (see the small paragraph right after Equation 6 on page 14 in their TOSEM 2014 article.)
                Preconditions.checkArgument(-1 <= forwardScore && forwardScore <= 1,
                        "Expected all forward state similarity scores to be within the range [-1,1].");
                Preconditions.checkArgument(-1 <= backwardScore && backwardScore <= 1,
                        "Expected all backward state similarity scores to be within the range [-1,1].");

                // Any negative score indicates an incompatible state pair. This is not explicit in the TOSEM 2014
                // article, but it is (to some extend) in the implementation of StateChum
                // (e.g., see statechum.analysis.learning.linear.GDLearnerGraph, line 498).
                // Therefore any such score is marked as minus infinity.
                if (forwardScore < 0.0d || backwardScore < 0.0d) {
                    averageScores.setEntry(leftIndex, rightIndex, Double.NEGATIVE_INFINITY);
                } else {
                    averageScores.setEntry(leftIndex, rightIndex, (forwardScore + backwardScore) / 2.0d);
                }
            }
        }

        // At this point, all computed average scores are either negative infinity in case the corresponding state pair
        // turned out to be incompatible, or otherwise guaranteed to be within the range [0,1].
        return averageScores;
    }

    /**
     * @return The computed {@code lhs.size()} times {@code rhs.size()} matrix of forward state similarity scores, all
     *     of which are in the range [-1,1], where any negative score indicates an incompatible state pair.
     */
    protected abstract RealMatrix computeForwardSimilarityScores();

    /**
     * @return The computed {@code lhs.size()} times {@code rhs.size()} matrix of backward state similarity scores, all
     *     of which are in the range [-1,1], where any negative score indicates an incompatible state pair.
     */
    protected abstract RealMatrix computeBackwardSimilarityScores();

    /**
     * Computes the set of all transition properties of {@code left} that cannot be combined with any property from
     * {@code right}.
     * 
     * @param left The set of transition properties to filter.
     * @param right The set of transition properties from which the filtering criteria is determined.
     * @return All properties of {@code left} minus the ones that can be combined with a property from {@code right}.
     */
    protected Set<T> uncombinableTransitionProperties(Set<T> left, Set<T> right) {
        return left.stream().filter(l -> right.stream().noneMatch(r -> transitionPropertyCombiner.areCombinable(l, r)))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
