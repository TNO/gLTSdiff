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

import com.google.common.base.Preconditions;

/**
 * A projector for projecting {@link Optional optional properties}.
 *
 * @param <T> The type of optional properties.
 * @param <U> The type of elements to project along.
 */
public class OptionalProjector<T, U> implements Projector<Optional<T>, U> {
    /** The projector for optional properties. */
    private final Projector<T, U> projector;

    /**
     * Instantiates a new optional property projector.
     *
     * @param projector The projector for optional properties.
     */
    public OptionalProjector(Projector<T, U> projector) {
        this.projector = projector;
    }

    @Override
    public Optional<Optional<T>> project(Optional<T> property, U along) {
        Preconditions.checkNotNull(property, "Expected a non-null property.");
        Preconditions.checkNotNull(along, "Expected a non-null element to project along.");

        if (property.isPresent()) {
            return projector.project(property.get(), along).map(Optional::of);
        }

        return Optional.of(Optional.empty());
    }
}
