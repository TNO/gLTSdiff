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

import com.github.tno.gltsdiff.glts.LTSStateProperty;

/**
 * A combiner for {@link LTSStateProperty LTS state properties}. Such properties are always combinable. Combining any
 * two such properties results in an LTS state property with combined initial state information.
 */
public class LTSStatePropertyCombiner extends Combiner<LTSStateProperty> {
    @Override
    protected boolean computeAreCombinable(LTSStateProperty left, LTSStateProperty right) {
        return true;
    }

    @Override
    protected LTSStateProperty computeCombination(LTSStateProperty left, LTSStateProperty right) {
        return new LTSStateProperty(left.isInitial() || right.isInitial());
    }
}
