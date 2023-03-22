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

public abstract class WalkinshawScorerTest {
    protected double roundToTwoDecimals(double d) {
        return (Double.isInfinite(d)) ? d : Math.round(d * 100.0d) / 100.0d;
    }
}
