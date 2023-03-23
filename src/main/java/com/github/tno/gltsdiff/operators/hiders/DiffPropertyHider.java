//////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2021-2023 Contributors to the GitHub community
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
 * A hider for {@link DiffProperty difference properties} of type {@code T} that hides their inner {@code T}-typed
 * properties and leaves the associated {@link DiffKind} unchanged.
 *
 * @param <T> The type of inner properties.
 */
public class DiffPropertyHider<T> implements Hider<DiffProperty<T>> {
    /** Hider for the inner properties. */
    private final Hider<T> hider;

    /**
     * Instantiates a hider for difference properties with a given hider for the inner properties.
     *
     * @param hider Hider for the inner properties.
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
