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

import com.github.tno.gltsdiff.lts.AutomatonStateProperty;
import com.github.tno.gltsdiff.lts.SimpleAutomaton;
import com.github.tno.gltsdiff.lts.State;
import com.github.tno.gltsdiff.matchers.Matcher;
import com.github.tno.gltsdiff.matchers.scorers.SimilarityScorer;
import com.github.tno.gltsdiff.matchers.scorers.WalkinshawGlobalScorer;
import com.github.tno.gltsdiff.operators.combiners.EqualityCombiner;

public abstract class MatcherTest {
    public abstract <T> Matcher<AutomatonStateProperty, T, SimpleAutomaton<T>> newMatcher(SimpleAutomaton<T> lhs,
            SimpleAutomaton<T> rhs, SimilarityScorer<AutomatonStateProperty, T, SimpleAutomaton<T>> scoring);

    @Test
    public void testSmallThreeStateExample() {
        // Obtain stub automata.
        Pair<SimpleAutomaton<String>, SimpleAutomaton<String>> automata = StubAutomata.smallThreeStateExample();
        SimpleAutomaton<String> lhs = automata.getFirst();
        SimpleAutomaton<String> rhs = automata.getSecond();

        // Perform state matching.
        Map<State<AutomatonStateProperty>, State<AutomatonStateProperty>> matching = newMatcher(lhs, rhs,
                new WalkinshawGlobalScorer<>(lhs, rhs, new EqualityCombiner<>())).compute();

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

    @Test
    public void testSmallExampleWalkinshaw() {
        // Obtain stub automata.
        Pair<SimpleAutomaton<String>, SimpleAutomaton<String>> automata = StubAutomata.smallExampleWalkinshaw();
        SimpleAutomaton<String> lhs = automata.getFirst();
        SimpleAutomaton<String> rhs = automata.getSecond();

        // Perform state matching.
        Map<State<AutomatonStateProperty>, State<AutomatonStateProperty>> matching = newMatcher(lhs, rhs,
                new WalkinshawGlobalScorer<>(lhs, rhs, new EqualityCombiner<>())).compute();

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

    @Test
    public void testRunningExampleWalkinshaw() {
        // Obtain stub automata.
        Pair<SimpleAutomaton<String>, SimpleAutomaton<String>> automata = StubAutomata.runningExampleWalkinshaw();
        SimpleAutomaton<String> lhs = automata.getFirst();
        SimpleAutomaton<String> rhs = automata.getSecond();

        // Perform state matching.
        Map<State<AutomatonStateProperty>, State<AutomatonStateProperty>> matching = newMatcher(lhs, rhs,
                new WalkinshawGlobalScorer<>(lhs, rhs, new EqualityCombiner<>())).compute();

        // State name abbreviations (as they appear in the paper).
        State<?> A = lhs.getStateById(0);
        State<?> B = lhs.getStateById(1);
        State<?> C = lhs.getStateById(2);
        State<?> D = lhs.getStateById(3);
        State<?> E = rhs.getStateById(0);
        State<?> F = rhs.getStateById(1);
        State<?> H = rhs.getStateById(3);
        State<?> I = rhs.getStateById(4);

        // Expected matchings.
        assertEquals(4, matching.size());
        assertEquals(E, matching.get(A));
        assertEquals(F, matching.get(B));
        assertEquals(H, matching.get(C));
        assertEquals(I, matching.get(D));
    }

    @Test
    public void testIndustrialExample1() {
        // Obtain stub automata.
        Pair<SimpleAutomaton<String>, SimpleAutomaton<String>> automata = StubAutomata.industrialExample1();
        SimpleAutomaton<String> lhs = automata.getFirst();
        SimpleAutomaton<String> rhs = automata.getSecond();

        // Perform state matching.
        Map<State<AutomatonStateProperty>, State<AutomatonStateProperty>> matching = newMatcher(lhs, rhs,
                new WalkinshawGlobalScorer<>(lhs, rhs, new EqualityCombiner<>())).compute();

        // State name abbreviations.
        State<?> L1 = lhs.getStateById(0);
        State<?> L2 = lhs.getStateById(1);
        State<?> L3 = lhs.getStateById(2);
        State<?> L4 = lhs.getStateById(3);
        State<?> R1 = rhs.getStateById(0);
        State<?> R2 = rhs.getStateById(1);

        // Expected matchings.
        assertEquals(2, matching.size());
        assertEquals(R1, matching.get(L1));
        assertEquals(R2, matching.get(L2));
        assertTrue(!matching.containsKey(L3));
        assertTrue(!matching.containsKey(L4));
    }

    @Test
    public void testIndustrialExample2() {
        // Obtain stub automata.
        Pair<SimpleAutomaton<String>, SimpleAutomaton<String>> automata = StubAutomata.industrialExample2();
        SimpleAutomaton<String> lhs = automata.getFirst();
        SimpleAutomaton<String> rhs = automata.getSecond();

        // Perform state matching.
        Map<State<AutomatonStateProperty>, State<AutomatonStateProperty>> matching = newMatcher(lhs, rhs,
                new WalkinshawGlobalScorer<>(lhs, rhs, new EqualityCombiner<>())).compute();

        // State name abbreviations.
        State<?> L4 = lhs.getStateById(0);
        State<?> L5 = lhs.getStateById(1);
        State<?> L6 = lhs.getStateById(2);
        State<?> L8 = lhs.getStateById(4);
        State<?> L13 = lhs.getStateById(9);
        State<?> L14 = lhs.getStateById(10);
        State<?> L15 = lhs.getStateById(11);
        State<?> R4 = rhs.getStateById(3);
        State<?> R5 = rhs.getStateById(4);
        State<?> R6 = rhs.getStateById(5);
        State<?> R8 = rhs.getStateById(6);
        State<?> R13 = rhs.getStateById(11);
        State<?> R14 = rhs.getStateById(12);
        State<?> R15 = rhs.getStateById(13);

        // Expected matchings.
        assertEquals(7, matching.size());
        assertEquals(R4, matching.get(L4));
        assertEquals(R5, matching.get(L5));
        assertEquals(R6, matching.get(L6));
        assertEquals(R8, matching.get(L8));
        assertEquals(R13, matching.get(L13));
        assertEquals(R14, matching.get(L14));
        assertEquals(R15, matching.get(L15));
    }
}
