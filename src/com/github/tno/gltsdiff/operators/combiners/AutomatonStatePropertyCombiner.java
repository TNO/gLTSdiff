//////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2021-2022 Contributors to the GitHub community
//
// This program and the accompanying materials are made available
// under the terms of the MIT License which is available at
// https://opensource.org/licenses/MIT
//
// SPDX-License-Identifier: MIT
//////////////////////////////////////////////////////////////////////////////

package com.github.tno.gltsdiff.operators.combiners;

import com.github.tno.gltsdiff.lts.AutomatonStateProperty;

/**
 * A combiner for {@link AutomatonStateProperty automaton state properties}. Any two such properties can be combined if
 * they agree on state acceptance (i.e., they either both indicate acceptance or both indicate non-acceptance).
 * Combining two such properties results in an automaton state property with combined initial state information.
 */
public class AutomatonStatePropertyCombiner extends Combiner<AutomatonStateProperty> {
    @Override
    protected boolean computeAreCombinable(AutomatonStateProperty left, AutomatonStateProperty right) {
        return left.isAccepting() == right.isAccepting();
    }

    @Override
    protected AutomatonStateProperty computeCombination(AutomatonStateProperty left, AutomatonStateProperty right) {
        return new AutomatonStateProperty(left.isInitial() || right.isInitial(), left.isAccepting());
    }
}
