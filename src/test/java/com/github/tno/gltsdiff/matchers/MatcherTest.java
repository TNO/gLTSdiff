//////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2021-2023 Contributors to the GitHub community
//
// This program and the accompanying materials are made available
// under the terms of the MIT License which is available at
// https://opensource.org/licenses/MIT
//
// SPDX-License-Identifier: MIT
//////////////////////////////////////////////////////////////////////////////

package com.github.tno.gltsdiff.matchers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.apache.commons.math3.util.Pair;
import org.junit.jupiter.api.Test;

import com.github.tno.gltsdiff.TestAutomata;
import com.github.tno.gltsdiff.glts.State;
import com.github.tno.gltsdiff.glts.lts.automaton.Automaton;
import com.github.tno.gltsdiff.glts.lts.automaton.AutomatonStateProperty;
import com.github.tno.gltsdiff.operators.combiners.EqualityCombiner;
import com.github.tno.gltsdiff.operators.combiners.lts.automaton.AutomatonStatePropertyCombiner;
import com.github.tno.gltsdiff.scorers.SimilarityScorer;
import com.github.tno.gltsdiff.scorers.lts.WalkinshawGlobalLTSScorer;

/** Base class for {@link Matcher} tests. */
public abstract class MatcherTest {
    /**
     * Construct the matcher to use for testing.
     *
     * @param <T> The type of transition properties.
     * @param scorer The scorer to use to create scoring-based matchers. May be ignored by non-scoring-based matchers.
     * @return The matcher.
     */
    public abstract <T> Matcher<AutomatonStateProperty, T, Automaton<T>>
            newMatcher(SimilarityScorer<AutomatonStateProperty, T, Automaton<T>> scorer);

    /** Test {@link TestAutomata#smallTwoAndThreeStatesExample}. */
    @Test
    public void testSmallTwoAndThreeStatesExample() {
        // Obtain test automata.
        Pair<Automaton<String>, Automaton<String>> automata = TestAutomata.smallTwoAndThreeStatesExample();
        Automaton<String> lhs = automata.getFirst();
        Automaton<String> rhs = automata.getSecond();

        // Perform state matching.
        Matcher<AutomatonStateProperty, String, Automaton<String>> matcher = newMatcher(
                new WalkinshawGlobalLTSScorer<>(new AutomatonStatePropertyCombiner(), new EqualityCombiner<>()));
        Map<State<AutomatonStateProperty>, State<AutomatonStateProperty>> matching = matcher.compute(lhs, rhs);

        // State abbreviations.
        State<?> l0 = lhs.getStateById(0);
        State<?> l1 = lhs.getStateById(1);
        State<?> r0 = rhs.getStateById(0);
        State<?> r1 = rhs.getStateById(1);
        State<?> r2 = rhs.getStateById(2);

        // There are two possible best matchings for this example. Assert that one of them is chosen.
        boolean matchingCase1 = matching.get(l0) == r0 && matching.get(l1) == r1;
        boolean matchingCase2 = matching.get(l0) == r2 && matching.get(l1) == r1;

        assertEquals(2, matching.size());
        assertTrue(matchingCase1 || matchingCase2);
    }

    /** Test {@link TestAutomata#smallWalkinshawExample}. */
    @Test
    public void testSmallWalkinshawExample() {
        // Obtain test automata.
        Pair<Automaton<String>, Automaton<String>> automata = TestAutomata.smallWalkinshawExample();
        Automaton<String> lhs = automata.getFirst();
        Automaton<String> rhs = automata.getSecond();

        // Perform state matching.
        Matcher<AutomatonStateProperty, String, Automaton<String>> matcher = newMatcher(
                new WalkinshawGlobalLTSScorer<>(new AutomatonStatePropertyCombiner(), new EqualityCombiner<>()));
        Map<State<AutomatonStateProperty>, State<AutomatonStateProperty>> matching = matcher.compute(lhs, rhs);

        // State name abbreviations (as they appear in the paper).
        State<?> a = lhs.getStateById(0);
        State<?> b = lhs.getStateById(1);
        State<?> c = lhs.getStateById(2);
        State<?> e = rhs.getStateById(0);
        State<?> f = rhs.getStateById(1);

        // Expected matchings.
        assertEquals(e, matching.get(a));
        assertEquals(f, matching.get(b));
        assertTrue(!matching.containsKey(c));
    }

    /** Test {@link TestAutomata#runningExampleWalkinshaw}. */
    @Test
    public void testRunningExampleWalkinshaw() {
        // Obtain test automata.
        Pair<Automaton<String>, Automaton<String>> automata = TestAutomata.runningExampleWalkinshaw();
        Automaton<String> lhs = automata.getFirst();
        Automaton<String> rhs = automata.getSecond();

        // Perform state matching.
        Matcher<AutomatonStateProperty, String, Automaton<String>> matcher = newMatcher(
                new WalkinshawGlobalLTSScorer<>(new AutomatonStatePropertyCombiner(), new EqualityCombiner<>()));
        Map<State<AutomatonStateProperty>, State<AutomatonStateProperty>> matching = matcher.compute(lhs, rhs);

        // State name abbreviations (as they appear in the paper).
        State<?> sA = lhs.getStateById(0);
        State<?> sB = lhs.getStateById(1);
        State<?> sC = lhs.getStateById(2);
        State<?> sD = lhs.getStateById(3);
        State<?> sE = rhs.getStateById(0);
        State<?> sF = rhs.getStateById(1);
        State<?> sH = rhs.getStateById(3);
        State<?> sI = rhs.getStateById(4);

        // Expected matchings.
        assertEquals(4, matching.size());
        assertEquals(sE, matching.get(sA));
        assertEquals(sF, matching.get(sB));
        assertEquals(sH, matching.get(sC));
        assertEquals(sI, matching.get(sD));
    }

    /** Test {@link TestAutomata#industrialExample1}. */
    @Test
    public void testIndustrialExample1() {
        // Obtain test automata.
        Pair<Automaton<String>, Automaton<String>> automata = TestAutomata.industrialExample1();
        Automaton<String> lhs = automata.getFirst();
        Automaton<String> rhs = automata.getSecond();

        // Perform state matching.
        Matcher<AutomatonStateProperty, String, Automaton<String>> matcher = newMatcher(
                new WalkinshawGlobalLTSScorer<>(new AutomatonStatePropertyCombiner(), new EqualityCombiner<>()));
        Map<State<AutomatonStateProperty>, State<AutomatonStateProperty>> matching = matcher.compute(lhs, rhs);

        // State name abbreviations.
        State<?> sL1 = lhs.getStateById(0);
        State<?> sL2 = lhs.getStateById(1);
        State<?> sL3 = lhs.getStateById(2);
        State<?> sL4 = lhs.getStateById(3);
        State<?> sR1 = rhs.getStateById(0);
        State<?> sR2 = rhs.getStateById(1);

        // Expected matchings.
        assertEquals(2, matching.size());
        assertEquals(sR1, matching.get(sL1));
        assertEquals(sR2, matching.get(sL2));
        assertTrue(!matching.containsKey(sL3));
        assertTrue(!matching.containsKey(sL4));
    }

    /** Test {@link TestAutomata#industrialExample2}. */
    @Test
    public void testIndustrialExample2() {
        // Obtain test automata.
        Pair<Automaton<String>, Automaton<String>> automata = TestAutomata.industrialExample2();
        Automaton<String> lhs = automata.getFirst();
        Automaton<String> rhs = automata.getSecond();

        // Perform state matching.
        Matcher<AutomatonStateProperty, String, Automaton<String>> matcher = newMatcher(
                new WalkinshawGlobalLTSScorer<>(new AutomatonStatePropertyCombiner(), new EqualityCombiner<>()));
        Map<State<AutomatonStateProperty>, State<AutomatonStateProperty>> matching = matcher.compute(lhs, rhs);

        // State name abbreviations.
        State<?> sL4 = lhs.getStateById(0);
        State<?> sL5 = lhs.getStateById(1);
        State<?> sL6 = lhs.getStateById(2);
        State<?> sL8 = lhs.getStateById(4);
        State<?> sL13 = lhs.getStateById(9);
        State<?> sL14 = lhs.getStateById(10);
        State<?> sL15 = lhs.getStateById(11);
        State<?> sR4 = rhs.getStateById(3);
        State<?> sR5 = rhs.getStateById(4);
        State<?> sR6 = rhs.getStateById(5);
        State<?> sR8 = rhs.getStateById(6);
        State<?> sR13 = rhs.getStateById(11);
        State<?> sR14 = rhs.getStateById(12);
        State<?> sR15 = rhs.getStateById(13);

        // Expected matchings.
        assertEquals(7, matching.size());
        assertEquals(sR4, matching.get(sL4));
        assertEquals(sR5, matching.get(sL5));
        assertEquals(sR6, matching.get(sL6));
        assertEquals(sR8, matching.get(sL8));
        assertEquals(sR13, matching.get(sL13));
        assertEquals(sR14, matching.get(sL14));
        assertEquals(sR15, matching.get(sL15));
    }

    /** Test for state acceptance matching. */
    @Test
    public void testMatchingsShouldAlwaysAgreeOnStateAcceptance() {
        // Obtain the LHS.
        Automaton<String> lhs = new Automaton<>();
        State<AutomatonStateProperty> s1 = lhs.addState(new AutomatonStateProperty(true, true));
        State<AutomatonStateProperty> s2 = lhs.addState(new AutomatonStateProperty(false, true));
        lhs.addTransition(s1, "e1", s2);
        lhs.addTransition(s2, "e2", s1);

        // Obtain the RHS.
        Automaton<String> rhs = new Automaton<>();
        State<AutomatonStateProperty> t1 = rhs.addState(new AutomatonStateProperty(true, true));
        State<AutomatonStateProperty> t2 = rhs.addState(new AutomatonStateProperty(false, false));
        rhs.addTransition(t1, "e1", t2);
        rhs.addTransition(t2, "e2", t1);

        // Compute a matching.
        Matcher<AutomatonStateProperty, String, Automaton<String>> matcher = newMatcher(
                new WalkinshawGlobalLTSScorer<>(new AutomatonStatePropertyCombiner(), new EqualityCombiner<>()));
        Map<State<AutomatonStateProperty>, State<AutomatonStateProperty>> matching = matcher.compute(lhs, rhs);

        // Expected only the initial states to be matched.
        assertEquals(1, matching.size());
        assertEquals(t1, matching.get(s1));
    }
}
