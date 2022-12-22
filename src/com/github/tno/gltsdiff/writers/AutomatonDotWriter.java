//////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2021-2022 Contributors to the GitHub community
//
// This program and the accompanying materials are made available
// under the terms of the MIT License which is available at
// https://opensource.org/licenses/MIT
//
// SPDX-License-Identifier: MIT
//////////////////////////////////////////////////////////////////////////////

package com.github.tno.gltsdiff.writers;

import com.github.tno.gltsdiff.glts.Automaton;
import com.github.tno.gltsdiff.glts.State;
import com.github.tno.gltsdiff.glts.Transition;
import com.github.tno.gltsdiff.operators.printers.HtmlPrinter;

/**
 * Functionality for writing {@link Automaton automata} in DOT format.
 *
 * @param <S> The type of state properties.
 * @param <T> The type of transition properties.
 * @param <U> The type of automata to be written.
 */
public class AutomatonDotWriter<S, T, U extends Automaton<S, T>> extends LTSDotWriter<S, T, U> {
    static final String SHAPE_DOUBLE_CIRCLE = "doublecircle";

    /**
     * Instantiates a writer for the given automaton, which prints state identifiers as state labels.
     * 
     * @param automaton The automaton to be written.
     * @param transitionLabelPrinter A printer for printing transition labels.
     */
    public AutomatonDotWriter(U automaton, HtmlPrinter<Transition<S, T>> transitionLabelPrinter) {
        super(automaton, transitionLabelPrinter);
    }

    /**
     * Instantiates a writer for the given automaton.
     * 
     * @param automaton The automaton to be written.
     * @param stateLabelPrinter A printer for printing state labels.
     * @param transitionLabelPrinter A printer for printing transition labels.
     */
    public AutomatonDotWriter(U automaton, HtmlPrinter<State<S>> stateLabelPrinter,
            HtmlPrinter<Transition<S, T>> transitionLabelPrinter)
    {
        super(automaton, stateLabelPrinter, transitionLabelPrinter);
    }

    @Override
    protected String stateShape(State<S> state) {
        return glts.isAcceptingState(state) ? SHAPE_DOUBLE_CIRCLE : super.stateShape(state);
    }
}
