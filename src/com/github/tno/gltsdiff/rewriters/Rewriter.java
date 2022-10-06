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

import com.github.tno.gltsdiff.lts.LTS;

/**
 * A transformation of LTSs.
 * 
 * @param <S> The type of state properties.
 * @param <T> The type of transition properties.
 * @param <U> The type of LTSs to rewrite.
 */
public interface Rewriter<S, T, U extends LTS<S, T>> {
    /**
     * Transforms the given LTS.
     * 
     * @param lts The LTS to be transformed.
     * @return {@code true} if this transformation was <i>effective</i>, i.e., if the LTS has been modified by this
     *     transformation; {@code false} otherwise.
     */
    public boolean rewrite(U lts);
}
