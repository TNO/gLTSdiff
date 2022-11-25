
package com.github.tno.gltsdiff.rewriters;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
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
 * A <i>tangle</i> is defined to be an {@link DiffKind#UNCHANGED unchanged} state that has no unchanged incoming or
 * outgoing transitions, at least one {@link DiffKind#ADDED added} incoming/outgoing transition, and at least one
 * {@link DiffKind#REMOVED removed} incoming/outgoing transition. This rewriter splits all tangle states into two
 * states, an added and a removed one, and relocates all transitions accordingly to these new states.
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

            // Count how often the various difference kinds occur in 'connectedTransitionProperties' on the top level.
            Map<DiffKind, Long> diffKindCounts = connectedTransitionProperties.stream()
                    .collect(Collectors.groupingBy(DiffProperty::getDiffKind, Collectors.counting()));

            // If 'state' does not have any unchanged incoming/outgoing transitions, but has both added and removed
            // incoming/outgoing transitions, then 'state' is a tangle state.
            if (diffKindCounts.getOrDefault(DiffKind.UNCHANGED, 0L) == 0
                    && diffKindCounts.getOrDefault(DiffKind.ADDED, 0L) > 0
                    && diffKindCounts.getOrDefault(DiffKind.REMOVED, 0L) > 0)
            {
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
