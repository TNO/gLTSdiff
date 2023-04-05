//////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2021-2023 Contributors to the GitHub community
//
// This program and the accompanying materials are made available
// under the terms of the MIT License which is available at
// https://opensource.org/licenses/MIT
//
// SPDX-License-Identifier: MIT
//////////////////////////////////////////////////////////////////////////////

package com.github.tno.gltsdiff.writers.lts.automaton;

import com.github.tno.gltsdiff.glts.State;
import com.github.tno.gltsdiff.glts.Transition;
import com.github.tno.gltsdiff.glts.lts.automaton.AutomatonStateProperty;
import com.github.tno.gltsdiff.glts.lts.automaton.BaseAutomaton;
import com.github.tno.gltsdiff.operators.printers.HtmlPrinter;
import com.github.tno.gltsdiff.writers.lts.LTSDotWriter;

/**
 * Writer for writing {@link BaseAutomaton automata} in DOT format.
 *
 * @param <S> The type of automaton state properties.
 * @param <T> The type of transition properties.
 * @param <U> The type of automata to be written.
 */
public class AutomatonDotWriter<S extends AutomatonStateProperty, T, U extends BaseAutomaton<S, T>>
        extends LTSDotWriter<S, T, U>
{
    /** The double circle shape style to use. */
    protected static final String SHAPE_DOUBLE_CIRCLE = "doublecircle";

    /**
     * Instantiates a writer for automata, which prints state identifiers as state labels.
     *
     * @param transitionLabelPrinter A printer for printing transition labels.
     */
    public AutomatonDotWriter(HtmlPrinter<Transition<S, T>> transitionLabelPrinter) {
        super(transitionLabelPrinter);
    }

    /**
     * Instantiates a writer for the automata.
     *
     * @param stateLabelPrinter A printer for printing state labels.
     * @param transitionLabelPrinter A printer for printing transition labels.
     */
    public AutomatonDotWriter(HtmlPrinter<State<S>> stateLabelPrinter,
            HtmlPrinter<Transition<S, T>> transitionLabelPrinter)
    {
        super(stateLabelPrinter, transitionLabelPrinter);
    }

    @Override
    protected String getDigraphName() {
        return "automaton";
    }

    @Override
    protected String stateShape(U automaton, State<S> state) {
        return state.getProperty().isAccepting() ? SHAPE_DOUBLE_CIRCLE : super.stateShape(automaton, state);
    }
}
