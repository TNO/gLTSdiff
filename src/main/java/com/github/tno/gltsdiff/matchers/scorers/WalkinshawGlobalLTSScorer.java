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
 * Scorer that computes global similarity scores between {@link LTS LTSs}, by transforming the problem of finding global
 * similarity scores to a problem of solving a system of linear equations, as proposed by Walkinshaw et al. Takes
 * initial state information into account.
 *
 * @param <S> The type of state properties.
 * @param <T> The type of transition properties.
 * @param <U> The type of LTSs.
 */
public class WalkinshawGlobalLTSScorer<S, T, U extends LTS<S, T>> extends WalkinshawGlobalGLTSScorer<S, T, U> {
    /**
     * Instantiates a new Walkinshaw global scorer for LTSs. Uses an attenuation factor of 0.6.
     *
     * @param statePropertyCombiner The combiner for state properties.
     * @param transitionPropertyCombiner The combiner for transition properties.
     */
    public WalkinshawGlobalLTSScorer(Combiner<S> statePropertyCombiner, Combiner<T> transitionPropertyCombiner) {
        this(statePropertyCombiner, transitionPropertyCombiner, 0.6d);
    }

    /**
     * Instantiates a new Walkinshaw global scorer for LTSs.
     *
     * @param statePropertyCombiner The combiner for state properties.
     * @param transitionPropertyCombiner The combiner for transition properties.
     * @param attenuationFactor The attenuation factor, the ratio in the range [0,1] that determines how much the
     *     similarity scores of far-away states influence the final similarity scores. This factor can be tweaked a bit
     *     if the comparison results come out unsatisfactory. A ratio of 0 would mean that only local similarity scores
     *     are used. Note that, if one is only interested in local similarity, {@link WalkinshawLocalGLTSScorer} should
     *     be used instead, which gives the same result but is much faster. A ratio of 1 would mean that far-away state
     *     similarities contribute equally much as local ones.
     */
    public WalkinshawGlobalLTSScorer(Combiner<S> statePropertyCombiner, Combiner<T> transitionPropertyCombiner,
            double attenuationFactor)
    {
        super(statePropertyCombiner, transitionPropertyCombiner, attenuationFactor);
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
