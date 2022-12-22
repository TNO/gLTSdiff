//////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2021-2022 Contributors to the GitHub community
//
// This program and the accompanying materials are made available
// under the terms of the MIT License which is available at
// https://opensource.org/licenses/MIT
//
// SPDX-License-Identifier: MIT
//////////////////////////////////////////////////////////////////////////////

package com.github.tno.gltsdiff.operators.hiders;

import com.github.tno.gltsdiff.glts.AnnotatedProperty;
import com.google.common.base.Preconditions;

/**
 * A hider for {@link AnnotatedProperty annotated properties} that hides the inner property and removes all annotations.
 *
 * @param <T> The type of properties.
 * @param <U> The type of annotations.
 */
public class AnnotatedPropertyHider<T, U> implements Hider<AnnotatedProperty<T, U>> {
    /** The hider for properties. */
    private final Hider<T> hider;

    /**
     * Instantiates a new annotated property hider.
     * 
     * @param hider The hider for properties.
     */
    public AnnotatedPropertyHider(Hider<T> hider) {
        this.hider = hider;
    }

    @Override
    public AnnotatedProperty<T, U> hide(AnnotatedProperty<T, U> property) {
        Preconditions.checkNotNull(property, "Expected a non-null property.");
        return new AnnotatedProperty<>(hider.hide(property.getProperty()));
    }
}
