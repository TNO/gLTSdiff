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

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import com.github.tno.gltsdiff.builders.lts.automaton.diff.DiffAutomatonStructureComparatorBuilder;
import com.github.tno.gltsdiff.glts.State;
import com.github.tno.gltsdiff.glts.lts.automaton.diff.DiffAutomaton;
import com.github.tno.gltsdiff.glts.lts.automaton.diff.DiffAutomatonStateProperty;
import com.github.tno.gltsdiff.glts.lts.automaton.diff.DiffKind;
import com.github.tno.gltsdiff.glts.lts.automaton.diff.DiffProperty;
import com.github.tno.gltsdiff.operators.hiders.SubstitutionHider;
import com.github.tno.gltsdiff.writers.DotRenderer;

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

        // Configure comparison, merging and writing.
        DiffAutomatonStructureComparatorBuilder<String> builder = new DiffAutomatonStructureComparatorBuilder<>();
        builder.setDiffAutomatonTransitionPropertyHider(new SubstitutionHider<>("[skip]"));
        var comparator = builder.createComparator();
        var writer = builder.createWriter();

        // Write the inputs to files in DOT format, and render them to SVG.
        List<DiffAutomaton<String>> inputs = List.of(first, second);
        for (int i = 0; i < inputs.size(); i++) {
            DiffAutomaton<String> input = inputs.get(i);
            Path dotPath = Paths.get("examples/SimpleDiff/input" + (i + 1) + ".dot");
            writer.write(input, dotPath);
            DotRenderer.renderDot(dotPath);
        }

        // Apply structural comparison to the two input automata.
        DiffAutomaton<String> result = comparator.compare(first, second);

        // Write the result to a file in DOT format, and render it to SVG.
        Path resultDotPath = Paths.get("examples/SimpleDiff/result.dot");
        writer.write(result, resultDotPath);
        Path resultSvgPath = DotRenderer.renderDot(resultDotPath);
        System.out.println("The result is in: " + resultSvgPath);
    }
}
