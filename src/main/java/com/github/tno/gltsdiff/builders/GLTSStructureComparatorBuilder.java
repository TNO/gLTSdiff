//////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2023 Contributors to the GitHub community
//
// This program and the accompanying materials are made available
// under the terms of the MIT License which is available at
// https://opensource.org/licenses/MIT
//
// SPDX-License-Identifier: MIT
//////////////////////////////////////////////////////////////////////////////

package com.github.tno.gltsdiff.builders;

import com.github.tno.gltsdiff.glts.GLTS;

/**
 * {@link StructureComparatorBuilder Structure comparator builder} to more easily configure the various settings for
 * comparing, merging and (re)writing {@link GLTS GLTSs}.
 *
 * @param <S> The type of state properties.
 * @param <T> The type of transition properties.
 */
public class GLTSStructureComparatorBuilder<S, T> extends StructureComparatorBuilder<S, T, GLTS<S, T>> {
    @Override
    public StructureComparatorBuilder<S, T, GLTS<S, T>> setDefaultInstantiator() {
        return setInstantiator(() -> new GLTS<>());
    }
}
