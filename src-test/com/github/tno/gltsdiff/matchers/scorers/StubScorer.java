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

import org.apache.commons.math3.linear.RealMatrix;

import com.github.tno.gltsdiff.glts.LTS;
import com.google.common.base.Preconditions;

/**
 * A stub similarity scorer, that simply returns predefined similarity scores.
 *
 * @param <S> The type of state properties.
 * @param <T> The type of transition properties.
 * @param <U> The type of LTSs.
 */
public class StubScorer<S, T, U extends LTS<S, T>> implements SimilarityScorer<S, T, U> {
    /** The left-hand-side LTS. */
    private final U lhs;

    /** The right-hand-side LTS. */
    private final U rhs;

    /** The predefined matrix of similarity scores. */
    private final RealMatrix scores;

    /**
     * Instantiates a new stub similarity scorer.
     *
     * @param lhs The left-hand-side LTS.
     * @param rhs The right-hand-side LTS.
     * @param scores The predefined matrix of similarity scores.
     */
    public StubScorer(U lhs, U rhs, RealMatrix scores) {
        Preconditions.checkArgument(scores.getRowDimension() == lhs.size(),
                "Expected the score matrix to contain a row for every LHS state.");
        Preconditions.checkArgument(scores.getColumnDimension() == rhs.size(),
                "Expected the score matrix to contain a column for every RHS state.");

        this.lhs = lhs;
        this.rhs = rhs;
        this.scores = scores;
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
        return scores;
    }
}
