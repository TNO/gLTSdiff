//////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2021-2023 Contributors to the GitHub community
//
// This program and the accompanying materials are made available
// under the terms of the MIT License which is available at
// https://opensource.org/licenses/MIT
//
// SPDX-License-Identifier: MIT
//////////////////////////////////////////////////////////////////////////////

package com.github.tno.gltsdiff.matchers.lts;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.apache.commons.math3.util.Pair;
import org.junit.jupiter.api.Test;

import com.github.tno.gltsdiff.TestAutomata;
import com.github.tno.gltsdiff.glts.State;
import com.github.tno.gltsdiff.glts.lts.automaton.Automaton;
import com.github.tno.gltsdiff.glts.lts.automaton.AutomatonStateProperty;
import com.github.tno.gltsdiff.matchers.Matcher;
import com.github.tno.gltsdiff.matchers.MatcherTest;
import com.github.tno.gltsdiff.operators.combiners.EqualityCombiner;
import com.github.tno.gltsdiff.operators.combiners.lts.automaton.AutomatonStatePropertyCombiner;
import com.github.tno.gltsdiff.scorers.SimilarityScorer;

/** {@link BruteForceLTSMatcher} tests. */
public class BruteForceLTSMatcherTest extends MatcherTest {
    @Override
    public <T> Matcher<AutomatonStateProperty, T, Automaton<AutomatonStateProperty, T>>
            newMatcher(SimilarityScorer<AutomatonStateProperty, T, Automaton<AutomatonStateProperty, T>> scorer)
    {
        return new BruteForceLTSMatcher<>(new AutomatonStatePropertyCombiner(), new EqualityCombiner<>());
    }

    /** Test {@link TestAutomata#smallAutomataForBruteForceTesting}. */
    @Test
    public void testSuccessOnSmallInput() {
        // Obtain test automata.
        Pair<Automaton<AutomatonStateProperty, String>, Automaton<AutomatonStateProperty, String>> automata = TestAutomata
                .smallAutomataForBruteForceTesting();
        Automaton<AutomatonStateProperty, String> lhs = automata.getFirst();
        Automaton<AutomatonStateProperty, String> rhs = automata.getSecond();

        // Apply the brute force matcher.
        Matcher<AutomatonStateProperty, String, Automaton<AutomatonStateProperty, String>> matcher = newMatcher(null);
        Map<State<AutomatonStateProperty>, State<AutomatonStateProperty>> matching = matcher.compute(lhs, rhs);

        // State abbreviations.
        State<?> l0 = lhs.getStateById(0);
        State<?> l1 = lhs.getStateById(1);
        State<?> l2 = lhs.getStateById(2);
        State<?> r0 = rhs.getStateById(0);
        State<?> r1 = rhs.getStateById(1);
        State<?> r2 = rhs.getStateById(2);

        // Expected matchings.
        assertEquals(3, matching.size());
        assertTrue(matching.containsKey(l0));
        assertTrue(matching.containsKey(l1));
        assertTrue(matching.containsKey(l2));
        assertTrue(matching.get(l0) == r0);
        assertTrue(matching.get(l1) == r1);
        assertTrue(matching.get(l2) == r2);
    }
}
