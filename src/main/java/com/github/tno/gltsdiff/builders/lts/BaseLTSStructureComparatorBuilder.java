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
import com.github.tno.gltsdiff.glts.lts.BaseLTS;
import com.github.tno.gltsdiff.glts.lts.LTSStateProperty;
import com.github.tno.gltsdiff.matchers.lts.BruteForceLTSMatcher;
import com.github.tno.gltsdiff.matchers.lts.DynamicLTSMatcher;
import com.github.tno.gltsdiff.matchers.lts.WalkinshawLTSMatcher;
import com.github.tno.gltsdiff.scorers.lts.DynamicLTSScorer;
import com.github.tno.gltsdiff.scorers.lts.WalkinshawGlobalLTSScorer;
import com.github.tno.gltsdiff.scorers.lts.WalkinshawLocalLTSScorer;
import com.github.tno.gltsdiff.writers.lts.LTSDotWriter;

/**
 * {@link BaseStructureComparatorBuilder Structure comparator builder} to more easily configure the various settings for
 * comparing, merging and (re)writing {@link BaseLTS LTSs} and more specialized representations.
 *
 * @param <S> The type of LTS state properties.
 * @param <T> The type of transition properties.
 * @param <U> The type of LTSs to compare, combine and (re)write.
 */
public abstract class BaseLTSStructureComparatorBuilder<S extends LTSStateProperty, T, U extends BaseLTS<S, T>>
        extends BaseStructureComparatorBuilder<S, T, U>
{
    @Override
    public BaseStructureComparatorBuilder<S, T, U> setDynamicScorer() {
        return setScorer((s, t) -> new DynamicLTSScorer<>(s, t));
    }

    @Override
    public BaseStructureComparatorBuilder<S, T, U> setWalkinshawGlobalScorer() {
        return setScorer((s, t) -> new WalkinshawGlobalLTSScorer<>(s, t));
    }

    @Override
    public BaseStructureComparatorBuilder<S, T, U> setWalkinshawGlobalScorer(double attenuationFactor) {
        return setScorer((s, t) -> new WalkinshawGlobalLTSScorer<>(s, t, attenuationFactor));
    }

    @Override
    public BaseStructureComparatorBuilder<S, T, U> setWalkinshawLocalScorer() {
        return setScorer((s, t) -> new WalkinshawLocalLTSScorer<>(s, t));
    }

    @Override
    public BaseStructureComparatorBuilder<S, T, U> setWalkinshawLocalScorer(int nrOfRefinements) {
        return setScorer((s, t) -> new WalkinshawLocalLTSScorer<>(s, t, nrOfRefinements));
    }

    @Override
    public BaseStructureComparatorBuilder<S, T, U> setWalkinshawLocalScorer(int nrOfRefinements,
            double attenuationFactor)
    {
        return setScorer((s, t) -> new WalkinshawLocalLTSScorer<>(s, t, nrOfRefinements, attenuationFactor));
    }

    @Override
    public BaseStructureComparatorBuilder<S, T, U> setBruteForceMatcher() {
        return setMatcher((s, t, sc) -> new BruteForceLTSMatcher<>(s, t));
    }

    @Override
    public BaseStructureComparatorBuilder<S, T, U> setDynamicMatcher() {
        return setMatcher((s, t, sc) -> new DynamicLTSMatcher<>(s, t));
    }

    @Override
    public BaseStructureComparatorBuilder<S, T, U> setWalkinshawMatcher() {
        return setMatcher((s, t, sc) -> new WalkinshawLTSMatcher<>(sc, s, t));
    }

    @Override
    public BaseStructureComparatorBuilder<S, T, U> setWalkinshawMatcher(double landmarkThreshold,
            double landmarkRatio)
    {
        return setMatcher((s, t, sc) -> new WalkinshawLTSMatcher<>(sc, s, t, landmarkThreshold, landmarkRatio));
    }

    @Override
    public BaseStructureComparatorBuilder<S, T, U> setDefaultDotWriter() {
        return setDotWriter((sp, tp) -> new LTSDotWriter<>(sp, tp));
    }
}
