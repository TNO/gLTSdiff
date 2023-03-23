//////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2021-2023 Contributors to the GitHub community
//
// This program and the accompanying materials are made available
// under the terms of the MIT License which is available at
// https://opensource.org/licenses/MIT
//
// SPDX-License-Identifier: MIT
//////////////////////////////////////////////////////////////////////////////

package com.github.tno.gltsdiff.operators.projectors;

import java.util.Optional;

import com.github.tno.gltsdiff.glts.DiffAutomatonStateProperty;
import com.github.tno.gltsdiff.glts.DiffKind;
import com.google.common.base.Preconditions;

/**
 * A projector for projecting {@link DiffAutomatonStateProperty difference automaton state properties}.
 *
 * @param <U> The type of elements to project along.
 */
public class DiffAutomatonStatePropertyProjector<U> implements Projector<DiffAutomatonStateProperty, U> {
    /** The projector for difference kinds. */
    private final Projector<DiffKind, U> diffKindProjector;

    /**
     * Instantiates a new difference automaton state property projector.
     *
     * @param diffKindProjector The projector for difference kinds.
     */
    public DiffAutomatonStatePropertyProjector(Projector<DiffKind, U> diffKindProjector) {
        this.diffKindProjector = diffKindProjector;
    }

    @Override
    public Optional<DiffAutomatonStateProperty> project(DiffAutomatonStateProperty property, U along) {
        Preconditions.checkNotNull(property, "Expected a non-null property.");
        Preconditions.checkNotNull(along, "Expected a non-null element to project along.");

        Optional<DiffKind> projectedDiffKind = diffKindProjector.project(property.getStateDiffKind(), along);

        if (projectedDiffKind.isPresent()) {
            Optional<DiffKind> projectedInitDiffKind = property.isInitial()
                    ? diffKindProjector.project(property.getInitDiffKind(), along) : Optional.empty();

            return Optional.of(new DiffAutomatonStateProperty(property.isAccepting(), projectedDiffKind.get(),
                    projectedInitDiffKind));
        }

        return Optional.empty();
    }
}
