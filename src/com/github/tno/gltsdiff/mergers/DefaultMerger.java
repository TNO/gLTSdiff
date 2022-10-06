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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.github.tno.gltsdiff.lts.LTS;
import com.github.tno.gltsdiff.lts.State;
import com.github.tno.gltsdiff.lts.Transition;
import com.github.tno.gltsdiff.operators.combiners.Combiner;

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

    /** The combiner for transition properties. */
    private final Combiner<T> transitionPropertyCombiner;

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
        this.transitionPropertyCombiner = transitionPropertyCombiner;
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

        // 2.1 First define all combined transitions (as well as uncombined ones depending on circumstances).
        // Iterate over all state matchings, since these are the starting points of all combined transitions.
        for (Entry<State<S>, State<S>> assignment: matching.entrySet()) {
            State<S> leftState = assignment.getKey();
            State<S> rightState = assignment.getValue();

            // Keep track of leftover right transitions that have not yet been used to create a new combined transition.
            List<Transition<S, T>> rightTransitions = new LinkedList<>(rhs.getOutgoingTransitions(rightState));

            // Iterate over all transitions out of 'leftState' and try to find corresponding ones in 'rightTransitions'.
            for (Transition<S, T> leftTransition: lhs.getOutgoingTransitions(leftState)) {
                T leftProperty = leftTransition.getProperty();
                State<S> leftSucc = leftTransition.getTarget();

                // Try to find the first available right transition that is combinable with 'leftTransition'.
                Optional<Transition<S, T>> possibleRightTransition = Optional.empty();

                if (leftMatchedStates.contains(leftSucc)) {
                    possibleRightTransition = rightTransitions.stream()
                            .filter(transition -> transition.getSource() == rightState
                                    && transitionPropertyCombiner.areCombinable(leftProperty, transition.getProperty())
                                    && transition.getTarget() == matching.get(leftSucc))
                            .findFirst();
                }

                // Is such a matching transition still available?
                if (possibleRightTransition.isPresent()) {
                    Transition<S, T> rightTransition = possibleRightTransition.get();

                    // If so, then both transitions and turned into a single combined transition in 'diff'.
                    T combinedProperty = transitionPropertyCombiner.combine(leftProperty,
                            rightTransition.getProperty());
                    diff.addTransition(leftProjection.get(leftState), combinedProperty, leftProjection.get(leftSucc));

                    // The RHS transition cannot be combined with any other transition. Mark it as such.
                    rightTransitions.remove(rightTransition);
                } else {
                    // If not, then either (1) 'leftSucc' is not matched to any RHS state, or (2) 'leftSucc' is
                    // matched to a RHS state but no corresponding transition is available in 'rightTransitions'.
                    // In both these scenarios, 'leftTransition' will turn into an uncombined transition in 'diff'.
                    diff.addTransition(leftProjection.get(leftState), leftProperty, leftProjection.get(leftSucc));
                }
            }

            // At this point, possibly not all 'rightTransitions' have been part of new combined transitions in 'diff'.
            // Iterate over all these leftover transitions and add them as uncombined transitions.
            for (Transition<S, T> rightTransition: rightTransitions) {
                T property = rightTransition.getProperty();
                State<S> rightSucc = rightTransition.getTarget();
                diff.addTransition(rightProjection.get(rightState), property, rightProjection.get(rightSucc));
            }
        }

        // 2.2 Add a new (uncombined) transition for every transition that goes out of an unmatched LHS state.
        for (State<S> leftState: unmatchedLeftStates) {
            for (Transition<S, T> leftTransition: lhs.getOutgoingTransitions(leftState)) {
                State<S> newSrc = leftProjection.get(leftState);
                T property = leftTransition.getProperty();
                State<S> newDst = leftProjection.get(leftTransition.getTarget());
                diff.addTransition(newSrc, property, newDst);
            }
        }

        // 2.3 Add a new (uncombined) transition for every transition that goes out of an unmatched RHS state.
        for (State<S> rightState: unmatchedRightStates) {
            for (Transition<S, T> rightTransition: rhs.getOutgoingTransitions(rightState)) {
                State<S> newSrc = rightProjection.get(rightState);
                T property = rightTransition.getProperty();
                State<S> newDst = rightProjection.get(rightTransition.getTarget());
                diff.addTransition(newSrc, property, newDst);
            }
        }

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
}
