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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

import org.apache.commons.math3.util.Pair;

import com.github.tno.gltsdiff.StructureComparator;
import com.github.tno.gltsdiff.glts.State;
import com.github.tno.gltsdiff.glts.Transition;
import com.github.tno.gltsdiff.glts.lts.automaton.AutomatonStateProperty;
import com.github.tno.gltsdiff.glts.lts.automaton.SimpleAutomaton;
import com.github.tno.gltsdiff.matchers.BruteForceLTSMatcher;
import com.github.tno.gltsdiff.mergers.DefaultMerger;
import com.github.tno.gltsdiff.operators.combiners.AutomatonStatePropertyCombiner;
import com.github.tno.gltsdiff.operators.combiners.Combiner;
import com.github.tno.gltsdiff.operators.combiners.EqualityCombiner;
import com.github.tno.gltsdiff.operators.combiners.PairCombiner;
import com.github.tno.gltsdiff.operators.combiners.SetCombiner;
import com.github.tno.gltsdiff.operators.printers.HtmlPrinter;
import com.github.tno.gltsdiff.operators.printers.SetHtmlPrinter;
import com.github.tno.gltsdiff.operators.printers.StringHtmlPrinter;
import com.github.tno.gltsdiff.utils.DotRenderUtil;
import com.github.tno.gltsdiff.writers.AutomatonDotWriter;
import com.google.common.collect.ImmutableSet;

/**
 * Example that compares and merges multiple inputs.
 *
 * <p>
 * This example demonstrates how to apply gLTSdiff on more than two inputs. For that we use version-annotated GLTSs,
 * which are GLTSs where the transitions are annotated with a version (number). We construct three such models as input.
 * Then we compare the structures of these models, and compute a single model that shows how the three input models
 * relate.
 * </p>
 */
public class MultipleInputsExample {
    /** Constructor for the {@link MultipleInputsExample} class. */
    private MultipleInputsExample() {
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
        SimpleAutomaton<Pair<String, Set<Integer>>> first = new SimpleAutomaton<>();
        State<AutomatonStateProperty> f1 = first.addState(false);
        State<AutomatonStateProperty> f2 = first.addState(false);
        State<AutomatonStateProperty> f3 = first.addState(false);
        first.addTransition(f1, Pair.create("a", ImmutableSet.of(1)), f2);
        first.addTransition(f2, Pair.create("b", ImmutableSet.of(1)), f3);
        first.addTransition(f3, Pair.create("c", ImmutableSet.of(1)), f1);

        // Create the second input automaton to compare.
        SimpleAutomaton<Pair<String, Set<Integer>>> second = new SimpleAutomaton<>();
        State<AutomatonStateProperty> s1 = second.addState(false);
        State<AutomatonStateProperty> s2 = second.addState(false);
        State<AutomatonStateProperty> s3 = second.addState(false);
        second.addTransition(s1, Pair.create("a", ImmutableSet.of(2)), s2);
        second.addTransition(s2, Pair.create("b", ImmutableSet.of(2)), s3);
        second.addTransition(s3, Pair.create("c", ImmutableSet.of(2)), s1);
        second.addTransition(s3, Pair.create("d", ImmutableSet.of(2)), s1);

        // Create the third input automaton to compare.
        SimpleAutomaton<Pair<String, Set<Integer>>> third = new SimpleAutomaton<>();
        State<AutomatonStateProperty> t1 = third.addState(false);
        State<AutomatonStateProperty> t2 = third.addState(false);
        State<AutomatonStateProperty> t3 = third.addState(false);
        third.addTransition(t1, Pair.create("a", ImmutableSet.of(3)), t2);
        third.addTransition(t2, Pair.create("b", ImmutableSet.of(3)), t3);
        third.addTransition(t3, Pair.create("c", ImmutableSet.of(3)), t1);
        third.addTransition(t3, Pair.create("d", ImmutableSet.of(3)), t1);
        third.addTransition(t1, Pair.create("e", ImmutableSet.of(3)), t2);

        // Get all inputs.
        List<SimpleAutomaton<Pair<String, Set<Integer>>>> inputs = List.of(first, second, third);

        // Prepare DOT (HTML) printers for printing version-annotated GLTSs.
        HtmlPrinter<Set<Integer>> versionSetPrinter = new SetHtmlPrinter<>(new StringHtmlPrinter<>(), "", ",", "");
        HtmlPrinter<Pair<String, Set<Integer>>> transitionPropertyPrinter = pair -> pair.getFirst() + "<br/>" + "{"
                + versionSetPrinter.print(pair.getSecond()) + "}";
        HtmlPrinter<Transition<AutomatonStateProperty, Pair<String, Set<Integer>>>> printer = transition -> transitionPropertyPrinter
                .print(transition.getProperty());

        // Write the inputs to files in DOT format, and render them to SVG.
        for (int i = 0; i < inputs.size(); i++) {
            SimpleAutomaton<Pair<String, Set<Integer>>> input = inputs.get(i);
            Path dotPath = Paths.get("examples/multipleinputs/input" + (i + 1) + ".dot");
            try (OutputStream stream = new BufferedOutputStream(new FileOutputStream(dotPath.toFile()))) {
                new AutomatonDotWriter<>(printer).write(input, stream);
            }
            DotRenderUtil.renderDot(dotPath);
        }

        // Instantiate combiners for the states and transitions of the three input automata.
        Combiner<AutomatonStateProperty> statePropertyCombiner = new AutomatonStatePropertyCombiner();
        Combiner<Pair<String, Set<Integer>>> transitionPropertyCombiner = new PairCombiner<>(new EqualityCombiner<>(),
                new SetCombiner<>(new EqualityCombiner<>()));

        // Define a helper function to (more) easily compare the three automata.
        StructureComparator<AutomatonStateProperty, Pair<String, Set<Integer>>, SimpleAutomaton<Pair<String, Set<Integer>>>> comparator = new StructureComparator<>(
                new BruteForceLTSMatcher<>(statePropertyCombiner, transitionPropertyCombiner),
                new DefaultMerger<>(statePropertyCombiner, transitionPropertyCombiner, SimpleAutomaton::new));

        // Apply structural comparison to the three input automata.
        SimpleAutomaton<Pair<String, Set<Integer>>> result = inputs.stream().reduce(comparator::compare).get();

        // Write the result to a file in DOT format, and render it to SVG.
        Path dotPath = Paths.get("examples/multipleinputs/result.dot");
        try (OutputStream stream = new BufferedOutputStream(new FileOutputStream(dotPath.toFile()))) {
            new AutomatonDotWriter<>(printer).write(result, stream);
        }
        Path svgPath = DotRenderUtil.renderDot(dotPath);
        System.out.println("The result is in: " + svgPath);
    }
}
