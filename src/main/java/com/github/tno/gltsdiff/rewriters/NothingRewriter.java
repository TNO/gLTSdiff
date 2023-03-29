//////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2023 Contributors to the GitHub community
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
 * Rewriter that does not perform any rewriting.
 *
 * @param <S> The type of state properties.
 * @param <T> The type of transition properties.
 * @param <U> The type of GLTSs to rewrite.
 */
public class NothingRewriter<S, T, U extends GLTS<S, T>> implements Rewriter<S, T, U> {
    @Override
    public boolean rewrite(U glts) {
        return false;
    }
}
