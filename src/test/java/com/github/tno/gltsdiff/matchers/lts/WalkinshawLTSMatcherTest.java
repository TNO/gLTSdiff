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

import com.github.tno.gltsdiff.glts.lts.automaton.Automaton;
import com.github.tno.gltsdiff.glts.lts.automaton.AutomatonStateProperty;
import com.github.tno.gltsdiff.matchers.Matcher;
import com.github.tno.gltsdiff.matchers.MatcherTest;
import com.github.tno.gltsdiff.operators.combiners.EqualityCombiner;
import com.github.tno.gltsdiff.operators.combiners.lts.automaton.AutomatonStatePropertyCombiner;
import com.github.tno.gltsdiff.scorers.SimilarityScorer;

/** {@link WalkinshawLTSMatcher} tests. */
public class WalkinshawLTSMatcherTest extends MatcherTest {
    @Override
    public <T> Matcher<AutomatonStateProperty, T, Automaton<T>>
            newMatcher(SimilarityScorer<AutomatonStateProperty, T, Automaton<T>> scorer)
    {
        return new WalkinshawLTSMatcher<>(scorer, new AutomatonStatePropertyCombiner(), new EqualityCombiner<>());
    }
}
