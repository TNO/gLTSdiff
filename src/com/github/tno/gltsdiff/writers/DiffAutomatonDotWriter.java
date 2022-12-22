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

import com.github.tno.gltsdiff.glts.DiffAutomaton;
import com.github.tno.gltsdiff.glts.DiffAutomatonStateProperty;
import com.github.tno.gltsdiff.glts.DiffKind;
import com.github.tno.gltsdiff.glts.DiffProperty;
import com.github.tno.gltsdiff.glts.State;
import com.github.tno.gltsdiff.glts.Transition;
import com.github.tno.gltsdiff.operators.printers.DiffKindHtmlPrinter;
import com.github.tno.gltsdiff.operators.printers.HtmlPrinter;
import com.google.common.base.Preconditions;

/**
 * Functionality for writing {@link DiffAutomaton difference automata} in DOT format.
 *
 * @param <T> The type of transition properties.
 * @param <U> The type of difference automata to be written.
 */
public class DiffAutomatonDotWriter<T, U extends DiffAutomaton<T>>
        extends AutomatonDotWriter<DiffAutomatonStateProperty, DiffProperty<T>, U>
{
    static final String CONTRAST_COLOR = "#ffffff";

    static final String STYLE_FILLED = "filled";

    /** A printer for printing the colors of difference kinds. */
    private final HtmlPrinter<DiffKind> diffKindColorPrinter = new DiffKindHtmlPrinter();

    /**
     * Instantiates a writer for the given difference automaton, which prints state identifiers as state labels.
     * 
     * @param automaton The automaton to be written.
     * @param transitionLabelPrinter A printer for printing transition labels.
     */
    public DiffAutomatonDotWriter(U automaton, HtmlPrinter<DiffProperty<T>> transitionLabelPrinter) {
        super(automaton, transition -> transitionLabelPrinter.print(transition.getProperty()));
    }

    /**
     * Instantiates a writer for the given difference automaton.
     * 
     * @param automaton The automaton to be written.
     * @param stateLabelPrinter A printer for printing state labels.
     * @param transitionLabelPrinter A printer for printing transition labels.
     */
    public DiffAutomatonDotWriter(U automaton, HtmlPrinter<State<DiffAutomatonStateProperty>> stateLabelPrinter,
            HtmlPrinter<DiffProperty<T>> transitionLabelPrinter)
    {
        super(automaton, stateLabelPrinter, transition -> transitionLabelPrinter.print(transition.getProperty()));
    }

    @Override
    protected String stateStyle(State<DiffAutomatonStateProperty> state) {
        switch (state.getProperty().getStateDiffKind()) {
            case ADDED:
            case REMOVED:
                return STYLE_FILLED;
            case UNCHANGED:
                return super.stateStyle(state);
            default:
                throw new RuntimeException("Unknown difference kind.");
        }
    }

    @Override
    protected String stateColor(State<DiffAutomatonStateProperty> state) {
        return diffKindColorPrinter.print(state.getProperty().getStateDiffKind());
    }

    @Override
    protected String stateFontColor(State<DiffAutomatonStateProperty> state) {
        switch (state.getProperty().getStateDiffKind()) {
            case ADDED:
            case REMOVED:
                return CONTRAST_COLOR;
            case UNCHANGED:
                return DEFAULT_COLOR;
            default:
                throw new RuntimeException("Unknown difference kind.");
        }
    }

    @Override
    protected String transitionColor(Transition<DiffAutomatonStateProperty, DiffProperty<T>> transition) {
        return diffKindColorPrinter.print(transition.getProperty().getDiffKind());
    }

    @Override
    protected String initialStateColor(State<DiffAutomatonStateProperty> initialState) {
        Preconditions.checkArgument(glts.isInitialState(initialState), "Expected an initial state.");
        return diffKindColorPrinter.print(initialState.getProperty().getInitDiffKind());
    }
}
