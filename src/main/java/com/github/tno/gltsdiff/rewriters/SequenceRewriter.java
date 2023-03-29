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
 * A rewriter that applies multiple rewriters in sequence.
 *
 * @param <S> The type of state properties.
 * @param <T> The type of transition properties.
 * @param <U> The type of GLTSs to rewrite.
 */
public class SequenceRewriter<S, T, U extends GLTS<S, T>> implements Rewriter<S, T, U> {
    /** The rewriters to apply in sequence. */
    private final List<Rewriter<S, T, U>> rewriters;

    /**
     * Instantiates a sequence rewriter.
     *
     * @param rewriters The rewriters to apply in sequence.
     */
    public SequenceRewriter(Collection<Rewriter<S, T, U>> rewriters) {
        this.rewriters = new ArrayList<>(rewriters);
    }

    @Override
    public boolean rewrite(U glts) {
        boolean changed = false;
        for (Rewriter<S, T, U> rewriter: rewriters) {
            changed |= rewriter.rewrite(glts);
        }
        return changed;
    }
}
