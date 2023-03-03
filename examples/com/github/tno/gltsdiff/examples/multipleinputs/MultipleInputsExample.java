//////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2022-2023 Contributors to the GitHub community
//
// This program and the accompanying materials are made available
// under the terms of the MIT License which is available at
// https://opensource.org/licenses/MIT
//
// SPDX-License-Identifier: MIT
//////////////////////////////////////////////////////////////////////////////

package com.github.tno.gltsdiff.examples.multipleinputs;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.commons.math3.util.Pair;

import com.github.tno.gltsdiff.StructureComparator;
import com.github.tno.gltsdiff.glts.AutomatonStateProperty;
import com.github.tno.gltsdiff.glts.SimpleAutomaton;
import com.github.tno.gltsdiff.glts.State;
import com.github.tno.gltsdiff.glts.Transition;
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
import com.github.tno.gltsdiff.writers.AutomatonDotWriter;
import com.google.common.collect.ImmutableSet;

/** Example that compares and merges multiple inputs. */
public class MultipleInputsExample {
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

        // Instantiate combiners for the states and transitions of the three input automata.
        Combiner<AutomatonStateProperty> statePropertyCombiner = new AutomatonStatePropertyCombiner();
        Combiner<Pair<String, Set<Integer>>> transitionPropertyCombiner = new PairCombiner<>(new EqualityCombiner<>(),
                new SetCombiner<>(new EqualityCombiner<>()));

        // Define a helper function to (more) easily compare the three automata.
        StructureComparator<AutomatonStateProperty, Pair<String, Set<Integer>>, SimpleAutomaton<Pair<String, Set<Integer>>>> comparator = new StructureComparator<>(
                new BruteForceLTSMatcher<>(statePropertyCombiner, transitionPropertyCombiner),
                new DefaultMerger<>(statePropertyCombiner, transitionPropertyCombiner, SimpleAutomaton::new));

        // Apply structural comparison to the three input automata.
        SimpleAutomaton<Pair<String, Set<Integer>>> result = Stream.of(first, second, third).reduce(comparator::compare)
                .get();

        // Prepare DOT (HTML) printers for printing the result.
        HtmlPrinter<Set<Integer>> versionSetPrinter = new SetHtmlPrinter<>(new StringHtmlPrinter<>(), "", ",", "");
        HtmlPrinter<Pair<String, Set<Integer>>> transitionPropertyPrinter = pair -> pair.getFirst() + "<br/>" + "{"
                + versionSetPrinter.print(pair.getSecond()) + "}";
        HtmlPrinter<Transition<AutomatonStateProperty, Pair<String, Set<Integer>>>> printer = transition -> transitionPropertyPrinter
                .print(transition.getProperty());

        // Write the result to the console, in DOT format.
        new AutomatonDotWriter<>(printer).write(result, System.out);
    }
}
