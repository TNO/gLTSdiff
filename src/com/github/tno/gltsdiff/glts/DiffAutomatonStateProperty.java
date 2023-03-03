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
import java.util.Optional;

import com.google.common.base.Preconditions;

/** A property that is associated to states in {@link DiffAutomaton difference automata}. */
public class DiffAutomatonStateProperty extends AutomatonStateProperty {
    /** The non-{@code null} difference kind of the associated state. */
    private final DiffKind stateDiffKind;

    /**
     * The non-{@code null} difference kind of the initial state arrow, which is {@link Optional#empty()} if and only if
     * the associated state is not initial, i.e., if {@link #isInitial()} is {@code false};
     */
    private final Optional<DiffKind> initDiffKind;

    /**
     * Instantiates a new difference automaton state property.
     *
     * @param isAccepting Whether the associated state is accepting.
     * @param stateDiffKind The non-{@code null} difference kind of the associated state.
     * @param initDiffKind A non-{@code null} non-empty optional in case the state is initial with a difference kind for
     *     the initial state arrow, or {@link Optional#empty()} if the associated state is not initial.
     */
    public DiffAutomatonStateProperty(boolean isAccepting, DiffKind stateDiffKind, Optional<DiffKind> initDiffKind) {
        super(Preconditions.checkNotNull(initDiffKind, "Expected non-null initial state information.").isPresent(),
                isAccepting);

        Preconditions.checkNotNull(stateDiffKind, "Expected a non-null state difference kind.");

        initDiffKind.ifPresent(initKind -> Preconditions.checkArgument(
                initKind == stateDiffKind || stateDiffKind == DiffKind.UNCHANGED,
                "Expected consistent difference kinds."));

        this.stateDiffKind = stateDiffKind;
        this.initDiffKind = initDiffKind;
    }

    /**
     * Returns the difference kind of the associated state.
     *
     * @return The non-{@code null} difference kind of the associated state.
     */
    public DiffKind getStateDiffKind() {
        return stateDiffKind;
    }

    /**
     * Returns the difference kind of the initial state arrow. This method can only be called if the associated state is
     * initial, i.e., if {@link #isInitial} returns {@code true}.
     *
     * @return The non-{@code null} difference kind of the initial state arrow.
     */
    public DiffKind getInitDiffKind() {
        Preconditions.checkArgument(isInitial(), "Expected the associated state to be initial.");
        return initDiffKind.get();
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), stateDiffKind, initDiffKind);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof DiffAutomatonStateProperty)) {
            return false;
        }

        final DiffAutomatonStateProperty other = (DiffAutomatonStateProperty)object;

        return super.equals(other) && Objects.equals(stateDiffKind, other.stateDiffKind)
                && Objects.equals(initDiffKind, other.initDiffKind);
    }
}
