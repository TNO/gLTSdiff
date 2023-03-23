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

import com.github.tno.gltsdiff.TestAutomata;
import com.github.tno.gltsdiff.glts.lts.automaton.AutomatonStateProperty;
import com.github.tno.gltsdiff.glts.lts.automaton.SimpleAutomaton;
import com.github.tno.gltsdiff.operators.combiners.AutomatonStatePropertyCombiner;
import com.github.tno.gltsdiff.operators.combiners.EqualityCombiner;

/** {@link WalkinshawLocalLTSScorer} tests. */
public class WalkinshawLocalLTSScorerTest extends WalkinshawScorerTest {
    /** Test {@link TestAutomata#smallWalkinshawExample} with one refinement. */
    @Test
    public void testSmallWalkinshawExampleWithOneRefinement() {
        // Obtain test automata.
        Pair<SimpleAutomaton<String>, SimpleAutomaton<String>> automata = TestAutomata.smallWalkinshawExample();
        SimpleAutomaton<String> lhs = automata.getFirst();
        SimpleAutomaton<String> rhs = automata.getSecond();

        // Compute local state similarity scores.
        SimilarityScorer<AutomatonStateProperty, String, SimpleAutomaton<String>> scorer = new WalkinshawLocalLTSScorer<>(
                new AutomatonStatePropertyCombiner(), new EqualityCombiner<>(), 1);
        RealMatrix scores = scorer.compute(lhs, rhs);

        // State names, as they appear in the TOSEM'13 paper of Walkinshaw et al. (Figure 3).
        int a = 0;
        int b = 1;
        int c = 2;
        int e = 0;
        int f = 1;

        // Check whether the scores are as expected.
        assertEquals(0.38d, roundToTwoDecimals(scores.getEntry(a, e)));
        assertEquals(Double.NEGATIVE_INFINITY, roundToTwoDecimals(scores.getEntry(a, f)));
        assertEquals(Double.NEGATIVE_INFINITY, roundToTwoDecimals(scores.getEntry(b, e)));
        assertEquals(0.29d, roundToTwoDecimals(scores.getEntry(b, f)));
        assertEquals(Double.NEGATIVE_INFINITY, roundToTwoDecimals(scores.getEntry(c, e)));
        assertEquals(0.06d, roundToTwoDecimals(scores.getEntry(c, f)));
    }

    /** Test {@link TestAutomata#smallWalkinshawExample} with two refinements. */
    @Test
    public void testSmallWalkinshawExampleWithTwoRefinements() {
        // Obtain test automata.
        Pair<SimpleAutomaton<String>, SimpleAutomaton<String>> automata = TestAutomata.smallWalkinshawExample();
        SimpleAutomaton<String> lhs = automata.getFirst();
        SimpleAutomaton<String> rhs = automata.getSecond();

        // Compute local state similarity scores.
        SimilarityScorer<AutomatonStateProperty, String, SimpleAutomaton<String>> scorer = new WalkinshawLocalLTSScorer<>(
                new AutomatonStatePropertyCombiner(), new EqualityCombiner<>(), 2);
        RealMatrix scores = scorer.compute(lhs, rhs);

        // State names, as they appear in the TOSEM'13 paper of Walkinshaw et al. (Figure 3).
        int a = 0;
        int b = 1;
        int c = 2;
        int e = 0;
        int f = 1;

        // Check whether the scores are as expected.
        assertEquals(0.42d, roundToTwoDecimals(scores.getEntry(a, e)));
        assertEquals(Double.NEGATIVE_INFINITY, roundToTwoDecimals(scores.getEntry(a, f)));
        assertEquals(Double.NEGATIVE_INFINITY, roundToTwoDecimals(scores.getEntry(b, e)));
        assertEquals(0.28d, roundToTwoDecimals(scores.getEntry(b, f)));
        assertEquals(Double.NEGATIVE_INFINITY, roundToTwoDecimals(scores.getEntry(c, e)));
        assertEquals(0.03d, roundToTwoDecimals(scores.getEntry(c, f)));
    }
}
