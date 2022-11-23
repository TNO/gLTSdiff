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

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.github.tno.gltsdiff.lts.LTS;
import com.github.tno.gltsdiff.lts.State;
import com.github.tno.gltsdiff.lts.Transition;
import com.github.tno.gltsdiff.operators.combiners.Combiner;
import com.github.tno.gltsdiff.operators.combiners.SetCombiner;
import com.github.tno.gltsdiff.operators.combiners.TransitionCombiner;
import com.google.common.base.Preconditions;

/**
 * A merger for merging two given LTSs (the LHS and RHS) into a single LTS. The merged LTS is constructed by combining
 * state properties and transition properties using specified combiners.
 *
 * @param <S> The type of state properties.
 * @param <T> The type of transition properties.
 * @param <U> The type of LTSs to merge.
 */
public class DefaultMerger<S, T, U extends LTS<S, T>> extends AbstractMerger<S, T, U> {
    /** The left-hand-side LTS. */
    private final U lhs;

    /** The right-hand-side LTS. */
    private final U rhs;

    /** The combiner for state properties. */
    private final Combiner<S> statePropertyCombiner;

    /** The combiner for sets of transitions. */
    private final Combiner<Set<Transition<S, T>>> transitionCombiner;

    /** The supplier for instantiating new LTSs. */
    private final Supplier<U> instantiator;

    /**
     * Instantiates a new default merger.
     * 
     * @param lhs The left-hand-side LTS.
     * @param rhs The right-hand-side LTS.
     * @param statePropertyCombiner The combiner for state properties.
     * @param transitionPropertyCombiner The combiner for transition properties.
     * @param instantiator The supplier for instantiating new LTSs.
     */
    public DefaultMerger(U lhs, U rhs, Combiner<S> statePropertyCombiner, Combiner<T> transitionPropertyCombiner,
            Supplier<U> instantiator)
    {
        super(statePropertyCombiner);
        this.lhs = lhs;
        this.rhs = rhs;
        this.statePropertyCombiner = statePropertyCombiner;
        this.transitionCombiner = new SetCombiner<>(new TransitionCombiner<>(transitionPropertyCombiner));
        this.instantiator = instantiator;
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
    protected U mergeInternal(Map<State<S>, State<S>> matching) {
        U diff = instantiator.get();

        // 1. Define all states of 'diff'.

        // Maps for maintaining how the states of LHS and RHS relate to the new states of 'diff'.
        Map<State<S>, State<S>> leftProjection = new LinkedHashMap<>();
        Map<State<S>, State<S>> rightProjection = new LinkedHashMap<>();

        Set<State<S>> leftMatchedStates = new LinkedHashSet<>();
        Set<State<S>> rightMatchedStates = new LinkedHashSet<>();

        // 1.1 A combined state is added for every match in 'matching'.
        // Note: Additional sorting is applied to get the state ordering more consistent with earlier implementations.
        for (Entry<State<S>, State<S>> assignment: matching.entrySet().stream().sorted(matchingComparator())
                .collect(Collectors.toList()))
        {
            State<S> leftState = assignment.getKey();
            State<S> rightState = assignment.getValue();
            leftMatchedStates.add(leftState);
            rightMatchedStates.add(rightState);

            // Define and add the new combined state.
            // Note: all matched state pairs have combinable state properties (otherwise there is a bug).
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

        // Get sets of all LTS and RHS transitions, where all source and target states are projected to 'diff' states.
        Set<Transition<S, T>> leftTransitions = collectAllProjectedTransitionsOf(lhs, leftProjection);
        Set<Transition<S, T>> rightTransitions = collectAllProjectedTransitionsOf(rhs, rightProjection);

        // Combine all LTS and RHS transitions.
        Preconditions.checkArgument(transitionCombiner.areCombinable(leftTransitions, rightTransitions),
                "Expected sets to always be combinable.");

        transitionCombiner.combine(leftTransitions, rightTransitions).forEach(diff::addTransition);

        return diff;
    }

    /** @return A comparator for state matchings that considers initial state arrows and state identifiers. */
    private Comparator<Entry<State<S>, State<S>>> matchingComparator() {
        return Comparator
                // Firstly compare LHS initial states (descending order: first true, then false).
                .comparing((Entry<State<S>, State<S>> entry) -> !lhs.isInitialState(entry.getKey()))
                // Secondly compare RHS initial states (descending order: first true, then false).
                .thenComparing(entry -> !rhs.isInitialState(entry.getValue()))
                // Thirdly compare LHS state identifiers.
                .thenComparing(entry -> entry.getKey().getId())
                // Lastly compare RHS state identifiers.
                .thenComparing(entry -> entry.getValue().getId());
    }

    /**
     * Gives the set of all transitions of {@code lts}, where all source and target states are mapped according to
     * {@code projection}.
     * 
     * @param lts The LTS for which to collect all transitions.
     * @param projection The projection function that is applied to the source and target states of all transitions.
     *     This function must contain a mapping for every state of {@code lts}.
     * @return The set of all transitions of {@code lts}, projected along {@code projection}.
     */
    private Set<Transition<S, T>> collectAllProjectedTransitionsOf(U lts, Map<State<S>, State<S>> projection) {
        return lts.getStates().stream()
                // Retrieve all transitions of 'lts'.
                .flatMap(state -> lts.getOutgoingTransitions(state).stream())
                // Map the source and target states of all transitions of 'lts' along 'projection'.
                .map(transition -> new Transition<>(projection.get(transition.getSource()), transition.getProperty(),
                        projection.get(transition.getTarget())))
                // Collect all projected transitions into a set.
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
