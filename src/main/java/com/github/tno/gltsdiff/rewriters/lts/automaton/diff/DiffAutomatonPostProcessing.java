//////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2021-2023 Contributors to the GitHub community
//
// This program and the accompanying materials are made available
// under the terms of the MIT License which is available at
// https://opensource.org/licenses/MIT
//
// SPDX-License-Identifier: MIT
//////////////////////////////////////////////////////////////////////////////

package com.github.tno.gltsdiff.rewriters.lts.automaton.diff;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

import com.github.tno.gltsdiff.glts.lts.automaton.diff.DiffAutomaton;
import com.github.tno.gltsdiff.glts.lts.automaton.diff.DiffAutomatonStateProperty;
import com.github.tno.gltsdiff.glts.lts.automaton.diff.DiffProperty;
import com.github.tno.gltsdiff.operators.combiners.Combiner;
import com.github.tno.gltsdiff.operators.hiders.Hider;
import com.github.tno.gltsdiff.rewriters.CompositeRewriter;
import com.github.tno.gltsdiff.rewriters.LocalRedundancyRewriter;
import com.github.tno.gltsdiff.rewriters.Rewriter;

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
    public static <T> DiffAutomaton<T> rewrite(DiffAutomaton<T> diff, Combiner<DiffProperty<T>> combiner,
            Hider<DiffProperty<T>> hider)
    {
        // Defines a standard transition property inclusion relation.
        BiPredicate<DiffProperty<T>, DiffProperty<T>> isIncludedIn = (l, r) -> r.equals(combiner.combine(l, r));

        // Instantiate all rewriters to be used for post-processing.
        List<Rewriter<DiffAutomatonStateProperty, DiffProperty<T>, DiffAutomaton<T>>> rewriters = new ArrayList<>();
        rewriters.add(new EntanglementRewriter<>());
        rewriters.add(new LocalRedundancyRewriter<>(combiner));
        rewriters.add(new SkipForkPatternRewriter<>(combiner, hider, isIncludedIn));
        rewriters.add(new SkipJoinPatternRewriter<>(combiner, hider, isIncludedIn));

        // Repeatedly rewrite the difference automaton until rewriting no longer has effect.
        CompositeRewriter<DiffAutomatonStateProperty, DiffProperty<T>, DiffAutomaton<T>> rewriter = new CompositeRewriter<>(
                rewriters);
        rewriter.rewrite(diff);
        return diff;
    }
}
