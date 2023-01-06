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

import com.google.common.base.Preconditions;

/**
 * A substitution {@link Hider} that hides properties simply by replacing them by a specified non-{@code null}
 * substitute.
 *
 * @param <T> The type of properties to hide.
 */
public class SubstitutionHider<T> implements Hider<T> {
    private final T substitute;

    /**
     * Instantiates a new hider that hides properties by replacing them with {@code substitute}.
     * 
     * @param substitute The property used to replace hidden properties.
     */
    public SubstitutionHider(T substitute) {
        Preconditions.checkNotNull(substitute, "Expected the substitute to be non-null.");
        this.substitute = substitute;
    }

    @Override
    public T hide(T property) {
        return substitute;
    }
}
