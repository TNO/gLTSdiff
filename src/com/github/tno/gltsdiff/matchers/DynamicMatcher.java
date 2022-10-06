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

import com.github.tno.gltsdiff.lts.LTS;
import com.github.tno.gltsdiff.lts.State;
import com.github.tno.gltsdiff.matchers.scorers.SimilarityScorer;
import com.github.tno.gltsdiff.operators.combiners.Combiner;

/**
 * Contains functionality for computing state matchings that makes a trade-off between computational complexity and the
 * quality of the results. A heavyweight matcher is used on relatively small input LTSs, whereas a lightweight matcher
 * is used on large LTSs.
 *
 * @param <S> The type of state properties.
 * @param <T> The type of transition properties.
 * @param <U> The type of LTSs.
 */
public class DynamicMatcher<S, T, U extends LTS<S, T>> implements Matcher<S, T, U> {
    /** The left-hand-side LTS. */
    private final U lhs;

    /** The right-hand-side LTS. */
    private final U rhs;

    /** Matcher to compute state matchings for input LTSs larger than {@link #threshold}. */
    private final Matcher<S, T, U> lightweightMatcher;

    /** Matcher to compute state matchings for input LTSs smaller than or equal to {@link #threshold}. */
    private final Matcher<S, T, U> heavyweightMatcher;

    /**
     * Threshold on the number of states that are handled by the heavyweight matcher. Any input LTS with more states is
     * handled by the lightweight matcher.
     */
    private final int threshold;

    /**
     * Instantiates a new (LHS, RHS)-state matcher that dynamically determines whether to use a lightweight
     * {@link WalkinshawMatcher} or a heavyweight {@link KuhnMunkresMatcher} algorithm for computing state matchings,
     * based on the sizes of the two given input LTSs compared to a threshold of 45.
     * 
     * @param lhs The left-hand-side LTS.
     * @param rhs The right-hand-side LTS.
     * @param scoring The similarity scoring algorithm to be used by the lightweight and heavyweight matchers.
     * @param statePropertyCombiner The combiner for state properties.
     * @param transitionPropertyCombiner The combiner for transition properties.
     */
    public DynamicMatcher(U lhs, U rhs, SimilarityScorer<S, T, U> scoring, Combiner<S> statePropertyCombiner,
            Combiner<T> transitionPropertyCombiner)
    {
        this(lhs, rhs, new WalkinshawMatcher<>(lhs, rhs, scoring, statePropertyCombiner, transitionPropertyCombiner),
                new KuhnMunkresMatcher<>(lhs, rhs, scoring, statePropertyCombiner), 45);
    }

    /**
     * Instantiates a new dynamic matcher.
     * 
     * @param lhs The left-hand-side LTS.
     * @param rhs The right-hand-side LTS.
     * @param lightweightMatcher Matcher to compute state matchings for input LTSs larger than {@code threshold}.
     * @param heavyweightMatcher Matcher to compute state matchings for input LTSs smaller than or equal to
     *     {@code threshold}.
     * @param threshold Threshold on LTS size used to select matcher.
     */
    public DynamicMatcher(U lhs, U rhs, Matcher<S, T, U> lightweightMatcher, Matcher<S, T, U> heavyweightMatcher,
            int threshold)
    {
        this.lhs = lhs;
        this.rhs = rhs;
        this.lightweightMatcher = lightweightMatcher;
        this.heavyweightMatcher = heavyweightMatcher;
        this.threshold = threshold;
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
    public Map<State<S>, State<S>> compute() {
        if (getLhs().size() > threshold || getRhs().size() > threshold) {
            return lightweightMatcher.compute();
        } else {
            return heavyweightMatcher.compute();
        }
    }
}
