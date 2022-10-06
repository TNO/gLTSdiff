//////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2021-2022 Contributors to the GitHub community
//
// This program and the accompanying materials are made available
// under the terms of the MIT License which is available at
// https://opensource.org/licenses/MIT
//
// SPDX-License-Identifier: MIT
//////////////////////////////////////////////////////////////////////////////

package com.github.tno.gltsdiff.mergers;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.github.tno.gltsdiff.lts.LTS;
import com.github.tno.gltsdiff.lts.State;
import com.github.tno.gltsdiff.operators.combiners.Combiner;
import com.google.common.base.Preconditions;

/**
 * An abstract LTS merger that contains common validation logic for input matchings.
 * 
 * @param <S> The type of state properties.
 * @param <T> The type of transition properties.
 * @param <U> The type of LTSs to merge.
 */
public abstract class AbstractMerger<S, T, U extends LTS<S, T>> implements Merger<S, T, U> {
    /** The combiner for state properties. */
    private final Combiner<S> statePropertyCombiner;

    /**
     * Instantiates an abstract LTS merger.
     * 
     * @param statePropertyCombiner The combiner for state properties.
     */
    public AbstractMerger(Combiner<S> statePropertyCombiner) {
        this.statePropertyCombiner = statePropertyCombiner;
    }

    /**
     * Checks whether the given matching is proper.
     * 
     * @param matching The matching that is to be validated.
     * @throws IllegalArgumentException In case the matching is found to be improper.
     */
    protected void checkPreconditions(Map<State<S>, State<S>> matching) throws IllegalArgumentException {
        Set<State<S>> leftStates = new LinkedHashSet<>();
        Set<State<S>> rightStates = new LinkedHashSet<>();

        for (Entry<State<S>, State<S>> assignment: matching.entrySet()) {
            State<S> leftState = assignment.getKey();
            State<S> rightState = assignment.getValue();

            Preconditions.checkArgument(getLhs().getStates().contains(leftState), "All keys must be LHS states.");
            Preconditions.checkArgument(getRhs().getStates().contains(rightState), "All values must be RHS states.");
            Preconditions.checkArgument(!leftStates.contains(leftState), "All keys must be disjoint.");
            Preconditions.checkArgument(!rightStates.contains(rightState), "All values must be disjoint.");
            Preconditions.checkArgument(
                    statePropertyCombiner.areCombinable(leftState.getProperty(), rightState.getProperty()),
                    "Expected all matched states to have combinable properties.");

            leftStates.add(leftState);
            rightStates.add(rightState);
        }
    }

    @Override
    public U merge(Map<State<S>, State<S>> matching) throws IllegalArgumentException {
        checkPreconditions(matching);
        return mergeInternal(matching);
    }

    /**
     * Merges the LHS and RHS into a single LTS. The given (LHS, RHS)-state matching determines which LHS states are to
     * be merged with which RHS states.
     * 
     * @param matching A matching from LHS states to RHS states. This matching should be proper in the sense that:
     *     <ul>
     *     <li>All keys are states in the LHS.</li>
     *     <li>All values are states in the RHS.</li>
     *     <li>All mappings are disjoint: no LHS or RHS state is part of more than one match.</li>
     *     <li>All matched states must have combinable state properties.</li>
     *     </ul>
     * @return The LTS that is the merge of the LHS and the RHS.
     */
    protected abstract U mergeInternal(Map<State<S>, State<S>> matching);
}
