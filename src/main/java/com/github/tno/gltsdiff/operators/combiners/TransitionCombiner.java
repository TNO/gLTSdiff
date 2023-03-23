//////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2021-2023 Contributors to the GitHub community
//
// This program and the accompanying materials are made available
// under the terms of the MIT License which is available at
// https://opensource.org/licenses/MIT
//
// SPDX-License-Identifier: MIT
//////////////////////////////////////////////////////////////////////////////

package com.github.tno.gltsdiff.operators.combiners;

import com.github.tno.gltsdiff.glts.Transition;

/**
 * A combiner for {@link Transition transitions}.
 *
 * <p>
 * Any two transitions are combinable if their source and target states are equal and their properties are combinable.
 * Combining two transitions results in a transition with a combined property.
 * </p>
 *
 * @param <S> The type of state properties.
 * @param <T> The type of transition properties.
 */
public class TransitionCombiner<S, T> extends Combiner<Transition<S, T>> {
    /** The combiner for transition properties. */
    private final Combiner<T> transitionPropertyCombiner;

    /**
     * Instantiates a new transition combiner.
     *
     * @param transitionPropertyCombiner The combiner for transition properties.
     */
    public TransitionCombiner(Combiner<T> transitionPropertyCombiner) {
        this.transitionPropertyCombiner = transitionPropertyCombiner;
    }

    @Override
    protected boolean computeAreCombinable(Transition<S, T> left, Transition<S, T> right) {
        return left.getSource() == right.getSource()
                && transitionPropertyCombiner.areCombinable(left.getProperty(), right.getProperty())
                && left.getTarget() == right.getTarget();
    }

    @Override
    protected Transition<S, T> computeCombination(Transition<S, T> left, Transition<S, T> right) {
        return new Transition<>(left.getSource(),
                transitionPropertyCombiner.combine(left.getProperty(), right.getProperty()), left.getTarget());
    }
}
