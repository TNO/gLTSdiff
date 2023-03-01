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

import com.github.tno.gltsdiff.glts.LTS;
import com.github.tno.gltsdiff.glts.State;
import com.github.tno.gltsdiff.operators.combiners.Combiner;

/**
 * Scorer that computes local similarity scores for pairs of (LHS, RHS)-states in {@link LTS LTSs}, thereby taking
 * initial state information into account.
 *
 * @param <S> The type of state properties.
 * @param <T> The type of transition properties.
 * @param <U> The type of LTSs.
 */
public class WalkinshawLocalLTSScorer<S, T, U extends LTS<S, T>> extends WalkinshawLocalGLTSScorer<S, T, U> {
    /**
     * Instantiates a new Walkinshaw local similarity scorer for LTSs, that performs only a single refinement.
     * 
     * @param statePropertyCombiner The combiner for state properties.
     * @param transitionPropertyCombiner The combiner for transition properties.
     */
    public WalkinshawLocalLTSScorer(Combiner<S> statePropertyCombiner, Combiner<T> transitionPropertyCombiner) {
        super(statePropertyCombiner, transitionPropertyCombiner);
    }

    /**
     * Instantiates a new Walkinshaw local similarity scorer for LTSs.
     * 
     * @param statePropertyCombiner The combiner for state properties.
     * @param transitionPropertyCombiner The combiner for transition properties.
     * @param nrOfRefinements The number of refinements to perform, which must be at least 1.
     */
    public WalkinshawLocalLTSScorer(Combiner<S> statePropertyCombiner, Combiner<T> transitionPropertyCombiner,
            int nrOfRefinements)
    {
        super(statePropertyCombiner, transitionPropertyCombiner, nrOfRefinements);
    }

    @Override
    protected double getNumeratorAdjustment(U lhs, U rhs, State<S> leftState, State<S> rightState, boolean isForward) {
        double adjustment = super.getNumeratorAdjustment(lhs, rhs, leftState, rightState, isForward);

        // Adjust the numerator if backward scores are computed and 'leftState' and 'rightState' are both initial.
        if (!isForward && lhs.isInitialState(leftState) && rhs.isInitialState(rightState)) {
            adjustment += 1d;
        }

        return adjustment;
    }

    @Override
    protected double getDenominatorAdjustment(U lhs, U rhs, State<S> leftState, State<S> rightState,
            boolean isForward)
    {
        double adjustment = super.getDenominatorAdjustment(lhs, rhs, leftState, rightState, isForward);

        // Adjust the denominator if backward scores are computed and 'leftState' and/or 'rightState' is initial.
        if (!isForward && (lhs.isInitialState(leftState) || rhs.isInitialState(rightState))) {
            adjustment += 1d;
        }

        return adjustment;
    }
}
