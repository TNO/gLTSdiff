//////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2021-2023 Contributors to the GitHub community
//
// This program and the accompanying materials are made available
// under the terms of the MIT License which is available at
// https://opensource.org/licenses/MIT
//
// SPDX-License-Identifier: MIT
//////////////////////////////////////////////////////////////////////////////

package com.github.tno.gltsdiff.writers.lts.automaton.diff;

import com.github.tno.gltsdiff.glts.State;
import com.github.tno.gltsdiff.glts.Transition;
import com.github.tno.gltsdiff.glts.lts.automaton.diff.BaseDiffAutomaton;
import com.github.tno.gltsdiff.glts.lts.automaton.diff.DiffAutomatonStateProperty;
import com.github.tno.gltsdiff.glts.lts.automaton.diff.DiffKind;
import com.github.tno.gltsdiff.glts.lts.automaton.diff.DiffProperty;
import com.github.tno.gltsdiff.operators.printers.HtmlPrinter;
import com.github.tno.gltsdiff.operators.printers.lts.automaton.diff.DiffKindHtmlPrinter;
import com.github.tno.gltsdiff.writers.lts.automaton.AutomatonDotWriter;
import com.google.common.base.Preconditions;

/**
 * Writer for writing {@link BaseDiffAutomaton difference automata} in DOT format.
 *
 * @param <S> The type of difference automaton transition properties.
 * @param <T> The type of transition properties.
 * @param <U> The type of difference automata to be written.
 */
public class DiffAutomatonDotWriter<S extends DiffAutomatonStateProperty, T, U extends BaseDiffAutomaton<S, T>>
        extends AutomatonDotWriter<S, DiffProperty<T>, U>
{
    /** The contrast color to use. */
    static final String CONTRAST_COLOR = "#ffffff";

    /** The filled style to use. */
    static final String STYLE_FILLED = "filled";

    /** A printer for printing the colors of difference kinds. */
    private final HtmlPrinter<DiffKind> diffKindColorPrinter = new DiffKindHtmlPrinter();

    /**
     * Instantiates a writer for difference automata, which prints state identifiers as state labels.
     *
     * @param transitionLabelPrinter A printer for printing transition labels.
     */
    public DiffAutomatonDotWriter(HtmlPrinter<Transition<S, DiffProperty<T>>> transitionLabelPrinter) {
        super(transitionLabelPrinter);
    }

    /**
     * Instantiates a writer for difference automata.
     *
     * @param stateLabelPrinter A printer for printing state labels.
     * @param transitionLabelPrinter A printer for printing transition labels.
     */
    public DiffAutomatonDotWriter(HtmlPrinter<State<S>> stateLabelPrinter,
            HtmlPrinter<Transition<S, DiffProperty<T>>> transitionLabelPrinter)
    {
        super(stateLabelPrinter, transitionLabelPrinter);
    }

    @Override
    protected String getDigraphName() {
        return "diffautomaton";
    }

    @Override
    protected String initialStateColor(U automaton, State<S> initialState) {
        Preconditions.checkArgument(initialState.getProperty().isInitial(), "Expected an initial state.");
        return diffKindColorPrinter.print(initialState.getProperty().getInitDiffKind());
    }

    @Override
    protected String stateStyle(U automaton, State<S> state) {
        switch (state.getProperty().getStateDiffKind()) {
            case ADDED:
            case REMOVED:
                return STYLE_FILLED;
            case UNCHANGED:
                return super.stateStyle(automaton, state);
            default:
                throw new RuntimeException("Unknown difference kind.");
        }
    }

    @Override
    protected String stateColor(U automaton, State<S> state) {
        return diffKindColorPrinter.print(state.getProperty().getStateDiffKind());
    }

    @Override
    protected String stateFontColor(U automaton, State<S> state) {
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
    protected String transitionColor(U automaton, Transition<S, DiffProperty<T>> transition) {
        return diffKindColorPrinter.print(transition.getProperty().getDiffKind());
    }
}
