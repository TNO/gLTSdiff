
package com.github.tno.gltsdiff.rewriters;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.tno.gltsdiff.lts.DiffAutomaton;
import com.github.tno.gltsdiff.lts.DiffAutomatonStateProperty;
import com.github.tno.gltsdiff.lts.DiffKind;
import com.github.tno.gltsdiff.lts.DiffProperty;
import com.github.tno.gltsdiff.lts.State;
import com.github.tno.gltsdiff.lts.Transition;
import com.google.common.collect.Sets;

/**
 * A rewriter for rewriting tangles in {@link DiffAutomaton difference automata}.
 * <p>
 * A <i>tangle</i> is defined to be an {@link DiffKind#UNCHANGED unchanged} state that has at least one incoming or
 * outgoing transition, and all incoming/outgoing transitions are either {@link DiffKind#ADDED added} or
 * {@link DiffKind#REMOVED removed}. This rewriter splits each tangle state into two states, an added and a removed one,
 * and relocates all transitions accordingly to these two states.
 * </p>
 *
 * @param <T> The type of transition properties.
 */
public class DisentangleRewriter<T> implements Rewriter<DiffAutomatonStateProperty, DiffProperty<T>, DiffAutomaton<T>> {
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

            // If there are no such "connected" properties, then 'state' cannot be a tangle.
            if (connectedTransitionProperties.isEmpty()) {
                continue;
            }

            // Otherwise 'state' has at least one "connected" transition property.
            // Check whether all such properties are either added or removed, to determine whether 'state' is a tangle.
            boolean isTangleState = connectedTransitionProperties.stream()
                    .noneMatch(property -> property.getDiffKind() == DiffKind.UNCHANGED);

            if (isTangleState) {
                DiffAutomatonStateProperty stateProperty = state.getProperty();

                // We proceed by splitting 'state' into two states, an added and a removed one, and relocating all
                // incoming/outgoing transitions of 'state' accordingly. First we define the added state.
                State<DiffAutomatonStateProperty> addedState = automaton
                        .addState(new DiffAutomatonStateProperty(stateProperty.isAccepting(), DiffKind.ADDED,
                                stateProperty.isInitial() && stateProperty.getInitDiffKind() != DiffKind.REMOVED
                                        ? Optional.of(DiffKind.ADDED) : Optional.empty()));

                // Relocate all incoming added transitions into 'state'.
                List<Transition<DiffAutomatonStateProperty, DiffProperty<T>>> incomingTransitions = automaton
                        .getIncomingTransitions(state).stream()
                        .filter(transition -> transition.getProperty().getDiffKind() == DiffKind.ADDED)
                        .collect(Collectors.toList());

                for (Transition<DiffAutomatonStateProperty, DiffProperty<T>> transition: incomingTransitions) {
                    automaton.removeTransition(transition);
                    automaton.addTransition(transition.getSource(), transition.getProperty(), addedState);
                }

                // Relocate all outgoing added transitions out of 'state'.
                List<Transition<DiffAutomatonStateProperty, DiffProperty<T>>> outgoingTransitions = automaton
                        .getOutgoingTransitions(state).stream()
                        .filter(transition -> transition.getProperty().getDiffKind() == DiffKind.ADDED)
                        .collect(Collectors.toList());

                for (Transition<DiffAutomatonStateProperty, DiffProperty<T>> transition: outgoingTransitions) {
                    automaton.removeTransition(transition);
                    automaton.addTransition(addedState, transition.getProperty(), transition.getTarget());
                }

                // Finally, turn the original tangle state into a removed state.
                automaton.setStateProperty(state,
                        new DiffAutomatonStateProperty(stateProperty.isAccepting(), DiffKind.REMOVED,
                                stateProperty.isInitial() && stateProperty.getInitDiffKind() != DiffKind.ADDED
                                        ? Optional.of(DiffKind.REMOVED) : Optional.empty()));

                // Remember that 'automaton' has been updated.
                updated = true;
            }
        }

        return updated;
    }
}
