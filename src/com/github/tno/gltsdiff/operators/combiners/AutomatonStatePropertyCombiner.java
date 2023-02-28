//////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2021-2023 Contributors to the GitHub community
//
// This program and the accompanying materials are made available
// under the terms of the MIT License which is available at
// https://opensource.org/licenses/MIT
//
// SPDX-License-Identifier: MIT
//////////////////////////////////////////////////////////////////////////////

package com.github.tno.gltsdiff.operators.combiners;

import com.github.tno.gltsdiff.glts.AutomatonStateProperty;

/**
 * A combiner for {@link AutomatonStateProperty automaton state properties}. Any two such properties can be combined if
 * they agree on states being initial and accepting (i.e., either both states are initial and accepting or both states
 * are not initial and not accepting). Combining two such properties results in an automaton state property with their
 * equal initial state and state acceptance information.
 */
public class AutomatonStatePropertyCombiner extends Combiner<AutomatonStateProperty> {
    @Override
    protected boolean computeAreCombinable(AutomatonStateProperty left, AutomatonStateProperty right) {
        return left.isInitial() == right.isInitial() && left.isAccepting() == right.isAccepting();
    }

    @Override
    protected AutomatonStateProperty computeCombination(AutomatonStateProperty left, AutomatonStateProperty right) {
        return new AutomatonStateProperty(left.isInitial(), left.isAccepting());
    }
}
