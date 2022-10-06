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

import org.apache.commons.math3.linear.RealMatrix;

import com.github.tno.gltsdiff.lts.LTS;

/**
 * A scoring algorithm for computing similarity scores for every pair of (LHS, RHS)-states, indicating how similar their
 * surrounding structure is.
 *
 * @param <S> The type of state properties.
 * @param <T> The type of transition properties.
 * @param <U> The type of LTSs.
 */
public interface SimilarityScorer<S, T, U extends LTS<S, T>> {
    /** @return The left-hand-side LTS. */
    public U getLhs();

    /** @return The right-hand-side LTS. */
    public U getRhs();

    /**
     * Computes a matrix of (LHS, RHS)-state similarity scores. The rows correspond to LHS states, columns to RHS
     * states, and cells to a score that expresses how similar the (LHS, RHS)-state pair is. The computed similarity
     * scores are allowed to be of any range (i.e., they do not necessarily have to be within the range [0,1]), as long
     * as they are monotone: the higher the score, the higher the degree of similarity.
     * 
     * @return The computed matrix of similarity scores for every pair of (LHS, RHS)-states.
     */
    public RealMatrix compute();
}
