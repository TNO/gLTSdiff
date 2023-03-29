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

import com.github.tno.gltsdiff.builders.StructureComparatorBuilder;
import com.github.tno.gltsdiff.glts.lts.LTS;
import com.github.tno.gltsdiff.glts.lts.LTSStateProperty;
import com.github.tno.gltsdiff.matchers.lts.BruteForceLTSMatcher;
import com.github.tno.gltsdiff.matchers.lts.DynamicLTSMatcher;
import com.github.tno.gltsdiff.matchers.lts.WalkinshawLTSMatcher;
import com.github.tno.gltsdiff.scorers.lts.DynamicLTSScorer;
import com.github.tno.gltsdiff.scorers.lts.WalkinshawGlobalLTSScorer;
import com.github.tno.gltsdiff.scorers.lts.WalkinshawLocalLTSScorer;
import com.github.tno.gltsdiff.writers.lts.LTSDotWriter;

/**
 * Builder to more easily configure the various settings for comparing, merging and writing {@link LTS LTSs} and more
 * specialized representations.
 *
 * @param <S> The type of state properties.
 * @param <T> The type of transition properties.
 * @param <U> The type of LTSs to compare and combine.
 */
public abstract class LTSStructureComparatorBuilder<S extends LTSStateProperty, T, U extends LTS<S, T>>
        extends StructureComparatorBuilder<S, T, U>
{
    @Override
    public StructureComparatorBuilder<S, T, U> setDynamicScorer() {
        return setScorer((s, t) -> new DynamicLTSScorer<>(s, t));
    }

    @Override
    public StructureComparatorBuilder<S, T, U> setWalkinshawGlobalScorer() {
        return setScorer((s, t) -> new WalkinshawGlobalLTSScorer<>(s, t));
    }

    @Override
    public StructureComparatorBuilder<S, T, U> setWalkinshawGlobalScorer(double attenuationFactor) {
        return setScorer((s, t) -> new WalkinshawGlobalLTSScorer<>(s, t, attenuationFactor));
    }

    @Override
    public StructureComparatorBuilder<S, T, U> setWalkinshawLocalScorer() {
        return setScorer((s, t) -> new WalkinshawLocalLTSScorer<>(s, t));
    }

    @Override
    public StructureComparatorBuilder<S, T, U> setWalkinshawLocalScorer(int nrOfRefinements) {
        return setScorer((s, t) -> new WalkinshawLocalLTSScorer<>(s, t, nrOfRefinements));
    }

    @Override
    public StructureComparatorBuilder<S, T, U> setWalkinshawLocalScorer(int nrOfRefinements, double attenuationFactor) {
        return setScorer((s, t) -> new WalkinshawLocalLTSScorer<>(s, t, nrOfRefinements, attenuationFactor));
    }

    @Override
    public StructureComparatorBuilder<S, T, U> setBruteForceMatcher() {
        return setMatcher((s, t, sc) -> new BruteForceLTSMatcher<>(s, t));
    }

    @Override
    public StructureComparatorBuilder<S, T, U> setDynamicMatcher() {
        return setMatcher((s, t, sc) -> new DynamicLTSMatcher<>(s, t));
    }

    @Override
    public StructureComparatorBuilder<S, T, U> setWalkinshawMatcher() {
        return setMatcher((s, t, sc) -> new WalkinshawLTSMatcher<>(sc, s, t));
    }

    @Override
    public StructureComparatorBuilder<S, T, U> setWalkinshawMatcher(double landmarkThreshold, double landmarkRatio) {
        return setMatcher((s, t, sc) -> new WalkinshawLTSMatcher<>(sc, s, t, landmarkThreshold, landmarkRatio));
    }

    @Override
    public StructureComparatorBuilder<S, T, U> setDefaultDotWriter() {
        return setDotWriter((sp, tp) -> new LTSDotWriter<>(sp, tp));
    }
}
