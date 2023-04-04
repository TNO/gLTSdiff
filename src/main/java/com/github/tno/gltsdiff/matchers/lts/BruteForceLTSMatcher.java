//////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2021-2023 Contributors to the GitHub community
//
// This program and the accompanying materials are made available
// under the terms of the MIT License which is available at
// https://opensource.org/licenses/MIT
//
// SPDX-License-Identifier: MIT
//////////////////////////////////////////////////////////////////////////////

package com.github.tno.gltsdiff.matchers.lts;

import com.github.tno.gltsdiff.glts.State;
import com.github.tno.gltsdiff.glts.lts.LTS;
import com.github.tno.gltsdiff.glts.lts.LTSStateProperty;
import com.github.tno.gltsdiff.matchers.BruteForceMatcher;
import com.github.tno.gltsdiff.operators.combiners.Combiner;

/**
 * A brute force matching algorithm for {@link LTS LTSs} that calculates a best possible maximal (LHS, RHS)-state
 * matching, thereby taking initial state information into account.
 *
 * @param <S> The type of LTS state properties.
 * @param <T> The type of transition properties.
 * @param <U> The type of LTSs.
 */
public class BruteForceLTSMatcher<S extends LTSStateProperty, T, U extends LTS<S, T>>
        extends BruteForceMatcher<S, T, U>
{
    /**
     * Instantiates a new brute force matcher for LTSs.
     *
     * @param statePropertyCombiner The combiner for state properties.
     * @param transitionPropertyCombiner The combiner for transition properties.
     */
    public BruteForceLTSMatcher(Combiner<S> statePropertyCombiner, Combiner<T> transitionPropertyCombiner) {
        super(statePropertyCombiner, transitionPropertyCombiner);
    }

    @Override
    protected int getOptimizationObjectiveAdjustment(U lhs, U rhs, State<S> leftState, State<S> rightState) {
        int adjustment = super.getOptimizationObjectiveAdjustment(lhs, rhs, leftState, rightState);

        // Account for combinable initial state arrows.
        if (leftState.getProperty().isInitial() && rightState.getProperty().isInitial()) {
            adjustment += 1;
        }

        return adjustment;
    }
}
