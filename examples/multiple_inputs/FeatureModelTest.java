//////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2022-2023 Contributors to the GitHub community
//
// This program and the accompanying materials are made available
// under the terms of the MIT License which is available at
// https://opensource.org/licenses/MIT
//
// SPDX-License-Identifier: MIT
//////////////////////////////////////////////////////////////////////////////

package com.github.tno.gltsdiff.test;

import java.io.IOException;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.stream.Stream;

import org.apache.commons.math3.util.Pair;

import com.github.tno.gltsdiff.StructureComparator;
import com.github.tno.gltsdiff.glts.AutomatonStateProperty;
import com.github.tno.gltsdiff.glts.SimpleAutomaton;
import com.github.tno.gltsdiff.glts.State;
import com.github.tno.gltsdiff.glts.Transition;
import com.github.tno.gltsdiff.matchers.BruteForceMatcher;
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

public class FeatureModelTest {
    public static void main(String[] args) throws IOException {
        // Create the first automaton to compare.
        SimpleAutomaton<Pair<String, Set<Integer>>> first = new SimpleAutomaton<>();
        State<AutomatonStateProperty> f1 = first.addState(false);
        State<AutomatonStateProperty> f2 = first.addState(false);
        State<AutomatonStateProperty> f3 = first.addState(false);
        first.addTransition(f1, Pair.create("a", ImmutableSet.of(1)), f2);
        first.addTransition(f2, Pair.create("b", ImmutableSet.of(1)), f3);
        first.addTransition(f3, Pair.create("c", ImmutableSet.of(1)), f1);

        // Create the second automaton to compare.
        SimpleAutomaton<Pair<String, Set<Integer>>> second = new SimpleAutomaton<>();
        State<AutomatonStateProperty> s1 = second.addState(false);
        State<AutomatonStateProperty> s2 = second.addState(false);
        State<AutomatonStateProperty> s3 = second.addState(false);
        second.addTransition(s1, Pair.create("a", ImmutableSet.of(2)), s2);
        second.addTransition(s2, Pair.create("b", ImmutableSet.of(2)), s3);
        second.addTransition(s3, Pair.create("c", ImmutableSet.of(2)), s1);
        second.addTransition(s3, Pair.create("d", ImmutableSet.of(2)), s1);

        // Create the third automaton to compare.
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
        BinaryOperator<SimpleAutomaton<Pair<String, Set<Integer>>>> compare = (
                left, right
        ) -> new StructureComparator<>(left, right,
                new BruteForceMatcher<>(left, right, statePropertyCombiner, transitionPropertyCombiner),
                new DefaultMerger<>(left, right, statePropertyCombiner, transitionPropertyCombiner,
                        SimpleAutomaton::new)).compare();

        // Apply structural comparison to the three input automata.
        SimpleAutomaton<Pair<String, Set<Integer>>> result = Stream.of(first, second, third).reduce(compare).get();

        // Prepare DOT (HTML) printers for printing the result.
        HtmlPrinter<Set<Integer>> setPrinter = new SetHtmlPrinter<>(new StringHtmlPrinter<>(), (l, r) -> l + "," + r);
        HtmlPrinter<Pair<String, Set<Integer>>> pairPrinter = pair -> pair.getFirst() + "<br/>" + "{"
                + setPrinter.print(pair.getSecond()) + "}";
        HtmlPrinter<Transition<AutomatonStateProperty, Pair<String, Set<Integer>>>> printer = transition -> pairPrinter
                .print(transition.getProperty());

        // Print the result to the console, in DOT format.
        new AutomatonDotWriter<>(result, printer).write(System.out);
    }
}
