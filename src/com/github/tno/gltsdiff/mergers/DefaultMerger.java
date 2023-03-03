//////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2021-2023 Contributors to the GitHub community
//
// This program and the accompanying materials are made available
// under the terms of the MIT License which is available at
// https://opensource.org/licenses/MIT
//
// SPDX-License-Identifier: MIT
//////////////////////////////////////////////////////////////////////////////

package com.github.tno.gltsdiff.mergers;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.github.tno.gltsdiff.glts.GLTS;
import com.github.tno.gltsdiff.glts.State;
import com.github.tno.gltsdiff.glts.Transition;
import com.github.tno.gltsdiff.operators.combiners.Combiner;
import com.github.tno.gltsdiff.operators.combiners.SetCombiner;
import com.github.tno.gltsdiff.operators.combiners.TransitionCombiner;
import com.google.common.base.Preconditions;

/**
 * A merger for merging two given GLTSs (the LHS and RHS) into a single GLTS. The merged GLTS is constructed by
 * combining state properties and transition properties using specified combiners.
 *
 * @param <S> The type of state properties.
 * @param <T> The type of transition properties.
 * @param <U> The type of GLTSs to merge.
 */
public class DefaultMerger<S, T, U extends GLTS<S, T>> extends AbstractMerger<S, T, U> {
    /** The combiner for state properties. */
    private final Combiner<S> statePropertyCombiner;

    /** The combiner for sets of transitions. */
    private final Combiner<Set<Transition<S, T>>> transitionCombiner;

    /** The supplier for instantiating new GLTSs. */
    private final Supplier<U> instantiator;

    /**
     * Instantiates a new default merger.
     *
     * @param statePropertyCombiner The combiner for state properties.
     * @param transitionPropertyCombiner The combiner for transition properties.
     * @param instantiator The supplier for instantiating new GLTSs.
     */
    public DefaultMerger(Combiner<S> statePropertyCombiner, Combiner<T> transitionPropertyCombiner,
            Supplier<U> instantiator)
    {
        super(statePropertyCombiner);
        this.statePropertyCombiner = statePropertyCombiner;
        this.transitionCombiner = new SetCombiner<>(new TransitionCombiner<>(transitionPropertyCombiner));
        this.instantiator = instantiator;
    }

    @Override
    protected U mergeInternal(U lhs, U rhs, Map<State<S>, State<S>> matching) {
        U diff = instantiator.get();

        // 1. Define all states of 'diff'.

        // Maps for maintaining how the states of LHS and RHS relate to the new states of 'diff'.
        Map<State<S>, State<S>> leftProjection = new LinkedHashMap<>();
        Map<State<S>, State<S>> rightProjection = new LinkedHashMap<>();

        Set<State<S>> leftMatchedStates = new LinkedHashSet<>();
        Set<State<S>> rightMatchedStates = new LinkedHashSet<>();

        // 1.1 A combined state is added for every match in 'matching'.
        for (Entry<State<S>, State<S>> assignment: matching.entrySet()) {
            State<S> leftState = assignment.getKey();
            State<S> rightState = assignment.getValue();
            leftMatchedStates.add(leftState);
            rightMatchedStates.add(rightState);

            // Define and add the new combined state.
            Preconditions.checkArgument(
                    statePropertyCombiner.areCombinable(leftState.getProperty(), rightState.getProperty()),
                    "Expected all matched state pairs to have combinable state properties.");

            State<S> newState = diff
                    .addState(statePropertyCombiner.combine(leftState.getProperty(), rightState.getProperty()));

            // Keep track on how combined states are matched on LHS and RHS states.
            leftProjection.put(leftState, newState);
            rightProjection.put(rightState, newState);
        }

        // 1.2 An uncombined state is added for every state in LHS that is not matched.
        Set<State<S>> unmatchedLeftStates = lhs.getStates().stream().filter(s -> !leftMatchedStates.contains(s))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        for (State<S> leftState: unmatchedLeftStates) {
            State<S> newState = diff.addState(leftState.getProperty());
            leftProjection.put(leftState, newState);
        }

        // 1.3 An uncombined state is added for every state in RHS that is not matched.
        Set<State<S>> unmatchedRightStates = rhs.getStates().stream().filter(s -> !rightMatchedStates.contains(s))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        for (State<S> rightState: unmatchedRightStates) {
            State<S> newState = diff.addState(rightState.getProperty());
            rightProjection.put(rightState, newState);
        }

        // 2. Define all transitions of 'diff'.

        // Get sets of all LHS and RHS transitions, where all source and target states are projected to 'diff' states.
        Set<Transition<S, T>> leftTransitions = collectAllProjectedTransitionsOf(lhs, leftProjection);
        Set<Transition<S, T>> rightTransitions = collectAllProjectedTransitionsOf(rhs, rightProjection);

        // Combine all LHS and RHS transitions.
        Preconditions.checkArgument(transitionCombiner.areCombinable(leftTransitions, rightTransitions),
                "Expected sets to always be combinable.");

        transitionCombiner.combine(leftTransitions, rightTransitions).forEach(diff::addTransition);

        return diff;
    }

    /**
     * Gives the set of all transitions of {@code glts}, where all source and target states are mapped according to
     * {@code projection}.
     *
     * @param glts The GLTS for which to collect all transitions.
     * @param projection The projection function that is applied to the source and target states of all transitions.
     *     This function must contain a mapping for every state of {@code glts}.
     * @return The set of all transitions of {@code glts}, projected along {@code projection}.
     */
    private Set<Transition<S, T>> collectAllProjectedTransitionsOf(U glts, Map<State<S>, State<S>> projection) {
        return glts.getStates().stream()
                // Retrieve all transitions of 'glts'.
                .flatMap(state -> glts.getOutgoingTransitions(state).stream())
                // Map the source and target states of all transitions of 'glts' along 'projection'.
                .map(transition -> new Transition<>(projection.get(transition.getSource()), transition.getProperty(),
                        projection.get(transition.getTarget())))
                // Collect all projected transitions into a set.
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
