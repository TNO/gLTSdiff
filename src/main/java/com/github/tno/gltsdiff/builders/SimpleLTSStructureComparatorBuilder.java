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

import com.github.tno.gltsdiff.glts.lts.LTS;
import com.github.tno.gltsdiff.glts.lts.LTSStateProperty;
import com.github.tno.gltsdiff.glts.lts.SimpleLTS;
import com.github.tno.gltsdiff.operators.combiners.lts.LTSStatePropertyCombiner;

/**
 * {@link StructureComparatorBuilder Structure comparator builder} to more easily configure the various settings for
 * comparing, merging and writing {@link SimpleLTS simple LTSs}.
 *
 * @param <T> The type of transition properties.
 * @param <U> The type of LTSs to compare and combine.
 */
public class SimpleLTSStructureComparatorBuilder<T, U extends LTS<LTSStateProperty, T>>
        extends LTSStructureComparatorBuilder<LTSStateProperty, T, SimpleLTS<T>>
{
    @Override
    public StructureComparatorBuilder<LTSStateProperty, T, SimpleLTS<T>> setDefaultInstantiator() {
        return setInstantiator(() -> new SimpleLTS<>());
    }

    @Override
    public StructureComparatorBuilder<LTSStateProperty, T, SimpleLTS<T>> setDefaultStatePropertyCombiner() {
        return setStatePropertyCombiner(new LTSStatePropertyCombiner());
    }
}
