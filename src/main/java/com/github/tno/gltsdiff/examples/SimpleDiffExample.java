//////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2022-2023 Contributors to the GitHub community
//
// This program and the accompanying materials are made available
// under the terms of the MIT License which is available at
// https://opensource.org/licenses/MIT
//
// SPDX-License-Identifier: MIT
//////////////////////////////////////////////////////////////////////////////

package com.github.tno.gltsdiff.examples;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import com.github.tno.gltsdiff.StructureComparator;
import com.github.tno.gltsdiff.glts.State;
import com.github.tno.gltsdiff.glts.lts.automaton.diff.DiffAutomaton;
import com.github.tno.gltsdiff.glts.lts.automaton.diff.DiffAutomatonStateProperty;
import com.github.tno.gltsdiff.glts.lts.automaton.diff.DiffKind;
import com.github.tno.gltsdiff.glts.lts.automaton.diff.DiffProperty;
import com.github.tno.gltsdiff.matchers.lts.BruteForceLTSMatcher;
import com.github.tno.gltsdiff.mergers.DefaultMerger;
import com.github.tno.gltsdiff.operators.combiners.Combiner;
import com.github.tno.gltsdiff.operators.combiners.EqualityCombiner;
import com.github.tno.gltsdiff.operators.combiners.lts.automaton.diff.DiffAutomatonStatePropertyCombiner;
import com.github.tno.gltsdiff.operators.combiners.lts.automaton.diff.DiffPropertyCombiner;
import com.github.tno.gltsdiff.operators.printers.HtmlPrinter;
import com.github.tno.gltsdiff.operators.printers.StringHtmlPrinter;
import com.github.tno.gltsdiff.operators.printers.lts.automaton.diff.DiffPropertyHtmlPrinter;
import com.github.tno.gltsdiff.writers.DotRenderer;
import com.github.tno.gltsdiff.writers.lts.automaton.diff.DiffAutomatonDotWriter;

/**
 * Example that compares two difference automata.
 *
 * <p>
 * This example demonstrates how to apply gLTSdiff to compare two difference automata, one entirely red and the other
 * entirely green. The result is a difference automaton with their common parts merged (black), while their differences
 * remain (in red and green).
 * </p>
 */
public class SimpleDiffExample {
    /** Constructor for the {@link SimpleDiffExample} class. */
    private SimpleDiffExample() {
        // Static class.
    }

    /**
     * Main method to run the example.
     *
     * @param args The command line arguments. Are ignored.
     * @throws IOException In case of an I/O error.
     */
    public static void main(String[] args) throws IOException {
        // Create the first input automaton to compare.
        DiffAutomaton<String> first = new DiffAutomaton<>();
        State<DiffAutomatonStateProperty> f1 = first
                .addState(new DiffAutomatonStateProperty(true, DiffKind.REMOVED, Optional.of(DiffKind.REMOVED)));
        State<DiffAutomatonStateProperty> f2 = first
                .addState(new DiffAutomatonStateProperty(false, DiffKind.REMOVED, Optional.empty()));
        State<DiffAutomatonStateProperty> f3 = first
                .addState(new DiffAutomatonStateProperty(false, DiffKind.REMOVED, Optional.empty()));
        first.addTransition(f1, new DiffProperty<>("a", DiffKind.REMOVED), f2);
        first.addTransition(f2, new DiffProperty<>("b", DiffKind.REMOVED), f3);
        first.addTransition(f3, new DiffProperty<>("c", DiffKind.REMOVED), f1);

        // Create the second input automaton to compare.
        DiffAutomaton<String> second = new DiffAutomaton<>();
        State<DiffAutomatonStateProperty> s1 = second
                .addState(new DiffAutomatonStateProperty(true, DiffKind.ADDED, Optional.of(DiffKind.ADDED)));
        State<DiffAutomatonStateProperty> s2 = second
                .addState(new DiffAutomatonStateProperty(false, DiffKind.ADDED, Optional.empty()));
        State<DiffAutomatonStateProperty> s3 = second
                .addState(new DiffAutomatonStateProperty(false, DiffKind.ADDED, Optional.empty()));
        State<DiffAutomatonStateProperty> s4 = second
                .addState(new DiffAutomatonStateProperty(false, DiffKind.ADDED, Optional.empty()));
        second.addTransition(s1, new DiffProperty<>("a", DiffKind.ADDED), s2);
        second.addTransition(s2, new DiffProperty<>("b", DiffKind.ADDED), s3);
        second.addTransition(s3, new DiffProperty<>("c", DiffKind.ADDED), s1);
        second.addTransition(s2, new DiffProperty<>("d", DiffKind.ADDED), s4);
        second.addTransition(s4, new DiffProperty<>("e", DiffKind.ADDED), s1);

        // Get all inputs.
        List<DiffAutomaton<String>> inputs = List.of(first, second);

        // Prepare DOT (HTML) printers for printing difference automata.
        HtmlPrinter<DiffProperty<String>> transitionPropertyPrinter = new DiffPropertyHtmlPrinter<>(
                new StringHtmlPrinter<>());

        // Write the inputs to files in DOT format, and render them to SVG.
        for (int i = 0; i < inputs.size(); i++) {
            DiffAutomaton<String> input = inputs.get(i);
            Path dotPath = Paths.get("examples/SimpleDiff/input" + (i + 1) + ".dot");
            Files.createDirectories(dotPath.getParent());
            try (OutputStream stream = new BufferedOutputStream(new FileOutputStream(dotPath.toFile()))) {
                new DiffAutomatonDotWriter<>(transitionPropertyPrinter).write(input, stream);
            }
            DotRenderer.renderDot(dotPath);
        }

        // Instantiate combiners for the states and transitions of the input automata.
        Combiner<DiffAutomatonStateProperty> statePropertyCombiner = new DiffAutomatonStatePropertyCombiner();
        Combiner<DiffProperty<String>> transitionPropertyCombiner = new DiffPropertyCombiner<>(
                new EqualityCombiner<>());

        // Define a helper function to (more) easily compare the three automata.
        StructureComparator<DiffAutomatonStateProperty, DiffProperty<String>, DiffAutomaton<String>> comparator = new StructureComparator<>(
                new BruteForceLTSMatcher<>(statePropertyCombiner, transitionPropertyCombiner),
                new DefaultMerger<>(statePropertyCombiner, transitionPropertyCombiner, DiffAutomaton::new));

        // Apply structural comparison to the two input automata.
        DiffAutomaton<String> result = comparator.compare(first, second);

        // Write the result to a file in DOT format, and render it to SVG.
        Path dotPath = Paths.get("examples/SimpleDiff/result.dot");
        try (OutputStream stream = new BufferedOutputStream(new FileOutputStream(dotPath.toFile()))) {
            new DiffAutomatonDotWriter<>(transitionPropertyPrinter).write(result, stream);
        }
        Path svgPath = DotRenderer.renderDot(dotPath);
        System.out.println("The result is in: " + svgPath);
    }
}
