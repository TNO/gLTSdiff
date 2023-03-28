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

import com.github.tno.gltsdiff.glts.GLTS;

/**
 * A rewriter that repeatedly applies a rewriter until it no longer changes the GLTS.
 *
 * @param <S> The type of state properties.
 * @param <T> The type of transition properties.
 * @param <U> The type of GLTSs to rewrite.
 */
public class FixedPointRewriter<S, T, U extends GLTS<S, T>> implements Rewriter<S, T, U> {
    /** The rewriter to repeatedly apply. */
    private final Rewriter<S, T, U> rewriter;

    /**
     * Instantiates a fixed point rewriter.
     *
     * @param rewriter The rewriter to repeatedly apply.
     */
    public FixedPointRewriter(Rewriter<S, T, U> rewriter) {
        this.rewriter = rewriter;
    }

    @Override
    public boolean rewrite(U glts) {
        int rounds = 0;
        while (true) {
            rounds++;
            boolean changed = rewriter.rewrite(glts);
            if (!changed) {
                return rounds > 1;
            }
        }
    }
}
