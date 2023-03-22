//////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2021-2023 Contributors to the GitHub community
//
// This program and the accompanying materials are made available
// under the terms of the MIT License which is available at
// https://opensource.org/licenses/MIT
//
// SPDX-License-Identifier: MIT
//////////////////////////////////////////////////////////////////////////////

package com.github.tno.gltsdiff.matchers.scorers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.util.Pair;
import org.junit.jupiter.api.Test;

import com.github.tno.gltsdiff.StubAutomata;
import com.github.tno.gltsdiff.glts.SimpleAutomaton;
import com.github.tno.gltsdiff.operators.combiners.EqualityCombiner;

public class WalkinshawGlobalScorerTest extends WalkinshawScorerTest {
    @Test
    public void testSmallThreeStateExample() {
        // Obtain stub automata.
        Pair<SimpleAutomaton<String>, SimpleAutomaton<String>> automata = StubAutomata.smallThreeStateExample();
        SimpleAutomaton<String> lhs = automata.getFirst();
        SimpleAutomaton<String> rhs = automata.getSecond();

        // Compute state pair scores.
        SimilarityScorer<?, ?, ?> scorer = new WalkinshawGlobalScorer<>(lhs, rhs, new EqualityCombiner<>());
        RealMatrix scores = scorer.compute();

        // State name abbreviations.
        int a = 0;
        int b = 1;
        int c = 0;
        int d = 1;
        int e = 2;

        // Earlier observations, for the sake of regression testing.
        assertEquals(0.41d, roundToTwoDecimals(scores.getEntry(a, c)));
        assertEquals(0d, roundToTwoDecimals(scores.getEntry(a, d)));
        assertEquals(0.17d, roundToTwoDecimals(scores.getEntry(a, e)));
        assertEquals(0d, roundToTwoDecimals(scores.getEntry(b, c)));
        assertEquals(0.53d, roundToTwoDecimals(scores.getEntry(b, d)));
        assertEquals(0d, roundToTwoDecimals(scores.getEntry(b, e)));
    }

    @Test
    public void testSmallExampleWalkinshaw() {
        // Obtain stub automata.
        Pair<SimpleAutomaton<String>, SimpleAutomaton<String>> automata = StubAutomata.smallExampleWalkinshaw();
        SimpleAutomaton<String> lhs = automata.getFirst();
        SimpleAutomaton<String> rhs = automata.getSecond();

        // Compute state pair scores.
        SimilarityScorer<?, ?, ?> scorer = new WalkinshawGlobalScorer<>(lhs, rhs, new EqualityCombiner<>());
        RealMatrix scores = scorer.compute();

        // State names, as they appear in the paper.
        int a = 0;
        int b = 1;
        int c = 2;
        int e = 0;
        int f = 1;

        // Check whether the scores match the expected scores as reported in the paper (Table 5, Page 16).
        // The first score 0.43d is slightly different from the paper, due to accounting for initial state properties.
        assertEquals(0.43d, roundToTwoDecimals(scores.getEntry(a, e)));
        assertEquals(0.1d, roundToTwoDecimals(scores.getEntry(a, f)));
        assertEquals(0.18d, roundToTwoDecimals(scores.getEntry(b, e)));
        assertEquals(0.34d, roundToTwoDecimals(scores.getEntry(b, f)));
        assertEquals(0.0d, roundToTwoDecimals(scores.getEntry(c, e)));
        assertEquals(0.08d, roundToTwoDecimals(scores.getEntry(c, f))); // Slightly different from paper.
    }

    @Test
    public void testRunningExampleWalkinshaw() {
        // Obtain stub automata.
        Pair<SimpleAutomaton<String>, SimpleAutomaton<String>> automata = StubAutomata.runningExampleWalkinshaw();
        SimpleAutomaton<String> lhs = automata.getFirst();
        SimpleAutomaton<String> rhs = automata.getSecond();

        // Compute state pair scores.
        SimilarityScorer<?, ?, ?> scorer = new WalkinshawGlobalScorer<>(lhs, rhs, new EqualityCombiner<>());
        RealMatrix scores = scorer.compute();

        // State names, as they appear in the paper.
        int A = 0;
        int B = 1;
        int C = 2;
        int D = 3;
        int E = 0;
        int F = 1;
        int H = 3;
        int I = 4;

        // Note: the scores reported in the paper (Table B, page 42) seem to make zero sense at all. Because of that, I
        // added some earlier observations, just for the sake of regression testing.

        assertEquals(0.58d, roundToTwoDecimals(scores.getEntry(A, E)));
        assertEquals(0.55d, roundToTwoDecimals(scores.getEntry(B, F)));
        assertEquals(0.34d, roundToTwoDecimals(scores.getEntry(C, H)));
        assertEquals(0.67d, roundToTwoDecimals(scores.getEntry(D, I)));
    }

    @Test
    public void testIndustrialExample1() {
        // Obtain stub automata.
        Pair<SimpleAutomaton<String>, SimpleAutomaton<String>> automata = StubAutomata.industrialExample1();
        SimpleAutomaton<String> lhs = automata.getFirst();
        SimpleAutomaton<String> rhs = automata.getSecond();

        // Compute state pair scores.
        SimilarityScorer<?, ?, ?> scorer = new WalkinshawGlobalScorer<>(lhs, rhs, new EqualityCombiner<>());
        RealMatrix scores = scorer.compute();

        // State name abbreviations.
        int L1 = 0;
        int L2 = 1;
        int L3 = 2;
        int L4 = 3;
        int R1 = 0;
        int R2 = 1;

        // These assertions are based on previous observations, for the sake of regression testing.
        assertEquals(0.5d, roundToTwoDecimals(scores.getEntry(L1, R1)));
        assertEquals(0d, roundToTwoDecimals(scores.getEntry(L1, R2)));
        assertEquals(0d, roundToTwoDecimals(scores.getEntry(L2, R1)));
        assertEquals(0.33d, roundToTwoDecimals(scores.getEntry(L2, R2)));
        assertEquals(0d, roundToTwoDecimals(scores.getEntry(L3, R1)));
        assertEquals(0d, roundToTwoDecimals(scores.getEntry(L3, R2)));
        assertEquals(0d, roundToTwoDecimals(scores.getEntry(L4, R1)));
        assertEquals(0.33d, roundToTwoDecimals(scores.getEntry(L4, R2)));
    }

    @Test
    public void testSwappedThreeStateLoopExample() {
        // Obtain stub automata.
        Pair<SimpleAutomaton<String>, SimpleAutomaton<String>> automata = StubAutomata
                .smallThreeStateLoopWithSwappedEvents();
        SimpleAutomaton<String> lhs = automata.getFirst();
        SimpleAutomaton<String> rhs = automata.getSecond();

        // Compute state pair scores.
        RealMatrix scores = new WalkinshawGlobalScorer<>(lhs, rhs, new EqualityCombiner<>()).compute();

        // Earlier observations, for the sake of regression testing.
        assertEquals(0.33d, roundToTwoDecimals(scores.getEntry(0, 0)));
        assertEquals(0d, roundToTwoDecimals(scores.getEntry(0, 1)));
        assertEquals(0.13d, roundToTwoDecimals(scores.getEntry(0, 2)));
        assertEquals(0d, roundToTwoDecimals(scores.getEntry(1, 0)));
        assertEquals(0.28d, roundToTwoDecimals(scores.getEntry(1, 1)));
        assertEquals(0.25d, roundToTwoDecimals(scores.getEntry(1, 2)));
        assertEquals(0.13d, roundToTwoDecimals(scores.getEntry(2, 0)));
        assertEquals(0.25d, roundToTwoDecimals(scores.getEntry(2, 1)));
        assertEquals(0d, roundToTwoDecimals(scores.getEntry(2, 2)));
    }
}
