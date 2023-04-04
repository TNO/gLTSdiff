//////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2021-2023 Contributors to the GitHub community
//
// This program and the accompanying materials are made available
// under the terms of the MIT License which is available at
// https://opensource.org/licenses/MIT
//
// SPDX-License-Identifier: MIT
//////////////////////////////////////////////////////////////////////////////

package com.github.tno.gltsdiff.matchers.scorers.lts;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.util.Pair;
import org.junit.jupiter.api.Test;

import com.github.tno.gltsdiff.TestAutomata;
import com.github.tno.gltsdiff.glts.lts.automaton.Automaton;
import com.github.tno.gltsdiff.glts.lts.automaton.AutomatonStateProperty;
import com.github.tno.gltsdiff.matchers.scorers.WalkinshawScorerTest;
import com.github.tno.gltsdiff.operators.combiners.EqualityCombiner;
import com.github.tno.gltsdiff.operators.combiners.lts.automaton.AutomatonStatePropertyCombiner;
import com.github.tno.gltsdiff.scorers.SimilarityScorer;
import com.github.tno.gltsdiff.scorers.lts.WalkinshawGlobalLTSScorer;

/** {@link WalkinshawGlobalLTSScorer} tests. */
public class WalkinshawGlobalLTSScorerTest extends WalkinshawScorerTest {
    /** Test {@link TestAutomata#smallTwoAndThreeStatesExample}. */
    @Test
    public void testSmallTwoAndThreeStatesExample() {
        // Obtain test automata.
        Pair<Automaton<AutomatonStateProperty, String>, Automaton<AutomatonStateProperty, String>> automata = TestAutomata
                .smallTwoAndThreeStatesExample();
        Automaton<AutomatonStateProperty, String> lhs = automata.getFirst();
        Automaton<AutomatonStateProperty, String> rhs = automata.getSecond();

        // Compute state pair scores.
        SimilarityScorer<AutomatonStateProperty, String, Automaton<AutomatonStateProperty, String>> scorer = new WalkinshawGlobalLTSScorer<>(
                new AutomatonStatePropertyCombiner(), new EqualityCombiner<>());
        RealMatrix scores = scorer.compute(lhs, rhs);

        // State name abbreviations.
        int a = 0;
        int b = 1;
        int c = 0;
        int d = 1;
        int e = 2;

        // Earlier observations, for the sake of regression testing.
        assertEquals(0.36d, roundToTwoDecimals(scores.getEntry(a, c)));
        assertEquals(Double.NEGATIVE_INFINITY, roundToTwoDecimals(scores.getEntry(a, d)));
        assertEquals(Double.NEGATIVE_INFINITY, roundToTwoDecimals(scores.getEntry(a, e)));
        assertEquals(Double.NEGATIVE_INFINITY, roundToTwoDecimals(scores.getEntry(b, c)));
        assertEquals(0.38d, roundToTwoDecimals(scores.getEntry(b, d)));
        assertEquals(0d, roundToTwoDecimals(scores.getEntry(b, e)));
    }

    /** Test {@link TestAutomata#smallWalkinshawExample}. */
    @Test
    public void testSmallWalkinshawExample() {
        // Obtain test automata.
        Pair<Automaton<AutomatonStateProperty, String>, Automaton<AutomatonStateProperty, String>> automata = TestAutomata
                .smallWalkinshawExample();
        Automaton<AutomatonStateProperty, String> lhs = automata.getFirst();
        Automaton<AutomatonStateProperty, String> rhs = automata.getSecond();

        // Compute state pair scores.
        SimilarityScorer<AutomatonStateProperty, String, Automaton<AutomatonStateProperty, String>> scorer = new WalkinshawGlobalLTSScorer<>(
                new AutomatonStatePropertyCombiner(), new EqualityCombiner<>());
        RealMatrix scores = scorer.compute(lhs, rhs);

        // State names, as they appear in the TOSEM'13 paper of Walkinshaw et al.
        int a = 0;
        int b = 1;
        int c = 2;
        int e = 0;
        int f = 1;

        // Earlier observations, for the sake of regression testing. Scores are different from the TOSEM'13 paper.
        assertEquals(0.42d, roundToTwoDecimals(scores.getEntry(a, e)));
        assertEquals(Double.NEGATIVE_INFINITY, roundToTwoDecimals(scores.getEntry(a, f)));
        assertEquals(Double.NEGATIVE_INFINITY, roundToTwoDecimals(scores.getEntry(b, e)));
        assertEquals(0.28d, roundToTwoDecimals(scores.getEntry(b, f)));
        assertEquals(Double.NEGATIVE_INFINITY, roundToTwoDecimals(scores.getEntry(c, e)));
        assertEquals(0.03d, roundToTwoDecimals(scores.getEntry(c, f)));
    }

    /** Test {@link TestAutomata#runningExampleWalkinshaw}. */
    @Test
    public void testRunningExampleWalkinshaw() {
        // Obtain test automata.
        Pair<Automaton<AutomatonStateProperty, String>, Automaton<AutomatonStateProperty, String>> automata = TestAutomata
                .runningExampleWalkinshaw();
        Automaton<AutomatonStateProperty, String> lhs = automata.getFirst();
        Automaton<AutomatonStateProperty, String> rhs = automata.getSecond();

        // Compute state pair scores.
        SimilarityScorer<AutomatonStateProperty, String, Automaton<AutomatonStateProperty, String>> scorer = new WalkinshawGlobalLTSScorer<>(
                new AutomatonStatePropertyCombiner(), new EqualityCombiner<>());
        RealMatrix scores = scorer.compute(lhs, rhs);

        // State names, as they appear in the TOSEM'13 paper of Walkinshaw et al.
        int sA = 0;
        int sB = 1;
        int sC = 2;
        int sD = 3;
        int sE = 0;
        int sF = 1;
        int sH = 3;
        int sI = 4;

        // Earlier observations, for the sake of regression testing. Scores are different from the TOSEM'13 paper.
        assertEquals(0.58d, roundToTwoDecimals(scores.getEntry(sA, sE)));
        assertEquals(0.55d, roundToTwoDecimals(scores.getEntry(sB, sF)));
        assertEquals(0.34d, roundToTwoDecimals(scores.getEntry(sC, sH)));
        assertEquals(0.67d, roundToTwoDecimals(scores.getEntry(sD, sI)));
    }

    /** Test {@link TestAutomata#industrialExample1}. */
    @Test
    public void testIndustrialExample1() {
        // Obtain test automata.
        Pair<Automaton<AutomatonStateProperty, String>, Automaton<AutomatonStateProperty, String>> automata = TestAutomata
                .industrialExample1();
        Automaton<AutomatonStateProperty, String> lhs = automata.getFirst();
        Automaton<AutomatonStateProperty, String> rhs = automata.getSecond();

        // Compute state pair scores.
        SimilarityScorer<AutomatonStateProperty, String, Automaton<AutomatonStateProperty, String>> scorer = new WalkinshawGlobalLTSScorer<>(
                new AutomatonStatePropertyCombiner(), new EqualityCombiner<>());
        RealMatrix scores = scorer.compute(lhs, rhs);

        // State name abbreviations.
        int sL1 = 0;
        int sL2 = 1;
        int sL3 = 2;
        int sL4 = 3;
        int sR1 = 0;
        int sR2 = 1;

        // Earlier observations, for the sake of regression testing.
        assertEquals(0.5d, roundToTwoDecimals(scores.getEntry(sL1, sR1)));
        assertEquals(Double.NEGATIVE_INFINITY, roundToTwoDecimals(scores.getEntry(sL1, sR2)));
        assertEquals(Double.NEGATIVE_INFINITY, roundToTwoDecimals(scores.getEntry(sL2, sR1)));
        assertEquals(0.33d, roundToTwoDecimals(scores.getEntry(sL2, sR2)));
        assertEquals(Double.NEGATIVE_INFINITY, roundToTwoDecimals(scores.getEntry(sL3, sR1)));
        assertEquals(0d, roundToTwoDecimals(scores.getEntry(sL3, sR2)));
        assertEquals(Double.NEGATIVE_INFINITY, roundToTwoDecimals(scores.getEntry(sL4, sR1)));
        assertEquals(0.33d, roundToTwoDecimals(scores.getEntry(sL4, sR2)));
    }

    /** Test {@link TestAutomata#smallThreeStateLoopWithSwappedEvents}. */
    @Test
    public void testSmallThreeStateLoopWithSwappedEvents() {
        // Obtain test automata.
        Pair<Automaton<AutomatonStateProperty, String>, Automaton<AutomatonStateProperty, String>> automata = TestAutomata
                .smallThreeStateLoopWithSwappedEvents();
        Automaton<AutomatonStateProperty, String> lhs = automata.getFirst();
        Automaton<AutomatonStateProperty, String> rhs = automata.getSecond();

        // Compute state pair scores.
        SimilarityScorer<AutomatonStateProperty, String, Automaton<AutomatonStateProperty, String>> scorer = new WalkinshawGlobalLTSScorer<>(
                new AutomatonStatePropertyCombiner(), new EqualityCombiner<>());
        RealMatrix scores = scorer.compute(lhs, rhs);

        // Earlier observations, for the sake of regression testing.
        assertEquals(0.33d, roundToTwoDecimals(scores.getEntry(0, 0)));
        assertEquals(Double.NEGATIVE_INFINITY, roundToTwoDecimals(scores.getEntry(0, 1)));
        assertEquals(Double.NEGATIVE_INFINITY, roundToTwoDecimals(scores.getEntry(0, 2)));
        assertEquals(Double.NEGATIVE_INFINITY, roundToTwoDecimals(scores.getEntry(1, 0)));
        assertEquals(0.28d, roundToTwoDecimals(scores.getEntry(1, 1)));
        assertEquals(0.1d, roundToTwoDecimals(scores.getEntry(1, 2)));
        assertEquals(Double.NEGATIVE_INFINITY, roundToTwoDecimals(scores.getEntry(2, 0)));
        assertEquals(0.1d, roundToTwoDecimals(scores.getEntry(2, 1)));
        assertEquals(0d, roundToTwoDecimals(scores.getEntry(2, 2)));
    }
}
