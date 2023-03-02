//////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2021-2023 Contributors to the GitHub community
//
// This program and the accompanying materials are made available
// under the terms of the MIT License which is available at
// https://opensource.org/licenses/MIT
//
// SPDX-License-Identifier: MIT
//////////////////////////////////////////////////////////////////////////////

package com.github.tno.gltsdiff.glts;

import java.util.Objects;

/** A property that is associated to states in {@link LTS LTSs}. */
public class LTSStateProperty {
    /** Whether the associated state is initial. */
    private final boolean isInitial;

    /**
     * Instantiates a new LTS state property.
     *
     * @param isInitial Whether the associated state is initial.
     */
    public LTSStateProperty(boolean isInitial) {
        this.isInitial = isInitial;
    }

    /**
     * Returns whether the associated state is initial.
     *
     * @return {@code true} if the associated state is initial, {@code false} otherwise.
     */
    public boolean isInitial() {
        return this.isInitial;
    }

    @Override
    public int hashCode() {
        return Objects.hash(isInitial);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof LTSStateProperty)) {
            return false;
        }

        final LTSStateProperty other = (LTSStateProperty)object;

        return Objects.equals(isInitial, other.isInitial);
    }
}
