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

import java.io.IOException;
import java.io.Writer;
import java.util.Comparator;

import com.github.tno.gltsdiff.glts.LTS;
import com.github.tno.gltsdiff.glts.State;
import com.github.tno.gltsdiff.glts.Transition;
import com.github.tno.gltsdiff.operators.printers.HtmlPrinter;

/**
 * Functionality for writing {@link LTS LTSs} in DOT format.
 *
 * @param <S> The type of state properties.
 * @param <T> The type of transition properties.
 * @param <U> The type of LTSs to be written.
 */
public class LTSDotWriter<S, T, U extends LTS<S, T>> extends GLTSDotWriter<S, T, U> {
    /**
     * Instantiates a writer for the given LTS, which prints state identifiers as state labels.
     * 
     * @param lts The LTS to be written.
     * @param transitionLabelPrinter A printer for printing transition labels.
     */
    public LTSDotWriter(U lts, HtmlPrinter<Transition<S, T>> transitionLabelPrinter) {
        super(lts, transitionLabelPrinter);
    }

    /**
     * Instantiates a writer for the given LTS.
     * 
     * @param lts The LTS to be written.
     * @param stateLabelPrinter A printer for printing state labels.
     * @param transitionLabelPrinter A printer for printing transition labels.
     */
    public LTSDotWriter(U lts, HtmlPrinter<State<S>> stateLabelPrinter,
            HtmlPrinter<Transition<S, T>> transitionLabelPrinter)
    {
        super(lts, stateLabelPrinter, transitionLabelPrinter);
    }

    @Override
    protected String getDigraphName() {
        return "lts";
    }

    @Override
    protected void writeTransitions(Writer writer) throws IOException {
        // Write all initial state arrows.
        for (State<S> state: sortStates(glts.getInitialStates())) {
            writeInitialTransition(writer, state);
        }

        // Write all transitions.
        super.writeTransitions(writer);
    }

    @Override
    protected Comparator<State<S>> getStateComparator() {
        return Comparator
                // First compare initial state information (descending order: first true, then false).
                .comparing((State<S> state) -> !glts.isInitialState(state))
                // Then compare states in the default way.
                .thenComparing(super.getStateComparator());
    }

    private void writeInitialTransition(Writer writer, State<S> initialTransition) throws IOException {
        String initialTransitionId = stateId(initialTransition);
        writer.write(String.format("\t__init%s [label=<> shape=\"none\"];", initialTransitionId));
        writer.write(System.lineSeparator());

        writer.write(String.format("\t__init%s -> %s", initialTransitionId, initialTransitionId));
        optionalWrite(" [color=\"%s\"]", skipDefaultColor(initialStateColor(initialTransition)), writer);
        writer.write(";");
        writer.write(System.lineSeparator());
    }
}
