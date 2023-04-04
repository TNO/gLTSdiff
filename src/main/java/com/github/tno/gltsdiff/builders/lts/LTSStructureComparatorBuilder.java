//////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2023 Contributors to the GitHub community
//
// This program and the accompanying materials are made available
// under the terms of the MIT License which is available at
// https://opensource.org/licenses/MIT
//
// SPDX-License-Identifier: MIT
//////////////////////////////////////////////////////////////////////////////

package com.github.tno.gltsdiff.builders.lts;

import com.github.tno.gltsdiff.builders.BaseStructureComparatorBuilder;
import com.github.tno.gltsdiff.glts.lts.LTSStateProperty;
import com.github.tno.gltsdiff.glts.lts.SimpleLTS;
import com.github.tno.gltsdiff.operators.combiners.lts.LTSStatePropertyCombiner;

/**
 * {@link BaseStructureComparatorBuilder Structure comparator builder} to more easily configure the various settings for
 * comparing, merging and (re)writing {@link SimpleLTS simple LTSs}.
 *
 * @param <T> The type of transition properties.
 */
public class LTSStructureComparatorBuilder<T>
        extends BaseLTSStructureComparatorBuilder<LTSStateProperty, T, SimpleLTS<T>>
{
    @Override
    public BaseStructureComparatorBuilder<LTSStateProperty, T, SimpleLTS<T>> setDefaultInstantiator() {
        return setInstantiator(() -> new SimpleLTS<>());
    }

    @Override
    public BaseStructureComparatorBuilder<LTSStateProperty, T, SimpleLTS<T>> setDefaultStatePropertyCombiner() {
        return setStatePropertyCombiner(new LTSStatePropertyCombiner());
    }
}
