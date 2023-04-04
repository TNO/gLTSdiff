//////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2023 Contributors to the GitHub community
//
// This program and the accompanying materials are made available
// under the terms of the MIT License which is available at
// https://opensource.org/licenses/MIT
//
// SPDX-License-Identifier: MIT
//////////////////////////////////////////////////////////////////////////////

package com.github.tno.gltsdiff.rewriters.lts.automaton.diff;

import java.util.Optional;

import com.github.tno.gltsdiff.glts.lts.automaton.diff.BaseDiffAutomaton;
import com.github.tno.gltsdiff.glts.lts.automaton.diff.DiffAutomatonStateProperty;
import com.github.tno.gltsdiff.glts.lts.automaton.diff.DiffKind;
import com.github.tno.gltsdiff.glts.lts.automaton.diff.DiffProperty;
import com.github.tno.gltsdiff.rewriters.Rewriter;
import com.github.tno.gltsdiff.utils.TriFunction;

/**
 * Base class for rewriters that rewrite {@link BaseDiffAutomaton difference automata}.
 *
 * @param <S> The type of difference automaton state properties.
 * @param <T> The type of transition properties.
 * @param <U> The type of difference automata to rewrite.
 */
public abstract class DiffAutomatonRewriter<S extends DiffAutomatonStateProperty, T, U extends BaseDiffAutomaton<S, T>>
        implements Rewriter<S, DiffProperty<T>, U>
{
    /**
     * Function to transform a difference automaton state property. Given an existing difference automaton state
     * property, a new state difference kind, and a new initial state difference kind (present if state is an initial
     * state, absent otherwise), returns a difference automaton state property that has the new (initial) state
     * difference kinds. The function should not modify the existing state property.
     */
    protected final TriFunction<S, DiffKind, Optional<DiffKind>, S> statePropertyTransformer;

    /**
     * Instantiates a new difference automaton rewriter.
     *
     * @param statePropertyTransformer Function to transform a difference automaton state property. Given an existing
     *     difference automaton state property, a new state difference kind, and a new initial state difference kind
     *     (present if state is an initial state, absent otherwise), returns a difference automaton state property that
     *     has the new (initial) state difference kinds. The function should not modify the existing state property.
     */
    public DiffAutomatonRewriter(TriFunction<S, DiffKind, Optional<DiffKind>, S> statePropertyTransformer) {
        this.statePropertyTransformer = statePropertyTransformer;
    }
}
