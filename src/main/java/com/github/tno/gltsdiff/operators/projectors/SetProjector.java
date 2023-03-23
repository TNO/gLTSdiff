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

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;

/**
 * A projector for projecting sets of properties.
 *
 * @param <T> The type of properties.
 * @param <U> The type of elements to project along.
 */
public class SetProjector<T, U> implements Projector<Set<T>, U> {
    /** The projector for properties. */
    private final Projector<T, U> propertyProjector;

    /**
     * Instantiates a new property set projector.
     *
     * @param propertyProjector The projector for properties.
     */
    public SetProjector(Projector<T, U> propertyProjector) {
        this.propertyProjector = propertyProjector;
    }

    @Override
    public Optional<Set<T>> project(Set<T> properties, U along) {
        Preconditions.checkNotNull(properties, "Expected a non-null set of properties.");
        Preconditions.checkNotNull(along, "Expected a non-null element to project along.");

        return Optional.of(properties.stream().map(property -> propertyProjector.project(property, along))
                .filter(Optional::isPresent).map(Optional::get).collect(Collectors.toCollection(LinkedHashSet::new)));
    }
}
