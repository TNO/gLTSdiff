//////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2021-2023 Contributors to the GitHub community
//
// This program and the accompanying materials are made available
// under the terms of the MIT License which is available at
// https://opensource.org/licenses/MIT
//
// SPDX-License-Identifier: MIT
//////////////////////////////////////////////////////////////////////////////

package com.github.tno.gltsdiff.glts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.github.tno.gltsdiff.operators.projectors.Projector;
import com.google.common.base.Preconditions;

/**
 * A generalized labeled transition system with associated properties.
 *
 * @param <S> The type of state properties.
 * @param <T> The type of transition properties.
 */
public class GLTS<S, T> {
    /**
     * The set of states of this GLTS, all of which are unique and non-{@code null}, and have identifiers between
     * {@code 0} and {@code size() - 1} that are unique within this GLTS.
     */
    private final Set<State<S>> statesSet = new LinkedHashSet<>();

    /**
     * A list of states of this GLTS, which should coincide with {@link #statesSet}. This separate list is maintained
     * for performance reasons, mainly to efficiently implement {@link #getStateById(int)}.
     */
    private final List<State<S>> statesList = new ArrayList<>();

    /** A mapping from states to all their incoming transitions, all of which are non-{@code null}. */
    private final Map<State<S>, List<Transition<S, T>>> incomingTransitions = new LinkedHashMap<>();

    /** A mapping from states to all their outgoing transitions, all of which are non-{@code null}. */
    private final Map<State<S>, List<Transition<S, T>>> outgoingTransitions = new LinkedHashMap<>();

    /**
     * Returns whether the specified state exists in this GLTS.
     *
     * @param state The non-{@code null} state.
     * @return {@code true} if the state exists in this GLTS, {@code false} otherwise.
     */
    public boolean hasState(State<S> state) {
        Preconditions.checkNotNull(state, "Expected a non-null source state.");
        return getStates().contains(state);
    }

    /**
     * Returns whether the specified transition exists in this GLTS.
     *
     * @param source The non-{@code null} source state, which must exist in this GLTS.
     * @param property The non-{@code null} transition property.
     * @param target The non-{@code null} target state, which must exist in this GLTS.
     * @return {@code true} if the given transition exists in this GLTS, {@code false} otherwise.
     */
    public boolean hasTransition(State<S> source, T property, State<S> target) {
        Preconditions.checkNotNull(source, "Expected a non-null source state.");
        Preconditions.checkNotNull(property, "Expected a non-null transition property.");
        Preconditions.checkNotNull(target, "Expected a non-null target state.");
        Preconditions.checkArgument(hasState(source), "Expected an existing source state.");
        Preconditions.checkArgument(hasState(target), "Expected an existing target state.");

        return getOutgoingTransitions(source).stream()
                .anyMatch(transition -> transition.getProperty().equals(property) && transition.getTarget() == target);
    }

    /**
     * Returns whether the specified transition exists in this GLTS.
     *
     * @param transition The non-{@code null} transition.
     * @return {@code true} if the given transition exists in this GLTS, {@code false} otherwise.
     */
    public boolean hasTransition(Transition<S, T> transition) {
        return hasTransition(transition.getSource(), transition.getProperty(), transition.getTarget());
    }

    /**
     * Returns the states of this GLTS.
     *
     * @return The set of all states of this GLTS, all of which are non-{@code null} and have identifiers between
     *     {@code 0} and {@code size() - 1} that are unique within this GLTS.
     */
    public Set<State<S>> getStates() {
        return Collections.unmodifiableSet(statesSet);
    }

    /**
     * Returns the state with the specified identifier.
     *
     * @param id The state identifier, which must be between {@code 0} and {@code size() - 1}.
     * @return The non-{@code null} state within this GLTS with the specified identifier.
     */
    public State<S> getStateById(int id) {
        Preconditions.checkArgument(0 <= id && id < statesSet.size(), "Expected the identifier to be within range.");
        State<S> state = statesList.get(id);
        Preconditions.checkArgument(state.getId() == id, "Expected state identifiers to be consistent.");
        return state;
    }

    /**
     * Returns the transitions of this GLTS.
     *
     * @return The set of all transitions of this GLTS, all of which are non-{@code null}.
     */
    public Set<Transition<S, T>> getTransitions() {
        return outgoingTransitions.values().stream().flatMap(ts -> ts.stream())
                .collect(Collectors.toCollection(() -> new LinkedHashSet<>()));
    }

    /**
     * Returns the set of states that can reach the given state by a single transition.
     *
     * @param state The non-{@code null} target state, which must exist in this GLTS.
     * @return The set of all reachable states from {@code state} by a single transition, all of which are
     *     non-{@code null}.
     */
    public Set<State<S>> getPredecessorsOf(State<S> state) {
        Preconditions.checkNotNull(state, "Expected a non-null state.");
        return getIncomingTransitions(state).stream().map(Transition::getSource)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * Returns the set of states that the specified state can reach by a single transition.
     *
     * @param state The non-{@code null} source state, which must exist in this GLTS.
     * @return The set of all co-reachable states from {@code state} by a single transition, all of which are
     *     non-{@code null}.
     */
    public Set<State<S>> getSuccessorsOf(State<S> state) {
        Preconditions.checkNotNull(state, "Expected a non-null state.");
        return getOutgoingTransitions(state).stream().map(Transition::getTarget)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * Returns all transitions that go into the specified target state.
     *
     * @param state The non-{@code null} target state, which must exist in this GLTS.
     * @return All transitions that go into the given target state, all of which are non-{@code null}.
     */
    public List<Transition<S, T>> getIncomingTransitions(State<S> state) {
        Preconditions.checkNotNull(state, "Expected a non-null target state.");
        Preconditions.checkArgument(statesSet.contains(state), "Expected an existing target state.");
        return Collections.unmodifiableList(incomingTransitions.get(state));
    }

    /**
     * Returns all transitions that go out of the specified source state.
     *
     * @param state The non-{@code null} source state, which must exist in this GLTS.
     * @return All transitions that go out of the given source state, all of which are non-{@code null}.
     */
    public List<Transition<S, T>> getOutgoingTransitions(State<S> state) {
        Preconditions.checkNotNull(state, "Expected a non-null source state.");
        Preconditions.checkArgument(statesSet.contains(state), "Expected an existing source state.");
        return Collections.unmodifiableList(outgoingTransitions.get(state));
    }

    /**
     * Returns the properties of all transitions that go into the specified target state.
     *
     * @param state The non-{@code null} target state, which must exist in this GLTS.
     * @return The properties of all transitions going into the given state, all of which are non-{@code null}.
     */
    public Set<T> getIncomingTransitionProperties(State<S> state) {
        Preconditions.checkNotNull(state, "Expected a non-null target state.");
        return getIncomingTransitions(state).stream().map(Transition::getProperty)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * Returns the properties of all transitions that go out of the specified source state.
     *
     * @param state The non-{@code null} source state, which must exist in this GLTS.
     * @return The properties of all transitions going out of the given state, all of which are non-{@code null}.
     */
    public Set<T> getOutgoingTransitionProperties(State<S> state) {
        Preconditions.checkNotNull(state, "Expected a non-null source state.");
        return getOutgoingTransitions(state).stream().map(Transition::getProperty)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * Returns the number of states in this GLTS.
     *
     * @return The number of states in this GLTS.
     */
    public int size() {
        return getStates().size();
    }

    /**
     * Count states of this GLTS that satisfy a given predicate.
     *
     * @param predicate The predicate.
     * @return The number of states that satisfy the predicate.
     */
    public long countStates(Predicate<State<S>> predicate) {
        return getStates().stream().filter(predicate).count();
    }

    /**
     * Count transitions of this GLTS with a property that satisfies a given predicate.
     *
     * @param predicate The predicate.
     * @return The number of transitions with a property that satisfies the predicate.
     */
    public long countTransitions(Predicate<T> predicate) {
        return getStates().stream().flatMap(state -> getOutgoingTransitions(state).stream())
                .map(Transition::getProperty).filter(predicate).count();
    }

    /**
     * Updates the identifier of the given state.
     *
     * @param state The state whose identifier to update.
     * @param id The new state identifier.
     */
    protected void updateStateId(State<S> state, int id) {
        Preconditions.checkNotNull(state, "Expected a non-null state.");
        state.id = id;
    }

    /**
     * Replaces the state property associated to the given state with the specified new property.
     *
     * @param state The non-{@code null} state whose property is to be replaced, which must exist in this GLTS.
     * @param property The non-{@code null} new state property.
     */
    public void setStateProperty(State<S> state, S property) {
        Preconditions.checkArgument(statesSet.contains(state), "Expected an existing state.");
        state.setProperty(property);
    }

    /**
     * Adds a state to this GLTS.
     *
     * @param property The non-{@code null} property to associate to the newly added state.
     * @return The non-{@code null} newly added state, which has an identifier between {@code 0} and {@code size() - 1}
     *     that is unique within this GLTS.
     */
    public State<S> addState(S property) {
        Preconditions.checkNotNull(property, "Expected a non-null state property.");

        State<S> newState = new State<>(statesSet.size(), property);
        statesList.add(newState);
        statesSet.add(newState);
        incomingTransitions.put(newState, new LinkedList<>());
        outgoingTransitions.put(newState, new LinkedList<>());

        Preconditions.checkArgument(statesSet.size() == statesList.size(),
                "Expected a consistent internal representation of states.");

        return newState;
    }

    /**
     * Tries adding the specified transition to this GLTS.
     *
     * @param source The non-{@code null} source state, which must exist in this GLTS.
     * @param property The non-{@code null} transition property.
     * @param target The non-{@code null} target state, which must exist in this GLTS.
     * @return {@code true} if the transition has been added to this GLTS, {@code false} if it already existed.
     */
    public boolean tryAddTransition(State<S> source, T property, State<S> target) {
        if (hasTransition(source, property, target)) {
            return false;
        }

        addTransition(source, property, target);
        return true;
    }

    /**
     * Tries adding the specified transition to this GLTS.
     *
     * @param transition The non-{@code null} transition, whose source and target states must exist in this GLTS.
     * @return {@code true} if the transition has been added to this GLTS, {@code false} if it already existed.
     */
    public boolean tryAddTransition(Transition<S, T> transition) {
        return tryAddTransition(transition.getSource(), transition.getProperty(), transition.getTarget());
    }

    /**
     * Adds the given transition to this GLTS.
     *
     * @param transition The non-{@code null} transition to add, which must not already exist in this GLTS, but whose
     *     source and target states must exist in this GLTS.
     */
    public void addTransition(Transition<S, T> transition) {
        Preconditions.checkNotNull(transition, "Expected a non-null transition.");
        addTransition(transition.getSource(), transition.getProperty(), transition.getTarget());
    }

    /**
     * Adds a transition to this GLTS, which must not already exist in this GLTS.
     *
     * @param source The non-{@code null} source state, which must exist in this GLTS.
     * @param property The non-{@code null} transition property.
     * @param target The non-{@code null} target state, which must exist in this GLTS.
     */
    public void addTransition(State<S> source, T property, State<S> target) {
        Preconditions.checkNotNull(source, "Expected a non-null source state.");
        Preconditions.checkNotNull(property, "Expected a non-null transition property.");
        Preconditions.checkNotNull(target, "Expected a non-null target state.");
        Preconditions.checkArgument(statesSet.contains(source), "Expected an existing source state.");
        Preconditions.checkArgument(statesSet.contains(target), "Expected an existing target state.");
        Preconditions.checkArgument(!hasTransition(source, property, target), "Expected a non-existing transition.");

        Transition<S, T> transition = new Transition<>(source, property, target);
        incomingTransitions.get(target).add(transition);
        outgoingTransitions.get(source).add(transition);
    }

    /**
     * Removes the given state from this GLTS, thereby ensuring that the identifiers of all other states of this GLTS
     * remain unique and between {@code 0} and {@code size() - 1}.
     *
     * @param state The non-{@code null} state to remove, which must exist in this GLTS.
     */
    public void removeState(State<S> state) {
        Preconditions.checkNotNull(state, "Expected a non-null state.");
        Preconditions.checkArgument(statesSet.contains(state), "Expected an existing state.");
        Preconditions.checkArgument(statesList.get(state.id) == state, "Expected state identifiers to be consistent.");

        // Remove all transitions involving the given state.
        getSuccessorsOf(state).forEach(s -> incomingTransitions.get(s).removeIf(t -> t.getSource() == state));
        getPredecessorsOf(state).forEach(s -> outgoingTransitions.get(s).removeIf(t -> t.getTarget() == state));

        // Remove the given state.
        incomingTransitions.remove(state);
        outgoingTransitions.remove(state);
        statesList.remove(state.id);
        statesSet.remove(state);

        Preconditions.checkArgument(statesSet.size() == statesList.size(),
                "Expected a consistent internal representation of states.");

        // Re-assign identifiers of leftover states.
        for (int i = state.getId(); i < statesSet.size(); i++) {
            statesList.get(i).id = i;
        }
    }

    /**
     * Removes the given transition from this GLTS, which must exist in this GLTS.
     *
     * @param source The non-{@code null} source state of the transition to remove.
     * @param property The non-{@code null} property of the transition to remove.
     * @param target The non-{@code null} target state of the transition to remove.
     */
    public void removeTransition(State<S> source, T property, State<S> target) {
        Preconditions.checkNotNull(source, "Expected a non-null source state.");
        Preconditions.checkNotNull(property, "Expected a non-null transition property.");
        Preconditions.checkNotNull(target, "Expected a non-null target state.");
        Preconditions.checkArgument(statesSet.contains(source), "Expected an existing source state.");
        Preconditions.checkArgument(statesSet.contains(target), "Expected an existing target state.");
        Preconditions.checkArgument(hasTransition(source, property, target), "Expected an existing transition.");

        Transition<S, T> transition = new Transition<>(source, property, target);
        incomingTransitions.get(target).remove(transition);
        outgoingTransitions.get(source).remove(transition);
    }

    /**
     * Removes the specified transition from this GLTS.
     *
     * @param transition The non-{@code null} transition to remove, which must exist in this GLTS.
     */
    public void removeTransition(Transition<S, T> transition) {
        Preconditions.checkNotNull(transition, "Expected a non-null transition.");
        removeTransition(transition.getSource(), transition.getProperty(), transition.getTarget());
    }

    /**
     * Maps this GLTS to a different (kind of) GLTS, thereby filtering out any state with a property that is mapped to
     * {@code null}, as well as any transition with a property that is mapped to {@code null}.
     *
     * @param <U> The target type of state properties.
     * @param <V> The target type of transition properties.
     * @param <L> The type of the resulting GLTS.
     * @param instantiator A supplier that instantiates new GLTSs of the appropriate type.
     * @param statePropertyMapper A function for mapping state properties. Any state with an associated property that is
     *     mapped to {@code null} will not be included in the returned GLTS.
     * @param transitionPropertyMapper A function for mapping transition properties. Any transition with a property that
     *     is mapped to {@code null} will not be included in the returned GLTS.
     * @return The GLTS with mapped state and transition properties.
     */
    public <U, V, L extends GLTS<U, V>> L map(Supplier<L> instantiator, Function<S, U> statePropertyMapper,
            Function<T, V> transitionPropertyMapper)
    {
        // Instantiate a fresh GLTS.
        L mappedGlts = instantiator.get();

        // Define all states.
        Map<State<S>, State<U>> stateMapping = new LinkedHashMap<>(size());

        for (State<S> state: getStates()) {
            U mappedStateProperty = statePropertyMapper.apply(state.getProperty());

            if (mappedStateProperty != null) {
                State<U> mappedState = mappedGlts.addState(mappedStateProperty);
                stateMapping.put(state, mappedState);
            }
        }

        // Define all transitions.
        for (State<S> state: getStates()) {
            for (Transition<S, T> transition: getOutgoingTransitions(state)) {
                State<U> mappedSource = stateMapping.get(transition.getSource());
                V mappedTransitionProperty = transitionPropertyMapper.apply(transition.getProperty());
                State<U> mappedTarget = stateMapping.get(transition.getTarget());

                if (mappedSource != null && mappedTransitionProperty != null && mappedTarget != null) {
                    mappedGlts.tryAddTransition(mappedSource, mappedTransitionProperty, mappedTarget);
                }
            }
        }

        return mappedGlts;
    }

    /**
     * Projects this GLTS by projecting all state and transition properties along a given element {@code along}.
     *
     * @param <L> The target type of GLTSs to project to.
     * @param <U> The type of elements to project along.
     * @param instantiator A supplier that instantiates new GLTSs of the appropriate type.
     * @param statePropertyProjector A projector for projecting state properties.
     * @param transitionPropertyProjector A projector for projecting transition properties.
     * @param along The non-{@code null} element to project along.
     * @return The projected GLTS.
     */
    public <L extends GLTS<S, T>, U> L project(Supplier<L> instantiator, Projector<S, U> statePropertyProjector,
            Projector<T, U> transitionPropertyProjector, U along)
    {
        Preconditions.checkNotNull(along, "Expected a non-null element to project along.");

        return map(instantiator, stateProperty -> statePropertyProjector.project(stateProperty, along).orElse(null),
                transitionProperty -> transitionPropertyProjector.project(transitionProperty, along).orElse(null));
    }
}
