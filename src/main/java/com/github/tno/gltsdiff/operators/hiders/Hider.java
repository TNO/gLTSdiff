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

import java.util.Collection;
import java.util.stream.Stream;

import com.google.common.base.Preconditions;

/**
 * An operator for hiding properties.
 *
 * <p>
 * Hiding operators come with a unary idempotent operator {@link #hide(Object) hide(T)} for <i>hiding</i> properties
 * (for example, replacing the labels within transition properties with 'tau'). Hiding operators also describe whether
 * any {@code T}-typed property is <i>hidden</i> ({@link #isHidden(Object) isHidden(T)}).
 * </p>
 *
 * @param <T> The type of hidable properties.
 */
@FunctionalInterface
public interface Hider<T> {
    /**
     * Unary operator for <i>hiding</i> the specified property.
     *
     * <p>
     * This operator must be <u>idempotent</u> meaning that hiding more than once does not have any further effect after
     * the first hide.
     * </p>
     *
     * @param property The property to hide, which must not be {@code null}.
     * @return The hidden version of {@code property}, which is non-{@code null}.
     */
    public T hide(T property);

    /**
     * Determines whether {@code property} is <i>hidden</i>.
     *
     * @param property The input property, which must not be {@code null}.
     * @return {@code true} if and only if {@code property} is hidden.
     */
    public default boolean isHidden(T property) {
        Preconditions.checkNotNull(property, "Expected a non-null property.");
        return property.equals(hide(property));
    }

    /**
     * Determine whether all properties in {@code properties} are <i>hidden</i>.
     *
     * @param properties The input collection of properties, none of which must be {@code null}.
     * @return {@code true} if and only if all properties of {@code properties} are hidden.
     */
    public default boolean areHidden(Collection<T> properties) {
        return areHidden(properties.stream());
    }

    /**
     * Determines whether all properties in the specified {@code stream} are <i>hidden</i>. This operation requires that
     * {@code stream} is <u>finite</u>.
     *
     * @param stream The input stream whose properties must all not be {@code null}.
     * @return {@code true} if and only if all properties in {@code stream} are hidden.
     */
    public default boolean areHidden(Stream<T> stream) {
        return stream.allMatch(property -> isHidden(property));
    }
}
