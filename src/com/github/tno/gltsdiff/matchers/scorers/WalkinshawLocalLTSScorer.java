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
     * Instantiates a new Walkinshaw local similarity scorer for LTSs, that performs only a single refinement. Uses an
     * attenuation factor of 0.6.
     *
     * @param statePropertyCombiner The combiner for state properties.
     * @param transitionPropertyCombiner The combiner for transition properties.
     */
    public WalkinshawLocalLTSScorer(Combiner<S> statePropertyCombiner, Combiner<T> transitionPropertyCombiner) {
        this(statePropertyCombiner, transitionPropertyCombiner, 1, 0.6d);
    }

    /**
     * Instantiates a new Walkinshaw local similarity scorer for LTSs. Uses an attenuation factor of 0.6.
     *
     * @param statePropertyCombiner The combiner for state properties.
     * @param transitionPropertyCombiner The combiner for transition properties.
     * @param nrOfRefinements The number of refinements to perform, which must be at least 1.
     */
    public WalkinshawLocalLTSScorer(Combiner<S> statePropertyCombiner, Combiner<T> transitionPropertyCombiner,
            int nrOfRefinements)
    {
        this(statePropertyCombiner, transitionPropertyCombiner, nrOfRefinements, 0.6d);
    }

    /**
     * Instantiates a new Walkinshaw local similarity scorer for LTSs. Uses an attenuation factor of 0.6.
     *
     * @param statePropertyCombiner The combiner for state properties.
     * @param transitionPropertyCombiner The combiner for transition properties.
     * @param nrOfRefinements The number of refinements to perform, which must be at least 1.
     * @param attenuationFactor The attenuation factor, the ratio in the range [0,1] that determines how much the
     *     similarity scores of far-away states influence the final similarity scores. This factor can be tweaked a bit
     *     if the comparison results come out unsatisfactory. A ratio of 0 would mean that only local similarity scores
     *     are used. Note that, if one is only interested in local similarity, {@link WalkinshawLocalGLTSScorer} should
     *     be used instead, which gives the same result but is much faster. A ratio of 1 would mean that far-away state
     *     similarities contribute equally much as local ones.
     */
    public WalkinshawLocalLTSScorer(Combiner<S> statePropertyCombiner, Combiner<T> transitionPropertyCombiner,
            int nrOfRefinements, double attenuationFactor)
    {
        super(statePropertyCombiner, transitionPropertyCombiner, nrOfRefinements, attenuationFactor);
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
