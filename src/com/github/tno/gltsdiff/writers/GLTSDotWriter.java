//////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2021-2023 Contributors to the GitHub community
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

import com.github.tno.gltsdiff.glts.GLTS;
import com.github.tno.gltsdiff.glts.State;
import com.github.tno.gltsdiff.glts.Transition;
import com.github.tno.gltsdiff.operators.printers.HtmlPrinter;
import com.google.common.base.Preconditions;

/**
 * Functionality for writing {@link GLTS GLTSs} in DOT format.
 *
 * @param <S> The type of state properties.
 * @param <T> The type of transition properties.
 * @param <U> The type of GLTSs to be written.
 */
public class GLTSDotWriter<S, T, U extends GLTS<S, T>> {
    static final String DEFAULT_COLOR = "#000000";

    static final String DEFAULT_STYLE = "";

    static final String SHAPE_CIRCLE = "circle";

    /** A printer for printing state labels. */
    protected final HtmlPrinter<State<S>> stateLabelPrinter;

    /** A printer for printing transition labels. */
    protected final HtmlPrinter<Transition<S, T>> transitionLabelPrinter;

    /**
     * Instantiates a writer for GLTSs, which prints state identifiers as state labels.
     * 
     * @param transitionLabelPrinter A printer for printing transition labels.
     */
    public GLTSDotWriter(HtmlPrinter<Transition<S, T>> transitionLabelPrinter) {
        this(GLTSDotWriter::stateLabel, transitionLabelPrinter);
    }

    /**
     * Instantiates a writer for GLTSs.
     * 
     * @param stateLabelPrinter A printer for printing state labels.
     * @param transitionLabelPrinter A printer for printing transition labels.
     */
    public GLTSDotWriter(HtmlPrinter<State<S>> stateLabelPrinter,
            HtmlPrinter<Transition<S, T>> transitionLabelPrinter)
    {
        this.stateLabelPrinter = stateLabelPrinter;
        this.transitionLabelPrinter = transitionLabelPrinter;
    }

    /**
     * Writes a GLTS in DOT format to the provided output stream.
     * 
     * @param glts The GLTS.
     * @param stream Stream to output DOT data to.
     * @throws IOException In case of an I/O error.
     */
    public void write(U glts, OutputStream stream) throws IOException {
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(stream))) {
            // Open graph scope.
            writer.write("digraph " + getDigraphName() + " {");
            writer.write(System.lineSeparator());

            // Write all states and transitions.
            writeStates(glts, writer);
            writeTransitions(glts, writer);

            // Close graph scope.
            writer.append("}");
        }
    }

    /**
     * @param glts The GLTS.
     * @return The name of the digraph to be written in DOT format.
     */
    protected String getDigraphName() {
        return "glts";
    }

    /**
     * Writes state information of a GLTS in DOT format to the provided writer.
     * 
     * @param glts The GLTS.
     * @param writer The writer to write DOT data to.
     * @throws IOException In case of an I/O error.
     */
    protected void writeStates(U glts, Writer writer) throws IOException {
        for (State<S> state: sortStates(glts, glts.getStates())) {
            writeState(glts, writer, state);
        }
    }

    /**
     * Writes transition information of a GLTS in DOT format to the provided writer.
     * 
     * @param glts The GLTS.
     * @param writer The writer to write DOT data to.
     * @throws IOException In case of an I/O error.
     */
    protected void writeTransitions(U glts, Writer writer) throws IOException {
        for (State<S> source: sortStates(glts, glts.getStates())) {
            int index = 0;
            for (Transition<S, T> transition: glts.getOutgoingTransitions(source)) {
                writeTransition(glts, writer, transition, index++);
            }
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
     * Gives the DOT graph identifier of the specified transition within {@link #glts}.
     * 
     * @param glts The GLTS.
     * @param transition The transition for which to obtain the DOT graph identifier.
     * @param index The transition index.
     * @return The DOT graph identifier that identifies {@code transition} in the DOT graph.
     */
    protected String transitionId(U glts, Transition<S, T> transition, int index) {
        State<S> source = transition.getSource();
        State<S> target = transition.getTarget();

        Preconditions.checkArgument(glts.hasState(source), "Expected the source state to exist in the given GLTS.");
        Preconditions.checkArgument(glts.hasState(target), "Expected the target state to exist in the given GLTS.");
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

    protected String stateShape(U glts, State<S> state) {
        return SHAPE_CIRCLE;
    }

    protected String stateColor(U glts, State<S> state) {
        return DEFAULT_COLOR;
    }

    protected String stateFontColor(U glts, State<S> state) {
        return DEFAULT_COLOR;
    }

    protected String stateStyle(U glts, State<S> state) {
        return DEFAULT_STYLE;
    }

    protected String initialStateColor(U glts, State<S> state) {
        return DEFAULT_COLOR;
    }

    protected String transitionColor(U glts, Transition<S, T> transition) {
        return DEFAULT_COLOR;
    }

    /**
     * @param glts The GLTS.
     * @return A comparator that imposes a deterministic and total order on states.
     */
    protected Comparator<State<S>> getStateComparator(U glts) {
        return Comparator.comparing(State::getId);
    }

    /**
     * Writes information of a single state of a GLTS in DOT format to the provided writer.
     * 
     * @param glts The GLTS.
     * @param writer The writer to write DOT data to.
     * @param state The state to write in DOT format.
     * @throws IOException In case of an I/O error.
     */
    protected void writeState(U glts, Writer writer, State<S> state) throws IOException {
        String stateId = stateId(state);
        writer.write("\t");
        writer.write(stateId);
        writer.write(" [");
        writer.write(String.format("label=<%s>", stateLabelPrinter.print(state)));
        optionalWrite(" shape=\"%s\"", stateShape(glts, state), writer);
        optionalWrite(" fillcolor=\"%s\"", skipDefaultColor(stateColor(glts, state)), writer);
        optionalWrite(" fontcolor=\"%s\"", skipDefaultColor(stateFontColor(glts, state)), writer);
        optionalWrite(" style=\"%s\"", stateStyle(glts, state), writer);
        writer.write("];");
        writer.write(System.lineSeparator());
    }

    /**
     * Writes information of a single transition of a GLTS in DOT format to the provided writer.
     * 
     * @param glts The GLTS.
     * @param writer The writer to write DOT data to.
     * @param transition The transition to write in DOT format.
     * @param index The transition index.
     * @throws IOException In case of an I/O error.
     */
    protected void writeTransition(U glts, Writer writer, Transition<S, T> transition, int index) throws IOException {
        writer.write(String.format("\t%s -> %s [", stateId(transition.getSource()), stateId(transition.getTarget())));
        writer.write(String.format("label=<%s>", transitionLabelPrinter.print(transition)));
        optionalWrite(" color=\"%s\"", skipDefaultColor(transitionColor(glts, transition)), writer);
        writer.write(String.format(" id=\"%s\"", transitionId(glts, transition, index)));
        writer.write("];");
        writer.write(System.lineSeparator());
    }

    /**
     * Gives either the provided {@code color}, or an empty string in case this color represents a default color.
     * 
     * @param color A hex color code.
     * @return Either {@code color}, or the empty string in case {@code color} is a default color.
     */
    protected String skipDefaultColor(String color) {
        return DEFAULT_COLOR.equals(color) ? "" : color;
    }

    /**
     * Optionally writes the String.format(${template}, ${templateData}) to the writer. Writes only occur when the
     * trimmed value of ${templateData} is non empty.
     */
    protected void optionalWrite(String template, String templateData, Writer writer) throws IOException {
        if (!templateData.trim().isEmpty()) {
            writer.write(String.format(template, templateData));
        }
    }

    /**
     * Given a collection of states, constructs a list of states that is deterministically ordered. This is for example
     * needed for regression testing, which expects deterministic results on every run.
     * 
     * @param glts The GLTS.
     * @param states The states to order.
     * @return A list of deterministically ordered states.
     */
    protected List<State<S>> sortStates(U glts, Collection<State<S>> states) {
        return states.stream().sorted(getStateComparator(glts)).collect(Collectors.toList());
    }
}
