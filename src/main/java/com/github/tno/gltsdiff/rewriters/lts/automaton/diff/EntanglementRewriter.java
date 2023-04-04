//////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2022-2023 Contributors to the GitHub community
//
// This program and the accompanying materials are made available
// under the terms of the MIT License which is available at
// https://opensource.org/licenses/MIT
//
// SPDX-License-Identifier: MIT
//////////////////////////////////////////////////////////////////////////////

package com.github.tno.gltsdiff.rewriters.lts.automaton.diff;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.tno.gltsdiff.glts.State;
import com.github.tno.gltsdiff.glts.Transition;
import com.github.tno.gltsdiff.glts.lts.automaton.diff.DiffAutomaton;
import com.github.tno.gltsdiff.glts.lts.automaton.diff.DiffAutomatonStateProperty;
import com.github.tno.gltsdiff.glts.lts.automaton.diff.DiffKind;
import com.github.tno.gltsdiff.glts.lts.automaton.diff.DiffProperty;
import com.github.tno.gltsdiff.utils.TriFunction;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

/**
 * A rewriter that rewrites tangles in {@link DiffAutomaton difference automata}.
 *
 * <p>
 * A <i>tangle</i> is defined to be an {@link DiffKind#UNCHANGED unchanged} state that has no unchanged incoming or
 * outgoing transitions, at least one {@link DiffKind#ADDED added} incoming/outgoing transition, and at least one
 * {@link DiffKind#REMOVED removed} incoming/outgoing transition. This rewriter splits each tangle state into two
 * states, an added and a removed one, and relocates all transitions accordingly to these two states.
 * </p>
 *
 * @param <S> The type of difference automaton state properties.
 * @param <T> The type of transition properties.
 * @param <U> The type of difference automata to rewrite.
 */
public class EntanglementRewriter<S extends DiffAutomatonStateProperty, T, U extends DiffAutomaton<S, T>>
        extends DiffAutomatonRewriter<S, T, U>
{
    /**
     * Instantiates a new entanglement rewriter.
     *
     * @param statePropertyTransformer Function to transform a difference automaton state property. Given an existing
     *     difference automaton state property, a new state difference kind, and a new initial state difference kind
     *     (present if state is an initial state, absent otherwise), returns a difference automaton state property that
     *     has the new (initial) state difference kinds. The function should not modify the existing state property.
     */
    public EntanglementRewriter(TriFunction<S, DiffKind, Optional<DiffKind>, S> statePropertyTransformer) {
        super(statePropertyTransformer);
    }

    @Override
    public boolean rewrite(U automaton) {
        boolean updated = false;

        // Iterate over all unchanged states of 'automaton'.
        Set<State<S>> unchangedStates = automaton.getStates().stream()
                .filter(state -> state.getProperty().getStateDiffKind() == DiffKind.UNCHANGED)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        for (State<S> state: unchangedStates) {
            // Collect the properties of all incoming and outgoing transitions of 'state'.
            Set<DiffProperty<T>> connectedTransitionProperties = Sets.union(
                    automaton.getIncomingTransitionProperties(state), automaton.getOutgoingTransitionProperties(state));

            // Count how often the various difference kinds occur in 'connectedTransitionProperties' on the top level.
            Map<DiffKind, Long> diffKindCounts = connectedTransitionProperties.stream()
                    .collect(Collectors.groupingBy(DiffProperty::getDiffKind, Collectors.counting()));

            // If 'state' does not have any unchanged incoming/outgoing transitions, but has both added and removed
            // incoming/outgoing transitions, then 'state' is a tangle state.
            if (diffKindCounts.getOrDefault(DiffKind.UNCHANGED, 0L) == 0
                    && diffKindCounts.getOrDefault(DiffKind.ADDED, 0L) > 0
                    && diffKindCounts.getOrDefault(DiffKind.REMOVED, 0L) > 0)
            {
                // Split 'state' into an added and a remove state, and relocate all transitions accordingly.
                split(automaton, state, DiffKind.ADDED);
                split(automaton, state, DiffKind.REMOVED);

                // Make sure that all transitions of 'state' have been relocated.
                Preconditions.checkArgument(automaton.getIncomingTransitions(state).isEmpty(),
                        "Expected all incoming transitions of the tangle state to be relocated.");
                Preconditions.checkArgument(automaton.getOutgoingTransitions(state).isEmpty(),
                        "Expected all outgoing transitions of the tangle state to be relocated.");

                // Remove the tangle state, and remember that 'automaton' has been updated.
                automaton.removeState(state);
                updated = true;
            }
        }

        return updated;
    }

    /**
     * Splits {@code state} by defining a new state with the given difference kind ({@code diffKind}), and relocating
     * all incoming and outgoing transitions of {@code state} with this difference kind to the new state.
     *
     * @param automaton The difference automaton in which to define the new state.
     * @param state The state to split.
     * @param diffKind The difference kind of the new state, and of the transitions to relocate.
     */
    private void split(U automaton, State<S> state, DiffKind diffKind) {
        S stateProperty = state.getProperty();

        // Define the new state.
        boolean isInitial = stateProperty.isInitial() && (stateProperty.getInitDiffKind() == DiffKind.UNCHANGED
                || stateProperty.getInitDiffKind() == diffKind);

        S newStateProperty = statePropertyTransformer.apply(stateProperty, diffKind,
                isInitial ? Optional.of(diffKind) : Optional.empty());
        State<S> newState = automaton.addState(newStateProperty);

        // Relocate all incoming transitions of 'state'.
        List<Transition<S, DiffProperty<T>>> incomingTransitions = automaton.getIncomingTransitions(state).stream()
                .filter(transition -> transition.getProperty().getDiffKind() == diffKind).collect(Collectors.toList());

        for (Transition<S, DiffProperty<T>> transition: incomingTransitions) {
            automaton.removeTransition(transition);
            automaton.addTransition(transition.getSource(), transition.getProperty(), newState);
        }

        // Relocate all outgoing transitions of 'state'.
        List<Transition<S, DiffProperty<T>>> outgoingTransitions = automaton.getOutgoingTransitions(state).stream()
                .filter(transition -> transition.getProperty().getDiffKind() == diffKind).collect(Collectors.toList());

        for (Transition<S, DiffProperty<T>> transition: outgoingTransitions) {
            automaton.removeTransition(transition);
            automaton.addTransition(newState, transition.getProperty(), transition.getTarget());
        }
    }
}
