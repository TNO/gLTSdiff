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
 * A rewriter that applies multiple rewriters in sequence, and repeats applying until they no longer change the GLTS.
 *
 * @param <S> The type of state properties.
 * @param <T> The type of transition properties.
 * @param <U> The type of GLTSs to rewrite.
 */
public class SequenceRewriter<S, T, U extends GLTS<S, T>> implements Rewriter<S, T, U> {
    /** The rewriters to apply. */
    private final List<Rewriter<S, T, U>> rewriters;

    /**
     * Constructor for the {@link SequenceRewriter} class.
     *
     * <p>
     * Using a sequence rewriter is practially only useful if at least two rewriters are provided. Provide an empty
     * collection of rewriters is allowed, but using {@link NothingRewriter} would be more efficient. Providing a
     * singleton collection is also allowed, but using the single rewriter directly rather than wrapping it in a
     * sequence rewriter would be more efficient.
     * </p>
     *
     * @param rewriters The rewriters to apply.
     */
    public SequenceRewriter(Collection<Rewriter<S, T, U>> rewriters) {
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
