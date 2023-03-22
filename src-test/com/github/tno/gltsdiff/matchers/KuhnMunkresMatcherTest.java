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

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.util.Pair;
import org.junit.jupiter.api.Test;

import com.github.tno.gltsdiff.TestAutomata;
import com.github.tno.gltsdiff.glts.AutomatonStateProperty;
import com.github.tno.gltsdiff.glts.SimpleAutomaton;
import com.github.tno.gltsdiff.glts.State;
import com.github.tno.gltsdiff.matchers.scorers.FixedScoresScorer;
import com.github.tno.gltsdiff.matchers.scorers.SimilarityScorer;
import com.github.tno.gltsdiff.matchers.scorers.WalkinshawGlobalLTSScorer;
import com.github.tno.gltsdiff.operators.combiners.AutomatonStatePropertyCombiner;
import com.github.tno.gltsdiff.operators.combiners.EqualityCombiner;

/** {@link KuhnMunkresMatcher} tests. */
public class KuhnMunkresMatcherTest extends MatcherTest {
    @Override
    public <T> Matcher<AutomatonStateProperty, T, SimpleAutomaton<T>>
            newMatcher(SimilarityScorer<AutomatonStateProperty, T, SimpleAutomaton<T>> scorer)
    {
        return new KuhnMunkresMatcher<>(scorer, new AutomatonStatePropertyCombiner());
    }

    /** Test {@link TestAutomata#smallThreeStateLoopWithSwappedEvents}. */
    @Test
    public void testPreviouslyNonTerminatingExample() {
        // Obtain LHS and RHS.
        Pair<SimpleAutomaton<String>, SimpleAutomaton<String>> automata = TestAutomata
                .smallThreeStateLoopWithSwappedEvents();
        SimpleAutomaton<String> lhs = automata.getFirst();
        SimpleAutomaton<String> rhs = automata.getSecond();

        // Construct a scores matrix (higher score means better match).
        RealMatrix scores = new Array2DRowRealMatrix(3, 3);
        scores.setRow(0, new double[] {0.25d, 0d, 0.25d});
        scores.setRow(1, new double[] {0d, 0.25d, 0.25d});
        scores.setRow(2, new double[] {0.25d, 0.25d, 0d});

        // Compute a matching based on the scores.
        Matcher<AutomatonStateProperty, String, SimpleAutomaton<String>> matcher = newMatcher(
                new FixedScoresScorer<>(scores));
        Map<State<AutomatonStateProperty>, State<AutomatonStateProperty>> matching = matcher.compute(lhs, rhs);

        // State abbreviations.
        State<?> l0 = lhs.getStateById(0);
        State<?> l1 = lhs.getStateById(1);
        State<?> l2 = lhs.getStateById(2);
        State<?> r0 = rhs.getStateById(0);
        State<?> r1 = rhs.getStateById(1);
        State<?> r2 = rhs.getStateById(2);

        // There are two possible best matchings for this example. Assert that one of them is chosen.
        boolean matchingCase1 = matching.get(l0) == r0 && matching.get(l1) == r2 && matching.get(l2) == r1;
        boolean matchingCase2 = matching.get(l0) == r2 && matching.get(l1) == r1 && matching.get(l2) == r0;

        // Expected matchings.
        assertEquals(3, matching.size());
        assertTrue(matchingCase1 || matchingCase2);
    }

    /** Test for state acceptance matching. */
    @Test
    public void testMatchingsShouldAlwaysAgreeOnStateAcceptance() {
        // Obtain the LHS.
        SimpleAutomaton<String> lhs = new SimpleAutomaton<>();
        State<AutomatonStateProperty> s1 = lhs.addInitialState(true);
        State<AutomatonStateProperty> s2 = lhs.addState(true);
        lhs.addTransition(s1, "e1", s2);
        lhs.addTransition(s2, "e2", s1);

        // Obtain the RHS.
        SimpleAutomaton<String> rhs = new SimpleAutomaton<>();
        State<AutomatonStateProperty> t1 = rhs.addInitialState(true);
        State<AutomatonStateProperty> t2 = rhs.addState(false);
        rhs.addTransition(t1, "e1", t2);
        rhs.addTransition(t2, "e2", t1);

        // Compute a matching.
        Matcher<AutomatonStateProperty, String, SimpleAutomaton<String>> matcher = newMatcher(
                new WalkinshawGlobalLTSScorer<>(new AutomatonStatePropertyCombiner(), new EqualityCombiner<>()));
        Map<State<AutomatonStateProperty>, State<AutomatonStateProperty>> matching = matcher.compute(lhs, rhs);

        // Expected only the initial states to be matched.
        assertEquals(1, matching.size());
        assertEquals(t1, matching.get(s1));
    }
}
