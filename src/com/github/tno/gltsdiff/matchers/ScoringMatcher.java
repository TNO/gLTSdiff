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

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

import com.github.tno.gltsdiff.lts.LTS;
import com.github.tno.gltsdiff.lts.State;
import com.github.tno.gltsdiff.matchers.scorers.SimilarityScorer;
import com.google.common.base.Preconditions;

/**
 * A matcher that computes a (graph theoretical) matching between the states of the LHS and RHS, based on scores that
 * are computed by a {@link SimilarityScorer state similarity scoring algorithm}.
 *
 * @param <S> The type of state properties.
 * @param <T> The type of transition properties.
 * @param <U> The type of LTSs.
 */
public abstract class ScoringMatcher<S, T, U extends LTS<S, T>> implements Matcher<S, T, U> {
    /** The algorithm for computing state similarity scores. */
    private final SimilarityScorer<S, T, U> scoring;

    /**
     * Instantiates a new similarity scoring based matcher.
     * 
     * @param scoring The algorithm for computing state similarity scores.
     */
    public ScoringMatcher(SimilarityScorer<S, T, U> scoring) {
        this.scoring = scoring;
    }

    @Override
    public Map<State<S>, State<S>> compute() throws IllegalArgumentException {
        RealMatrix scores = scoring.compute();
        Preconditions.checkArgument(scores.getRowDimension() == getLhs().size());
        Preconditions.checkArgument(scores.getColumnDimension() == getRhs().size());
        return computeInternal(normalize(scores));
    }

    /**
     * Normalizes the given matrix of similarity scores, so that every matrix cell is within the interval [0,1].
     * 
     * @param scores The matrix of state similarity scores that is to be normalized.
     * @return The normalized scoring matrix.
     */
    protected RealMatrix normalize(RealMatrix scores) {
        // Empty matrices are trivially normalized.
        if (scores.getRowDimension() == 0 || scores.getColumnDimension() == 0) {
            return scores;
        }

        // Find the lowest and highest score in 'scores'.
        double lowest = scores.getEntry(0, 0);
        double highest = lowest;

        for (int row = 0; row < scores.getRowDimension(); row++) {
            for (int column = 0; column < scores.getColumnDimension(); column++) {
                double score = scores.getEntry(row, column);
                lowest = Math.min(lowest, score);
                highest = Math.max(highest, score);
            }
        }

        // The above loop guarantees that 'lowest <= highest'.

        // Return 'scores' if all its scores already are within [0,1].
        if (0.0d <= lowest && highest <= 1.0d) {
            return scores;
        }

        // If not, then new scores will have to be calculated.
        RealMatrix normalizedScores = new Array2DRowRealMatrix(scores.getRowDimension(), scores.getColumnDimension());

        // Calculate all normalized scores and store them in 'normalizedScores'.
        for (int row = 0; row < scores.getRowDimension(); row++) {
            for (int column = 0; column < scores.getColumnDimension(); column++) {
                double score = scores.getEntry(row, column);
                double newScore = lowest == highest ? 1.0d : (score - lowest) / (highest - lowest);
                normalizedScores.setEntry(row, column, newScore);
            }
        }

        return normalizedScores;
    }

    /**
     * Given a matrix {@code scores} containing similarity scores for (LHS, RHS)-state pairs, computes which LHS states
     * should be matched onto which RHS states. The computed matching comes as a mapping from LHS states to RHS states.
     * 
     * @param scores A matrix of (LHS, RHS)-state similarity scores. The rows correspond to LHS states, columns to RHS
     *     states, and cells to a score within the range [0,1] that expresses how similar the (LHS, RHS)-state pair is.
     *     The state similarity scores are intended to be monotone: the higher the score, the higher the degree of
     *     similarity.
     * @return A matching from LHS to RHS states that satisfies the following properties:
     *     <ul>
     *     <li>All states in the key set of the returned matching are LHS states.</li>
     *     <li>All states in the value set of the returned matching are RHS states.</li>
     *     <li>All state matchings are disjoint: there is no state that is involved in more than one matching.</li>
     *     <li>All matched states have combinable properties.</li>
     *     </ul>
     */
    protected abstract Map<State<S>, State<S>> computeInternal(RealMatrix scores);
}
