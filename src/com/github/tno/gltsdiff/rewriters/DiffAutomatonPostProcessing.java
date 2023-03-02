//////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2021-2023 Contributors to the GitHub community
//
// This program and the accompanying materials are made available
// under the terms of the MIT License which is available at
// https://opensource.org/licenses/MIT
//
// SPDX-License-Identifier: MIT
//////////////////////////////////////////////////////////////////////////////

package com.github.tno.gltsdiff.rewriters;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

import com.github.tno.gltsdiff.glts.DiffAutomaton;
import com.github.tno.gltsdiff.glts.DiffAutomatonStateProperty;
import com.github.tno.gltsdiff.glts.DiffProperty;
import com.github.tno.gltsdiff.operators.combiners.Combiner;
import com.github.tno.gltsdiff.operators.combiners.DiffPropertyCombiner;
import com.github.tno.gltsdiff.operators.hiders.DiffPropertyHider;
import com.github.tno.gltsdiff.operators.hiders.Hider;

/** A default post processor for difference automata. */
public class DiffAutomatonPostProcessing {
    /** Constructor for the {@link DiffAutomatonPostProcessing} class. */
    private DiffAutomatonPostProcessing() {
        // Static class.
    }

    /**
     * Rewrites the given difference automaton by applying post processing to it.
     *
     * @param <T> The type of transition properties.
     * @param diff The difference automaton to rewrite.
     * @param combiner The combiner for transition properties.
     * @param hider The hider for transition properties.
     * @return The post processed difference automaton.
     */
    public static <T> DiffAutomaton<T> rewrite(DiffAutomaton<T> diff, Combiner<T> combiner, Hider<T> hider) {
        // Instantiate a combiner and hider for difference transition properties.
        Combiner<DiffProperty<T>> diffCombiner = new DiffPropertyCombiner<>(combiner);
        Hider<DiffProperty<T>> diffHider = new DiffPropertyHider<>(hider);

        // Defines a standard transition property inclusion relation.
        BiPredicate<DiffProperty<T>, DiffProperty<T>> isIncludedIn = (l, r) -> r.equals(diffCombiner.combine(l, r));

        // Instantiate all rewriters to be used for post-processing.
        List<Rewriter<DiffAutomatonStateProperty, DiffProperty<T>, DiffAutomaton<T>>> rewriters = new ArrayList<>();
        rewriters.add(new EntanglementRewriter<>());
        rewriters.add(new LocalRedundancyRewriter<>(diffCombiner));
        rewriters.add(new SkipForkPatternRewriter<>(diffCombiner, diffHider, isIncludedIn));
        rewriters.add(new SkipJoinPatternRewriter<>(diffCombiner, diffHider, isIncludedIn));

        // Repeatedly rewrite the difference automaton until rewriting no longer has effect.
        boolean changed = true;
        do {
            changed = rewriters.stream().map(rewriter -> rewriter.rewrite(diff)).anyMatch(b -> b);
        } while (changed);

        return diff;
    }
}
