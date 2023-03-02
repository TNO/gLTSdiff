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

/**
 * A projector for projecting {@code T}-typed state and/or transition properties along a {@code U}-typed element.
 *
 * @param <T> The type of properties.
 * @param <U> The type of elements to project along.
 */
@FunctionalInterface
public interface Projector<T, U> {
    /**
     * Projects the specified {@code property} along a given element {@code along}.
     *
     * @param property The non-{@code null} property to project.
     * @param along The non-{@code null} element to project along.
     * @return The non-{@code null} projected property, or {@link Optional#empty()} if nothing remained after
     *     projection.
     */
    public Optional<T> project(T property, U along);
}
