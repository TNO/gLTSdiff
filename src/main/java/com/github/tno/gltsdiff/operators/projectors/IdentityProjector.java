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
 * A projector that returns the given input property unaltered.
 *
 * @param <T> The type of properties.
 * @param <U> The type of elements to project along.
 */
public class IdentityProjector<T, U> implements Projector<T, U> {
    @Override
    public Optional<T> project(T property, U along) {
        Preconditions.checkNotNull(property, "Expected a non-null property.");
        return Optional.of(property);
    }
}
