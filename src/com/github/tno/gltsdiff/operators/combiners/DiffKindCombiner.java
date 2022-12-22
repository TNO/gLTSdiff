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

import com.github.tno.gltsdiff.glts.DiffKind;

/**
 * A combiner for {@link DiffKind difference kinds}. Difference kinds can always be combined. Combining any two
 * difference kinds results either in {@link DiffKind#UNCHANGED} if the inputs are unequal, or otherwise gives a result
 * that is equal to the input operands.
 */
public class DiffKindCombiner extends Combiner<DiffKind> {
    @Override
    protected boolean computeAreCombinable(DiffKind left, DiffKind right) {
        return true;
    }

    @Override
    protected DiffKind computeCombination(DiffKind left, DiffKind right) {
        return left == right ? left : DiffKind.UNCHANGED;
    }
}
