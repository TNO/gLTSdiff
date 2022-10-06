//////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2021-2022 Contributors to the GitHub community
//
// This program and the accompanying materials are made available
// under the terms of the MIT License which is available at
// https://opensource.org/licenses/MIT
//
// SPDX-License-Identifier: MIT
//////////////////////////////////////////////////////////////////////////////

package com.github.tno.gltsdiff.rewriters;

import java.util.Collection;
import java.util.Set;

import com.github.tno.gltsdiff.lts.LTS;
import com.github.tno.gltsdiff.lts.State;
import com.github.tno.gltsdiff.lts.Transition;
import com.github.tno.gltsdiff.operators.combiners.Combiner;
import com.github.tno.gltsdiff.operators.combiners.TransitionCombiner;
import com.github.tno.gltsdiff.utils.EquivalenceClasses;

/**
 * Eliminates patterns of local redundancy in LTSs.
 * <p>
 * A <i>pattern of local redundancy</i> is defined to be a set of at least two transitions with combinable properties,
 * that all share the same source state and target state. This rewriter merges any such set of transitions into a single
 * combined transition.
 * </p>
 *
 * @param <S> The type of state properties.
 * @param <T> The type of transition properties.
 * @param <U> The type of LTSs to rewrite.
 */
public class LocalRedundancyRewriter<S, T, U extends LTS<S, T>> implements Rewriter<S, T, U> {
    /** The combiner for transitions. */
    private final Combiner<Transition<S, T>> combiner;

    /**
     * Instantiates a new rewriter for eliminating local redundancy.
     * 
     * @param combiner The combiner for transition properties.
     */
    public LocalRedundancyRewriter(Combiner<T> combiner) {
        this.combiner = new TransitionCombiner<>(combiner);
    }

    @Override
    public boolean rewrite(U lts) {
        boolean effective = false;

        for (State<S> state: lts.getStates()) {
            // Partition all outgoing transitions of 'state' into equivalence classes of combinable transitions.
            Collection<Set<Transition<S, T>>> equivClasses = EquivalenceClasses.split(lts.getOutgoingTransitions(state),
                    (left, right) -> combiner.areCombinable(left, right));

            // Rewrite all patterns of local redundancy consisting of at least two transitions into a single transition.
            for (Set<Transition<S, T>> redundancyPattern: equivClasses) {
                if (redundancyPattern.size() >= 2) {
                    redundancyPattern.forEach(lts::removeTransition);
                    lts.addTransition(combiner.combine(redundancyPattern));
                    effective = true;
                }
            }
        }

        return effective;
    }
}
