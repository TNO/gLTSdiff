//////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2021-2023 Contributors to the GitHub community
//
// This program and the accompanying materials are made available
// under the terms of the MIT License which is available at
// https://opensource.org/licenses/MIT
//
// SPDX-License-Identifier: MIT
//////////////////////////////////////////////////////////////////////////////

package com.github.tno.gltsdiff.glts.lts;

import com.github.tno.gltsdiff.glts.State;
import com.google.common.base.Preconditions;

/**
 * A simple LTS, which is a concrete GLTS with initial states.
 *
 * @param <T> The type of transition properties.
 */
public class SimpleLTS<T> extends LTS<LTSStateProperty, T> implements Cloneable {
    @Override
    public boolean isInitial(LTSStateProperty property) {
        Preconditions.checkNotNull(property, "Expected a non-null state property.");
        return property.isInitial();
    }

    /**
     * Adds a new initial state to this LTS.
     *
     * @return The non-{@code null} newly added initial state.
     */
    public State<LTSStateProperty> addInitialState() {
        return addState(new LTSStateProperty(true));
    }

    /**
     * Adds a new non-initial state to this LTS.
     *
     * @return The non-{@code null} newly added non-initial state.
     */
    public State<LTSStateProperty> addState() {
        return addState(new LTSStateProperty(false));
    }
}
