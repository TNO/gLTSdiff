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

import com.github.tno.gltsdiff.glts.GLTS;

/**
 * A scorer, i.e., a scoring algorithm for computing similarity scores for every pair of (LHS, RHS)-states, indicating
 * how similar their surrounding structure is.
 *
 * @param <S> The type of state properties.
 * @param <T> The type of transition properties.
 * @param <U> The type of GLTSs.
 */
public interface SimilarityScorer<S, T, U extends GLTS<S, T>> {
    /**
     * Computes a matrix of (LHS, RHS)-state similarity scores. The rows correspond to LHS states, columns to RHS
     * states, and cells to a score that expresses how similar the (LHS, RHS)-state pair is.
     *
     * <p>
     * Similarity scores are allowed to be any finite double, or {@link Double#NEGATIVE_INFINITY} to indicate that the
     * corresponding state pair is truly incompatible and should not be merged. Moreover, similarity scores are intended
     * to be monotone: the higher the score, the higher the degree of similarity.
     * </p>
     *
     * @param lhs The left-hand-side (LHS) GLTS.
     * @param rhs The right-hand-side (RHS) GLTS.
     * @return The computed matrix of similarity scores for every pair of (LHS, RHS)-states.
     */
    public RealMatrix compute(U lhs, U rhs);
}
