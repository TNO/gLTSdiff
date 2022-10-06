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

import java.util.Optional;

import com.github.tno.gltsdiff.lts.DiffAutomatonStateProperty;
import com.github.tno.gltsdiff.lts.DiffKind;

/**
 * A combiner for {@link DiffAutomatonStateProperty difference automaton state properties}. Any two such properties are
 * combinable if they agree on state acceptance (i.e., they either both indicate acceptance or both indicate
 * non-acceptance). Combining two such properties results in a difference automaton state property with a combined state
 * difference kind, and combined initial state (difference) information.
 */
public class DiffAutomatonStatePropertyCombiner extends Combiner<DiffAutomatonStateProperty> {
    /** The combiner for difference kinds. */
    private final Combiner<DiffKind> diffKindCombiner = new DiffKindCombiner();

    /** The combiner for optional difference kinds. */
    private final Combiner<Optional<DiffKind>> optionalDiffKindCombiner = new OptionalCombiner<>(diffKindCombiner);

    @Override
    protected boolean computeAreCombinable(DiffAutomatonStateProperty left, DiffAutomatonStateProperty right) {
        return left.isAccepting() == right.isAccepting();
    }

    @Override
    protected DiffAutomatonStateProperty computeCombination(DiffAutomatonStateProperty left,
            DiffAutomatonStateProperty right)
    {
        Optional<DiffKind> leftInitKind = left.isInitial() ? Optional.of(left.getInitDiffKind()) : Optional.empty();
        Optional<DiffKind> rightInitKind = right.isInitial() ? Optional.of(right.getInitDiffKind()) : Optional.empty();

        return new DiffAutomatonStateProperty(left.isAccepting(),
                diffKindCombiner.combine(left.getStateDiffKind(), right.getStateDiffKind()),
                optionalDiffKindCombiner.combine(leftInitKind, rightInitKind));
    }
}
