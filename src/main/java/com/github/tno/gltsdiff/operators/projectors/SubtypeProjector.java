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
import java.util.function.Function;

/**
 * A projector for projecting {@code U}-typed properties based on a projector for {@code T}-typed properties, with
 * {@code U} a subtype of {@code T}.
 *
 * @param <T> The type of properties.
 * @param <U> The subtype of properties.
 * @param <V> The type of elements to project along.
 */
public class SubtypeProjector<T, U extends T, V> implements Projector<U, V> {
    /** The projector for properties. */
    private final Projector<T, V> projector;

    /**
     * The converter function for projected properties. This function is only called with non-{@code null} properties,
     * and must always return a non-{@code null} property.
     */
    private final Function<T, U> converter;

    /**
     * Instantiates a new subtype property projector.
     *
     * @param projector The projector for properties.
     * @param converter The converter function for projected properties. This function is only called with
     *     non-{@code null} properties, and must always return a non-{@code null} property.
     */
    public SubtypeProjector(Projector<T, V> projector, Function<T, U> converter) {
        this.projector = projector;
        this.converter = converter;
    }

    @Override
    public Optional<U> project(U property, V along) {
        return projector.project(property, along).map(converter);
    }
}
