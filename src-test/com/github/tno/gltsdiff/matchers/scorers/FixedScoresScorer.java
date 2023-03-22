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
 * A similarity scorer that simply returns predefined similarity scores.
 *
 * @param <S> The type of state properties.
 * @param <T> The type of transition properties.
 * @param <U> The type of LTSs.
 */
public class FixedScoresScorer<S, T, U extends LTS<S, T>> implements SimilarityScorer<S, T, U> {
    /** The fixed matrix of similarity scores. */
    private final RealMatrix scores;

    /**
     * Instantiates a new stub similarity scorer.
     *
     * @param scores The fixed matrix of similarity scores.
     */
    public FixedScoresScorer(RealMatrix scores) {
        this.scores = scores;
    }

    @Override
    public RealMatrix compute(U lhs, U rhs) {
        Preconditions.checkArgument(scores.getRowDimension() == lhs.size(),
                "Mismatch between the number of score matrix rows and the number of LHS states.");
        Preconditions.checkArgument(scores.getColumnDimension() == rhs.size(),
                "Mismatch between the number of score matrix columns and the number of RLHS states.");

        return scores;
    }
}