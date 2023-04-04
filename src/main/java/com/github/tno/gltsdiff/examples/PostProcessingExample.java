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
import java.util.Collections;
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
 * Example that compares two difference automata and performs post-processing to improve the comparison result.
 *
 * <p>
 * This example demonstrates how to apply gLTSdiff to compare two difference automata, one entirely red and the other
 * entirely green. The result is a difference automaton with their common parts merged (black), while their differences
 * remain (in red and green). As the result has more red and green transitions than needed, post-processing is used to
 * eliminate the unnecessary differences.
 * </p>
 */
public class PostProcessingExample {
    /** Constructor for the {@link PostProcessingExample} class. */
    private PostProcessingExample() {
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
        DiffAutomaton<DiffAutomatonStateProperty, String> first = new DiffAutomaton<>();
        State<DiffAutomatonStateProperty> f1 = first
                .addState(new DiffAutomatonStateProperty(true, DiffKind.REMOVED, Optional.of(DiffKind.REMOVED)));
        State<DiffAutomatonStateProperty> f2 = first
                .addState(new DiffAutomatonStateProperty(false, DiffKind.REMOVED, Optional.empty()));
        first.addTransition(f1, new DiffProperty<>("a", DiffKind.REMOVED), f2);
        first.addTransition(f2, new DiffProperty<>("d", DiffKind.REMOVED), f1);

        // Create the second input automaton to compare.
        DiffAutomaton<DiffAutomatonStateProperty, String> second = new DiffAutomaton<>();
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
        second.addTransition(s3, new DiffProperty<>("c", DiffKind.ADDED), s4);
        second.addTransition(s4, new DiffProperty<>("d", DiffKind.ADDED), s1);

        // Configure comparison, merging and writing, without rewriters for post-processing.
        DiffAutomatonStructureComparatorBuilder<String> builder = new DiffAutomatonStructureComparatorBuilder<>();
        builder.setDiffAutomatonTransitionPropertyHider(new SubstitutionHider<>("[skip]"));
        builder.setRewriters(Collections.emptyList());
        var comparator = builder.createComparator();
        var writer = builder.createWriter();

        // Write the inputs to files in DOT format, and render them to SVG.
        List<DiffAutomaton<DiffAutomatonStateProperty, String>> inputs = List.of(first, second);
        for (int i = 0; i < inputs.size(); i++) {
            DiffAutomaton<DiffAutomatonStateProperty, String> input = inputs.get(i);
            Path dotPath = Paths.get("examples/PostProcessing/input" + (i + 1) + ".dot");
            writer.write(input, dotPath);
            DotRenderer.renderDot(dotPath);
        }

        // Apply structural comparison to the two input automata.
        DiffAutomaton<DiffAutomatonStateProperty, String> result = comparator.compare(first, second);

        // Write the comparison result to a file in DOT format, and render it to SVG.
        Path resultDotPath1 = Paths.get("examples/PostProcessing/result1.dot");
        writer.write(result, resultDotPath1);
        Path resultSvgPath1 = DotRenderer.renderDot(resultDotPath1);
        System.out.println("The comparison result is in: " + resultSvgPath1);

        // Reconfigure comparison, merging and writing, with rewriters for post-processing.
        builder.addDefaultRewriters();
        comparator = builder.createComparator();
        writer = builder.createWriter();

        // Reapply structural comparison to the two input automata.
        result = comparator.compare(first, second);

        // Write the post-processing result to a file in DOT format, and render it to SVG.
        Path resultDotPath2 = Paths.get("examples/PostProcessing/result2.dot");
        writer.write(result, resultDotPath2);
        Path resultSvgPath2 = DotRenderer.renderDot(resultDotPath2);
        System.out.println("The post-processed result is in: " + resultSvgPath2);
    }
}
