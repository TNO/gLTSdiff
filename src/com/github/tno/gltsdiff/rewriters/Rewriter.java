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

import com.github.tno.gltsdiff.glts.GLTS;

/**
 * A transformation of GLTSs.
 * 
 * @param <S> The type of state properties.
 * @param <T> The type of transition properties.
 * @param <U> The type of GLTSs to rewrite.
 */
public interface Rewriter<S, T, U extends GLTS<S, T>> {
    /**
     * Transforms the given GLTS.
     * 
     * @param glts The GLTS to be transformed.
     * @return {@code true} if this transformation was <i>effective</i>, i.e., if the GLTS has been modified by this
     *     transformation; {@code false} otherwise.
     */
    public boolean rewrite(U glts);
}
