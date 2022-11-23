//////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2021-2022 Contributors to the GitHub community
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

import com.github.tno.gltsdiff.lts.DiffAutomaton;
import com.github.tno.gltsdiff.lts.DiffAutomatonStateProperty;
import com.github.tno.gltsdiff.lts.DiffProperty;
import com.github.tno.gltsdiff.operators.combiners.Combiner;
import com.github.tno.gltsdiff.operators.combiners.DiffPropertyCombiner;
import com.github.tno.gltsdiff.operators.hiders.Hider;

/** A default post processor for difference automata. */
public class DiffAutomatonPostProcessing {
    private DiffAutomatonPostProcessing() {
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
        // Instantiate all rewriters to be used for post-processing.
        List<Rewriter<DiffAutomatonStateProperty, DiffProperty<T>, DiffAutomaton<T>>> rewriters = new ArrayList<>(3);
        rewriters.add(new DisentangleRewriter<>());
        rewriters.add(new LocalRedundancyRewriter<>(new DiffPropertyCombiner<>(combiner)));
        rewriters.add(new SkipForkPatternRewriter<>(combiner, hider));
        rewriters.add(new SkipJoinPatternRewriter<>(combiner, hider));

        // Repeatedly rewrite the difference automaton until rewriting no longer has effect.
        boolean changed = true;
        do {
            changed = rewriters.stream().map(rewriter -> rewriter.rewrite(diff)).anyMatch(b -> b);
        } while (changed);

        return diff;
    }
}
