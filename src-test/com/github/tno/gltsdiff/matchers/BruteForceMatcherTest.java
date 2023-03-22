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
import com.github.tno.gltsdiff.matchers.BruteForceMatcher;
import com.github.tno.gltsdiff.operators.combiners.AutomatonStatePropertyCombiner;
import com.github.tno.gltsdiff.operators.combiners.EqualityCombiner;

public class BruteForceMatcherTest {
    @Test
    public void testSuccessOnSmallInput() {
        // Obtain stub automata.
        Pair<SimpleAutomaton<String>, SimpleAutomaton<String>> automata = StubAutomata
                .smallAutomataForBruteForceTesting();
        SimpleAutomaton<String> lhs = automata.getFirst();
        SimpleAutomaton<String> rhs = automata.getSecond();

        // Apply the brute force matcher.
        Map<State<AutomatonStateProperty>, State<AutomatonStateProperty>> matching = new BruteForceMatcher<>(lhs, rhs,
                new AutomatonStatePropertyCombiner(), new EqualityCombiner<>()).compute();

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
