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

import java.util.Optional;
import java.util.function.Function;

import com.github.tno.gltsdiff.operators.projectors.DiffAutomatonStatePropertyProjector;
import com.github.tno.gltsdiff.operators.projectors.DiffKindProjector;
import com.github.tno.gltsdiff.operators.projectors.DiffPropertyProjector;
import com.github.tno.gltsdiff.operators.projectors.Projector;
import com.google.common.base.Preconditions;

/**
 * A difference automaton, which is a concrete automaton with difference information associated to states, initial
 * states and transitions.
 *
 * <p>
 * Difference automata maintain the invariant that difference kinds are always properly nested, meaning that:
 * <ul>
 * <li>State/transition properties that are {@link DiffKind#ADDED added} cannot contain nested properties that are
 * {@link DiffKind#REMOVED removed} or {@link DiffKind#UNCHANGED unchanged}.</li>
 * <li>State/transition properties that are {@link DiffKind#REMOVED removed} cannot contain nested properties that are
 * {@link DiffKind#ADDED added} or {@link DiffKind#UNCHANGED unchanged}.</li>
 * </ul>
 * </p>
 *
 * @param <T> The type of transition properties.
 */
public class DiffAutomaton<T> extends Automaton<DiffAutomatonStateProperty, DiffProperty<T>> implements Cloneable {
    @Override
    public boolean isInitial(DiffAutomatonStateProperty property) {
        Preconditions.checkNotNull(property, "Expected a non-null state property.");
        return property.isInitial();
    }

    @Override
    public boolean isAccepting(DiffAutomatonStateProperty property) {
        Preconditions.checkNotNull(property, "Expected a non-null state property.");
        return property.isAccepting();
    }

    /**
     * Returns the difference kind associated to the given initial state.
     *
     * @param state The non-{@code null} initial state.
     * @return The associated difference kind.
     */
    public DiffKind getInitialStateDiffKind(State<DiffAutomatonStateProperty> state) {
        Preconditions.checkArgument(isInitialState(state), "Expected an initial state.");
        return state.getProperty().getInitDiffKind();
    }

    @Override
    public void setStateProperty(State<DiffAutomatonStateProperty> state, DiffAutomatonStateProperty property) {
        Preconditions.checkNotNull(property, "Expected a non-null state property.");

        // Make sure that the difference kinds are consistent.
        DiffKind propertyDiffKind = property.getStateDiffKind();

        getIncomingTransitions(state).forEach(transition -> Preconditions.checkArgument(
                propertyDiffKind == transition.getProperty().getDiffKind() || propertyDiffKind == DiffKind.UNCHANGED,
                "Expected the state difference kind to be consistent with all incoming transitions."));
        getOutgoingTransitions(state).forEach(transition -> Preconditions.checkArgument(
                propertyDiffKind == transition.getProperty().getDiffKind() || propertyDiffKind == DiffKind.UNCHANGED,
                "Expected the state difference kind to be consistent with all outgoing transitions."));

        super.setStateProperty(state, property);
    }

    @Override
    public void addTransition(State<DiffAutomatonStateProperty> source, DiffProperty<T> property,
            State<DiffAutomatonStateProperty> target)
    {
        Preconditions.checkNotNull(property, "Expected a non-null transition property.");

        // Make sure that the difference kinds are consistent.
        DiffKind sourceDiffKind = source.getProperty().getStateDiffKind();
        DiffKind targetDiffKind = target.getProperty().getStateDiffKind();
        DiffKind propertyDiffKind = property.getDiffKind();

        Preconditions.checkArgument(sourceDiffKind == propertyDiffKind || sourceDiffKind == DiffKind.UNCHANGED,
                "Expected the difference kind of the transition property to be consistent with the source state.");
        Preconditions.checkArgument(targetDiffKind == propertyDiffKind || targetDiffKind == DiffKind.UNCHANGED,
                "Expected the difference kind of the transition property to be consistent with the target state.");

        super.addTransition(source, property, target);
    }

    @Override
    public DiffAutomaton<T> clone() {
        return map(DiffAutomaton::new, property -> property, property -> property);
    }

    /**
     * Projects this difference automaton along a given difference kind.
     *
     * @param projector The projector for projecting inner transition properties.
     * @param along The non-{@code null} difference kind to project along.
     * @return The projected difference automaton, containing only state and transition properties related to
     *     {@code along}.
     */
    public DiffAutomaton<T> project(Projector<T, DiffKind> projector, DiffKind along) {
        DiffKindProjector diffKindProjector = new DiffKindProjector();
        return project(DiffAutomaton::new, new DiffAutomatonStatePropertyProjector<>(diffKindProjector),
                new DiffPropertyProjector<>(projector, diffKindProjector), along);
    }

    /**
     * Returns the left (LHS) projection of this difference automaton.
     *
     * @param projector The projector for projecting inner transition properties.
     * @return The left (LHS) projection of this difference automaton, containing only the states, initial states and
     *     transitions that are {@link DiffKind#REMOVED}.
     */
    public DiffAutomaton<T> projectLeft(Projector<T, DiffKind> projector) {
        return project(projector, DiffKind.REMOVED);
    }

    /**
     * Returns the right (RHS) projection of this difference automaton.
     *
     * @param projector The projector for projecting inner transition properties.
     * @return The right (RHS) projection of this difference automaton, containing only the states, initial states and
     *     transitions that are {@link DiffKind#ADDED}.
     */
    public DiffAutomaton<T> projectRight(Projector<T, DiffKind> projector) {
        return project(projector, DiffKind.ADDED);
    }

    /**
     * Converts this difference automaton to a simple automaton with potentially different transition properties.
     *
     * @param <U> The target type of transition properties.
     * @param transitionPropertyMapper A function for mapping transition properties. Any transition with a property that
     *     is mapped to {@code null} will not be included in the returned simple automaton.
     * @return The non-{@code null} converted simple automaton.
     */
    public <U> SimpleAutomaton<U> toSimple(Function<T, U> transitionPropertyMapper) {
        return map(SimpleAutomaton::new,
                stateProperty -> new AutomatonStateProperty(stateProperty.isInitial(), stateProperty.isAccepting()),
                transitionProperty -> transitionPropertyMapper.apply(transitionProperty.getProperty()));
    }

    /**
     * Converts a given simple automaton to a difference automaton.
     *
     * @param <T> The type of transition properties.
     * @param automaton The non-{@code null} automaton to convert.
     * @param diffKind The non-{@code null} difference kind to be associated to all converted states, initial states and
     *     transitions.
     * @return The non-{@code null} converted difference automaton.
     */
    public static <T> DiffAutomaton<T> from(SimpleAutomaton<T> automaton, DiffKind diffKind) {
        Preconditions.checkNotNull(automaton, "Expected a non-null automaton.");
        Preconditions.checkNotNull(diffKind, "Expected a non-null difference kind.");

        return automaton.map(DiffAutomaton::new,
                stateProperty -> new DiffAutomatonStateProperty(stateProperty.isAccepting(), diffKind,
                        stateProperty.isInitial() ? Optional.of(diffKind) : Optional.empty()),
                transitionProperty -> new DiffProperty<>(transitionProperty, diffKind));
    }
}
