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

import com.github.tno.gltsdiff.lts.AutomatonStateProperty;
import com.github.tno.gltsdiff.lts.SimpleAutomaton;
import com.github.tno.gltsdiff.matchers.Matcher;
import com.github.tno.gltsdiff.matchers.WalkinshawMatcher;
import com.github.tno.gltsdiff.matchers.scorers.SimilarityScorer;
import com.github.tno.gltsdiff.operators.combiners.AutomatonStatePropertyCombiner;
import com.github.tno.gltsdiff.operators.combiners.EqualityCombiner;

public class WalkinshawMatcherTest extends MatcherTest {
    @Override
    public <T> Matcher<AutomatonStateProperty, T, SimpleAutomaton<T>> newMatcher(SimpleAutomaton<T> lhs,
            SimpleAutomaton<T> rhs, SimilarityScorer<AutomatonStateProperty, T, SimpleAutomaton<T>> scoring)
    {
        return new WalkinshawMatcher<>(lhs, rhs, scoring, new AutomatonStatePropertyCombiner(),
                new EqualityCombiner<>());
    }
}
