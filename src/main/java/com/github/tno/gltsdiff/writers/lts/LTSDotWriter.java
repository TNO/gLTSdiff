//////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2021-2023 Contributors to the GitHub community
//
// This program and the accompanying materials are made available
// under the terms of the MIT License which is available at
// https://opensource.org/licenses/MIT
//
// SPDX-License-Identifier: MIT
//////////////////////////////////////////////////////////////////////////////

package com.github.tno.gltsdiff.writers.lts;

import java.io.IOException;
import java.io.Writer;
import java.util.Comparator;

import com.github.tno.gltsdiff.glts.State;
import com.github.tno.gltsdiff.glts.Transition;
import com.github.tno.gltsdiff.glts.lts.BaseLTS;
import com.github.tno.gltsdiff.glts.lts.LTSStateProperty;
import com.github.tno.gltsdiff.operators.printers.HtmlPrinter;
import com.github.tno.gltsdiff.writers.DotWriter;

/**
 * Writer for writing {@link BaseLTS LTSs} in DOT format.
 *
 * @param <S> The type of LTS state properties.
 * @param <T> The type of transition properties.
 * @param <U> The type of LTSs to be written.
 */
public class LTSDotWriter<S extends LTSStateProperty, T, U extends BaseLTS<S, T>> extends DotWriter<S, T, U> {
    /**
     * Instantiates a writer for LTSs, which prints state identifiers as state labels.
     *
     * @param transitionLabelPrinter A printer for printing transition labels.
     */
    public LTSDotWriter(HtmlPrinter<Transition<S, T>> transitionLabelPrinter) {
        super(transitionLabelPrinter);
    }

    /**
     * Instantiates a writer for the LTSs.
     *
     * @param stateLabelPrinter A printer for printing state labels.
     * @param transitionLabelPrinter A printer for printing transition labels.
     */
    public LTSDotWriter(HtmlPrinter<State<S>> stateLabelPrinter, HtmlPrinter<Transition<S, T>> transitionLabelPrinter) {
        super(stateLabelPrinter, transitionLabelPrinter);
    }

    @Override
    protected String getDigraphName() {
        return "lts";
    }

    @Override
    protected Comparator<State<S>> getStateComparator(U lts) {
        return Comparator
                // First compare initial state information (descending order: first true, then false).
                .comparing((State<S> state) -> !state.getProperty().isInitial())
                // Then compare states in the default way.
                .thenComparing(super.getStateComparator(lts));
    }

    @Override
    protected void writeTransitions(U lts, Writer writer) throws IOException {
        // Write all initial state arrows.
        for (State<S> state: sortStates(lts, lts.getInitialStates())) {
            writeInitialTransition(lts, writer, state);
        }

        // Write all transitions.
        super.writeTransitions(lts, writer);
    }

    /**
     * Writes information of a single initial transition of an LTS in DOT format to the provided writer.
     *
     * @param lts The LTS.
     * @param writer The writer to write DOT data to.
     * @param initialTransition The initial transition to write in DOT format.
     * @throws IOException In case of an I/O error.
     */
    private void writeInitialTransition(U lts, Writer writer, State<S> initialTransition) throws IOException {
        String initialTransitionId = stateId(initialTransition);
        writer.write(
                String.format("\t__init%s [label=<> shape=\"none\" width=\"0\" height=\"0\"];", initialTransitionId));
        writer.write(System.lineSeparator());

        writer.write(String.format("\t__init%s -> %s", initialTransitionId, initialTransitionId));
        optionalWrite(" [color=\"%s\"]", skipDefaultColor(initialStateColor(lts, initialTransition)), writer);
        writer.write(";");
        writer.write(System.lineSeparator());
    }

    /**
     * Returns the color of the given initial state.
     *
     * @param glts The GLTS.
     * @param state The state of the GLTS.
     * @return The initial state color.
     */
    protected String initialStateColor(U glts, State<S> state) {
        return DEFAULT_COLOR;
    }
}
