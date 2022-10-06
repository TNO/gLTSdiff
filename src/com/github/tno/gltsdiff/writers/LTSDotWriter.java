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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.github.tno.gltsdiff.lts.LTS;
import com.github.tno.gltsdiff.lts.State;
import com.github.tno.gltsdiff.lts.Transition;
import com.github.tno.gltsdiff.operators.printers.HtmlPrinter;
import com.google.common.base.Preconditions;

/**
 * Functionality for writing {@link LTS}s in DOT format.
 *
 * @param <S> The type of state properties.
 * @param <T> The type of transition properties.
 * @param <U> The type of LTSs to be written.
 */
public abstract class LTSDotWriter<S, T, U extends LTS<S, T>> {
    static final String DEFAULT_COLOR = "#000000";

    static final String DEFAULT_STYLE = "";

    static final String SHAPE_CIRCLE = "circle";

    /** The LTS to be written. */
    protected final U lts;

    /** A printer for printing state labels. */
    protected final HtmlPrinter<State<S>> stateLabelPrinter;

    /** A printer for printing transition labels. */
    protected final HtmlPrinter<Transition<S, T>> transitionLabelPrinter;

    /**
     * Instantiates a writer for the given LTS, which uses {@link #stateLabel} to construct state labels.
     * 
     * @param lts The LTS to be written.
     * @param transitionLabelPrinter A printer for printing transition labels.
     */
    public LTSDotWriter(U lts, HtmlPrinter<Transition<S, T>> transitionLabelPrinter) {
        this(lts, LTSDotWriter::stateLabel, transitionLabelPrinter);
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
        this.lts = lts;
        this.stateLabelPrinter = stateLabelPrinter;
        this.transitionLabelPrinter = transitionLabelPrinter;
    }

    /**
     * Writes the enclosed LTS in DOT format to the provided output stream.
     * 
     * @param stream Stream to output DOT data to.
     * @throws IOException In case of an I/O error.
     */
    public void write(OutputStream stream) throws IOException {
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(stream))) {
            // Open graph scope.
            writer.write("digraph lts {");
            writer.write(System.lineSeparator());

            // Write all states.
            for (State<S> state: sortStates(lts.getStates())) {
                writeState(writer, state);
            }

            // Write all initial state arrows.
            for (State<S> state: sortStates(lts.getInitialStates())) {
                writeInitialTransition(writer, state);
            }

            // Write all transitions.
            for (State<S> source: sortStates(lts.getStates())) {
                int index = 0;
                for (Transition<S, T> transition: lts.getOutgoingTransitions(source)) {
                    writeTransition(writer, transition, index++);
                }
            }

            // Close graph scope.
            writer.append("}");
        }
    }

    /**
     * Gives the DOT graph identifier of the specified state.
     * 
     * @param state The state for which to obtain the DOT graph identifier.
     * @return The DOT graph identifier that identifies {@code state} in the DOT graph.
     */
    public static String stateId(State<?> state) {
        return Integer.toString(state.getId() + 1);
    }

    /**
     * Gives the DOT graph identifier of the specified transition within {@link #lts}.
     * 
     * @param transition The transition for which to obtain the DOT graph identifier.
     * @param index The transition index.
     * @return The DOT graph identifier that identifies {@code transition} in the DOT graph.
     */
    protected String transitionId(Transition<S, T> transition, int index) {
        State<S> source = transition.getSource();
        State<S> target = transition.getTarget();

        Preconditions.checkArgument(lts.hasState(source), "Expected the source state to exist in the given LTS.");
        Preconditions.checkArgument(lts.hasState(target), "Expected the target state to exist in the given LTS.");
        Preconditions.checkArgument(index >= 0, "Expected the given index to be non-negative.");

        return String.format("%s-%d-%s", stateId(source), index, stateId(target));
    }

    /**
     * Gives a standard DOT graph state label for the specified state.
     * 
     * @param state The state for which to obtain the DOT graph state label.
     * @return The DOT graph state label for {@code state}.
     */
    public static String stateLabel(State<?> state) {
        return "s" + stateId(state);
    }

    protected String stateShape(State<S> state) {
        return SHAPE_CIRCLE;
    }

    protected String stateColor(State<S> state) {
        return DEFAULT_COLOR;
    }

    protected String stateFontColor(State<S> state) {
        return DEFAULT_COLOR;
    }

    protected String stateStyle(State<S> state) {
        return DEFAULT_STYLE;
    }

    protected String initialStateColor(State<S> state) {
        return DEFAULT_COLOR;
    }

    protected String transitionColor(Transition<S, T> transition) {
        return DEFAULT_COLOR;
    }

    private void writeState(Writer writer, State<S> state) throws IOException {
        String stateId = stateId(state);
        writer.write("\t");
        writer.write(stateId);
        writer.write(" [");
        writer.write(String.format("label=<%s>", stateLabelPrinter.print(state)));
        optionalWrite(" shape=\"%s\"", stateShape(state), writer);
        optionalWrite(" fillcolor=\"%s\"", skipDefaultColor(stateColor(state)), writer);
        optionalWrite(" fontcolor=\"%s\"", skipDefaultColor(stateFontColor(state)), writer);
        optionalWrite(" style=\"%s\"", stateStyle(state), writer);
        writer.write("];");
        writer.write(System.lineSeparator());
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

    private void writeTransition(Writer writer, Transition<S, T> transition, int index) throws IOException {
        writer.write(String.format("\t%s -> %s [", stateId(transition.getSource()), stateId(transition.getTarget())));
        writer.write(String.format("label=<%s>", transitionLabelPrinter.print(transition)));
        optionalWrite(" color=\"%s\"", skipDefaultColor(transitionColor(transition)), writer);
        writer.write(String.format(" id=\"%s\"", transitionId(transition, index)));
        writer.write("];");
        writer.write(System.lineSeparator());
    }

    private String skipDefaultColor(String color) {
        return DEFAULT_COLOR.equals(color) ? "" : color;
    }

    /**
     * Optionally writes the String.format(${template}, ${templateData}) to the writer. Writes only occur when the
     * trimmed value of ${templateData} is non empty.
     */
    private void optionalWrite(String template, String templateData, Writer writer) throws IOException {
        if (!templateData.trim().isEmpty()) {
            writer.write(String.format(template, templateData));
        }
    }

    /**
     * Given a collection of states, constructs a list of states that is deterministically ordered. This is for example
     * needed for regression testing, which expects deterministic results on every run.
     * 
     * @param states The states to order.
     * @return A list of deterministically ordered states.
     */
    private List<State<S>> sortStates(Collection<State<S>> states) {
        return states.stream().sorted(Comparator.comparing(State::getId)).collect(Collectors.toList());
    }
}
