//////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2022-2023 Contributors to the GitHub community
//
// This program and the accompanying materials are made available
// under the terms of the MIT License which is available at
// https://opensource.org/licenses/MIT
//
// SPDX-License-Identifier: MIT
//////////////////////////////////////////////////////////////////////////////

package com.github.tno.gltsdiff.rewriters;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.tno.gltsdiff.glts.DiffAutomaton;
import com.github.tno.gltsdiff.glts.DiffAutomatonStateProperty;
import com.github.tno.gltsdiff.glts.DiffKind;
import com.github.tno.gltsdiff.glts.DiffProperty;
import com.github.tno.gltsdiff.glts.State;
import com.github.tno.gltsdiff.glts.Transition;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

/**
 * A rewriter for rewriting tangles in {@link DiffAutomaton difference automata}.
 *
 * <p>
 * A <i>tangle</i> is defined to be an {@link DiffKind#UNCHANGED unchanged} state that has no unchanged incoming or
 * outgoing transitions, at least one {@link DiffKind#ADDED added} incoming/outgoing transition, and at least one
 * {@link DiffKind#REMOVED removed} incoming/outgoing transition. This rewriter splits each tangle state into two
 * states, an added and a removed one, and relocates all transitions accordingly to these two states.
 * </p>
 *
 * @param <T> The type of transition properties.
 */
public class EntanglementRewriter<T>
        implements Rewriter<DiffAutomatonStateProperty, DiffProperty<T>, DiffAutomaton<T>>
{
    @Override
    public boolean rewrite(DiffAutomaton<T> automaton) {
        boolean updated = false;

        // Iterate over all unchanged states of 'automaton'.
        Set<State<DiffAutomatonStateProperty>> unchangedStates = automaton.getStates().stream()
                .filter(state -> state.getProperty().getStateDiffKind() == DiffKind.UNCHANGED)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        for (State<DiffAutomatonStateProperty> state: unchangedStates) {
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
    private void split(DiffAutomaton<T> automaton, State<DiffAutomatonStateProperty> state, DiffKind diffKind) {
        DiffAutomatonStateProperty stateProperty = state.getProperty();

        // Define the new state.
        boolean isInitial = stateProperty.isInitial() && (stateProperty.getInitDiffKind() == DiffKind.UNCHANGED
                || stateProperty.getInitDiffKind() == diffKind);

        State<DiffAutomatonStateProperty> newState = automaton.addState(new DiffAutomatonStateProperty(
                stateProperty.isAccepting(), diffKind, isInitial ? Optional.of(diffKind) : Optional.empty()));

        // Relocate all incoming transitions of 'state'.
        List<Transition<DiffAutomatonStateProperty, DiffProperty<T>>> incomingTransitions = automaton
                .getIncomingTransitions(state).stream()
                .filter(transition -> transition.getProperty().getDiffKind() == diffKind).collect(Collectors.toList());

        for (Transition<DiffAutomatonStateProperty, DiffProperty<T>> transition: incomingTransitions) {
            automaton.removeTransition(transition);
            automaton.addTransition(transition.getSource(), transition.getProperty(), newState);
        }

        // Relocate all outgoing transitions of 'state'.
        List<Transition<DiffAutomatonStateProperty, DiffProperty<T>>> outgoingTransitions = automaton
                .getOutgoingTransitions(state).stream()
                .filter(transition -> transition.getProperty().getDiffKind() == diffKind).collect(Collectors.toList());

        for (Transition<DiffAutomatonStateProperty, DiffProperty<T>> transition: outgoingTransitions) {
            automaton.removeTransition(transition);
            automaton.addTransition(newState, transition.getProperty(), transition.getTarget());
        }
    }
}
