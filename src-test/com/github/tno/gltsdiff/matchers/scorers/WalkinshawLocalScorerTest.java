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

public class WalkinshawLocalScorerTest extends WalkinshawScorerTest {
    @Test
    public void testSmallExampleWalkinshaw() {
        // Obtain stub automata.
        Pair<SimpleAutomaton<String>, SimpleAutomaton<String>> automata = StubAutomata.smallExampleWalkinshaw();
        SimpleAutomaton<String> lhs = automata.getFirst();
        SimpleAutomaton<String> rhs = automata.getSecond();

        // Compute local state similarity scores.
        SimilarityScorer<?, ?, ?> scorer = new WalkinshawLocalScorer<>(lhs, rhs, new EqualityCombiner<>(), 1);
        RealMatrix scores = scorer.compute();

        // State names, as they appear in the paper (Figure 3).
        int a = 0;
        int b = 1;
        int c = 2;
        int e = 0;
        int f = 1;

        // Check whether the scores are as expected.
        assertEquals(0.38d, roundToTwoDecimals(scores.getEntry(a, e)));
        assertEquals(0.08d, roundToTwoDecimals(scores.getEntry(a, f)));
        assertEquals(0.17d, roundToTwoDecimals(scores.getEntry(b, e)));
        assertEquals(0.29d, roundToTwoDecimals(scores.getEntry(b, f)));
        assertEquals(0.0d, roundToTwoDecimals(scores.getEntry(c, e)));
        assertEquals(0.08d, roundToTwoDecimals(scores.getEntry(c, f)));
    }

    @Test
    public void testSmallExampleWalkinshawWithTwoRefinements() {
        // Obtain stub automata.
        Pair<SimpleAutomaton<String>, SimpleAutomaton<String>> automata = StubAutomata.smallExampleWalkinshaw();
        SimpleAutomaton<String> lhs = automata.getFirst();
        SimpleAutomaton<String> rhs = automata.getSecond();

        // Compute local state similarity scores.
        SimilarityScorer<?, ?, ?> scorer = new WalkinshawLocalScorer<>(lhs, rhs, new EqualityCombiner<>(), 2);
        RealMatrix scores = scorer.compute();

        // State names, as they appear in the paper (Figure 3).
        int a = 0;
        int b = 1;
        int c = 2;
        int e = 0;
        int f = 1;

        // Check whether the scores are as expected.
        assertEquals(0.42d, roundToTwoDecimals(scores.getEntry(a, e)));
        assertEquals(0.1d, roundToTwoDecimals(scores.getEntry(a, f)));
        assertEquals(0.19d, roundToTwoDecimals(scores.getEntry(b, e)));
        assertEquals(0.34d, roundToTwoDecimals(scores.getEntry(b, f)));
        assertEquals(0.0d, roundToTwoDecimals(scores.getEntry(c, e)));
        assertEquals(0.09d, roundToTwoDecimals(scores.getEntry(c, f)));
    }
}
