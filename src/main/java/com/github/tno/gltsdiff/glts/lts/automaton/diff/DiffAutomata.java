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

import java.util.Optional;
import java.util.function.Function;

import com.github.tno.gltsdiff.glts.lts.automaton.Automaton;
import com.github.tno.gltsdiff.glts.lts.automaton.AutomatonStateProperty;
import com.github.tno.gltsdiff.operators.projectors.Projector;
import com.github.tno.gltsdiff.operators.projectors.lts.automaton.diff.DiffAutomatonStatePropertyProjector;
import com.github.tno.gltsdiff.operators.projectors.lts.automaton.diff.DiffKindProjector;
import com.github.tno.gltsdiff.operators.projectors.lts.automaton.diff.DiffPropertyProjector;
import com.google.common.base.Preconditions;

/** Utilities for working with difference automata. */
public class DiffAutomata {
    /** Constructor for the {@link DiffAutomata} class. */
    private DiffAutomata() {
        // Static class.
    }

    /**
     * Projects a difference automaton along a given difference kind.
     *
     * @param <T> The type of the difference automaton transition properties.
     * @param automaton The difference automaton to project.
     * @param projector The projector for projecting difference automaton transition properties.
     * @param along The non-{@code null} difference kind to project along.
     * @return The projected difference automaton, containing only state and transition properties related to
     *     {@code along}.
     */
    public static <T> DiffAutomaton<DiffAutomatonStateProperty, T> project(
            DiffAutomaton<DiffAutomatonStateProperty, T> automaton, Projector<T, DiffKind> projector, DiffKind along)
    {
        DiffKindProjector diffKindProjector = new DiffKindProjector();
        return automaton.project(DiffAutomaton::new, new DiffAutomatonStatePropertyProjector<>(diffKindProjector),
                new DiffPropertyProjector<>(projector, diffKindProjector), along);
    }

    /**
     * Returns the left (LHS) projection of a difference automaton.
     *
     * @param <T> The type of the difference automaton transition properties.
     * @param automaton The difference automaton to project.
     * @param projector The projector for projecting difference automaton transition properties.
     * @return The left (LHS) projection of the difference automaton, containing only the states, initial states and
     *     transitions that are {@link DiffKind#REMOVED}.
     */
    public static <T> DiffAutomaton<DiffAutomatonStateProperty, T>
            projectLeft(DiffAutomaton<DiffAutomatonStateProperty, T> automaton, Projector<T, DiffKind> projector)
    {
        return project(automaton, projector, DiffKind.REMOVED);
    }

    /**
     * Returns the right (RHS) projection of a difference automaton.
     *
     * @param <T> The type of the difference automaton transition properties.
     * @param automaton The difference automaton to project.
     * @param projector The projector for projecting difference automaton transition properties.
     * @return The right (RHS) projection of the difference automaton, containing only the states, initial states and
     *     transitions that are {@link DiffKind#ADDED}.
     */
    public static <T> DiffAutomaton<DiffAutomatonStateProperty, T>
            projectRight(DiffAutomaton<DiffAutomatonStateProperty, T> automaton, Projector<T, DiffKind> projector)
    {
        return project(automaton, projector, DiffKind.ADDED);
    }

    /**
     * Converts a difference automaton to an automaton with potentially different transition properties.
     *
     * @param <D> The type of difference automaton transition properties.
     * @param <A> The type of automaton transition properties.
     * @param automaton The difference automaton to convert.
     * @param transitionPropertyMapper A function for mapping transition properties. Any transition with a property that
     *     is mapped to {@code null} will not be included in the returned automaton.
     * @return The non-{@code null} automaton.
     */
    public static <D, A> Automaton<AutomatonStateProperty, A>
            toAutomaton(DiffAutomaton<DiffAutomatonStateProperty, D> automaton, Function<D, A> transitionPropertyMapper)
    {
        return automaton.map(Automaton::new,
                stateProperty -> new AutomatonStateProperty(stateProperty.isInitial(), stateProperty.isAccepting()),
                transitionProperty -> transitionPropertyMapper.apply(transitionProperty.getProperty()));
    }

    /**
     * Converts an automaton to a difference automaton.
     *
     * @param <T> The type of automaton transition properties.
     * @param automaton The non-{@code null} automaton to convert.
     * @param diffKind The non-{@code null} difference kind to be associated to all converted states, initial states and
     *     transitions.
     * @return The non-{@code null} difference automaton.
     */
    public static <T> DiffAutomaton<DiffAutomatonStateProperty, T>
            fromAutomaton(Automaton<AutomatonStateProperty, T> automaton, DiffKind diffKind)
    {
        Preconditions.checkNotNull(automaton, "Expected a non-null automaton.");
        Preconditions.checkNotNull(diffKind, "Expected a non-null difference kind.");

        return automaton.map(DiffAutomaton::new,
                stateProperty -> new DiffAutomatonStateProperty(stateProperty.isAccepting(), diffKind,
                        stateProperty.isInitial() ? Optional.of(diffKind) : Optional.empty()),
                transitionProperty -> new DiffProperty<>(transitionProperty, diffKind));
    }
}
