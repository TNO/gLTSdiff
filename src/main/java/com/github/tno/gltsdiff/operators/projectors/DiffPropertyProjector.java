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

import com.github.tno.gltsdiff.glts.DiffKind;
import com.github.tno.gltsdiff.glts.DiffProperty;
import com.google.common.base.Preconditions;

/**
 * A projector for projecting {@link DiffProperty difference properties}.
 *
 * @param <T> The type of inner properties.
 * @param <U> The type of elements to project along.
 */
public class DiffPropertyProjector<T, U> implements Projector<DiffProperty<T>, U> {
    /** The projector for the inner properties. */
    private final Projector<T, U> propertyProjector;

    /** The projector for difference kinds. */
    private final Projector<DiffKind, U> diffKindProjector;

    /**
     * Instantiates a new difference property projector.
     *
     * @param propertyProjector The projector for the inner properties.
     * @param diffKindProjector The projector for difference kinds.
     */
    public DiffPropertyProjector(Projector<T, U> propertyProjector, Projector<DiffKind, U> diffKindProjector) {
        this.propertyProjector = propertyProjector;
        this.diffKindProjector = diffKindProjector;
    }

    @Override
    public Optional<DiffProperty<T>> project(DiffProperty<T> property, U along) {
        Preconditions.checkNotNull(property, "Expected a non-null property.");
        Preconditions.checkNotNull(along, "Expected a non-null element to project along.");

        Optional<T> projectedProperty = propertyProjector.project(property.getProperty(), along);

        if (projectedProperty.isPresent()) {
            Optional<DiffKind> projectedDiffKind = diffKindProjector.project(property.getDiffKind(), along);

            if (projectedDiffKind.isPresent()) {
                return Optional.of(new DiffProperty<>(projectedProperty.get(), projectedDiffKind.get()));
            }
        }

        return Optional.empty();
    }
}
