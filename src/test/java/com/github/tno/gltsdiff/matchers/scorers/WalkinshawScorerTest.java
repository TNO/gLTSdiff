//////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2021-2023 Contributors to the GitHub community
//
// This program and the accompanying materials are made available
// under the terms of the MIT License which is available at
// https://opensource.org/licenses/MIT
//
// SPDX-License-Identifier: MIT
//////////////////////////////////////////////////////////////////////////////

package com.github.tno.gltsdiff.matchers.scorers;

import com.github.tno.gltsdiff.scorers.WalkinshawScorer;

/** Base class for {@link WalkinshawScorer} tests. */
public abstract class WalkinshawScorerTest {
    /**
     * Round the given similarity score to two decimals. Does not change infinite scores.
     *
     * @param s The score.
     * @return The rounded score.
     */
    protected double roundToTwoDecimals(double s) {
        return (Double.isInfinite(s)) ? s : Math.round(s * 100.0d) / 100.0d;
    }
}
