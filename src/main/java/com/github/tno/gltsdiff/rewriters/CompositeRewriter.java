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
import java.util.Collection;
import java.util.List;

import com.github.tno.gltsdiff.glts.GLTS;

/**
 * A composite rewriter that applies multiple rewriters repeatedly, until they no longer change the GLTS.
 *
 * @param <S> The type of state properties.
 * @param <T> The type of transition properties.
 * @param <U> The type of GLTSs to rewrite.
 */
public class CompositeRewriter<S, T, U extends GLTS<S, T>> implements Rewriter<S, T, U> {
    /** The rewriters to apply. */
    private final List<Rewriter<S, T, U>> rewriters;

    /**
     * Constructor for the {@link CompositeRewriter} class.
     *
     * @param rewriters The rewriters to apply.
     */
    public CompositeRewriter(Collection<Rewriter<S, T, U>> rewriters) {
        this.rewriters = new ArrayList<>(rewriters);
    }

    @Override
    public boolean rewrite(U glts) {
        boolean changed;
        int rounds = 0;
        do {
            rounds++;
            changed = rewriters.stream().map(rewriter -> rewriter.rewrite(glts)).anyMatch(b -> b);
        } while (changed);

        return rounds > 1;
    }
}
