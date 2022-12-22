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

import com.github.tno.gltsdiff.glts.DiffKind;
import com.github.tno.gltsdiff.glts.DiffProperty;
import com.google.common.base.Preconditions;

/**
 * A {@link Hider} for {@link DiffProperty difference properties} of type {@code T} that hides their inner
 * {@code T}-typed properties and leaves the associated {@link DiffKind} unchanged.
 *
 * @param <T> The type of inner properties.
 */
public class DiffPropertyHider<T> implements Hider<DiffProperty<T>> {
    private final Hider<T> hider;

    /**
     * Instantiates a {@link Hider} for difference properties using {@code hider} for the inner properties.
     * 
     * @param hider {@link Hider} to use for the inner properties.
     */
    public DiffPropertyHider(Hider<T> hider) {
        this.hider = hider;
    }

    @Override
    public DiffProperty<T> hide(DiffProperty<T> property) {
        Preconditions.checkNotNull(property, "Expected a non-null property.");
        return new DiffProperty<>(hider.hide(property.getProperty()), property.getDiffKind());
    }
}
