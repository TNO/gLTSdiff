//////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2021-2023 Contributors to the GitHub community
//
// This program and the accompanying materials are made available
// under the terms of the MIT License which is available at
// https://opensource.org/licenses/MIT
//
// SPDX-License-Identifier: MIT
//////////////////////////////////////////////////////////////////////////////

package com.github.tno.gltsdiff.glts.lts.automaton.diff;

import com.github.tno.gltsdiff.glts.State;
import com.github.tno.gltsdiff.glts.lts.automaton.BaseAutomaton;
import com.google.common.base.Preconditions;

/**
 * A base class for difference automata, {@link BaseAutomaton automata} with difference information for states, initial
 * states and transitions.
 *
 * <p>
 * Difference automata maintain the invariant that difference kinds are always properly nested, meaning that:
 * </p>
 * <ul>
 * <li>State/transition properties that are {@link DiffKind#ADDED added} cannot contain nested properties that are
 * {@link DiffKind#REMOVED removed} or {@link DiffKind#UNCHANGED unchanged}.</li>
 * <li>State/transition properties that are {@link DiffKind#REMOVED removed} cannot contain nested properties that are
 * {@link DiffKind#ADDED added} or {@link DiffKind#UNCHANGED unchanged}.</li>
 * </ul>
 *
 * @param <S> The type of difference automaton state properties.
 * @param <T> The type of transition properties.
 */
public abstract class BaseDiffAutomaton<S extends DiffAutomatonStateProperty, T>
        extends BaseAutomaton<S, DiffProperty<T>>
{
    @Override
    public void setStateProperty(State<S> state, S property) {
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
    public void addTransition(State<S> source, DiffProperty<T> property, State<S> target) {
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
}
