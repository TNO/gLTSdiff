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

/** A property that is associated to states in {@link Automaton automata}. */
public class AutomatonStateProperty extends LTSStateProperty {
    /** Whether the associated state is accepting. */
    private final boolean isAccepting;

    /**
     * Instantiates a new automaton state property.
     *
     * @param isInitial Whether the associated state is initial.
     * @param isAccepting Whether the associated state is accepting.
     */
    public AutomatonStateProperty(boolean isInitial, boolean isAccepting) {
        super(isInitial);
        this.isAccepting = isAccepting;
    }

    /**
     * Returns whether the associated state is accepting.
     *
     * @return {@code true} if the associated state is accepting, {@code false} otherwise.
     */
    public boolean isAccepting() {
        return this.isAccepting;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), isAccepting);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof AutomatonStateProperty)) {
            return false;
        }

        final AutomatonStateProperty other = (AutomatonStateProperty)object;

        return super.equals(other) && Objects.equals(isAccepting, other.isAccepting);
    }
}
