//////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2023 Contributors to the GitHub community
//
// This program and the accompanying materials are made available
// under the terms of the MIT License which is available at
// https://opensource.org/licenses/MIT
//
// SPDX-License-Identifier: MIT
//////////////////////////////////////////////////////////////////////////////

package com.github.tno.gltsdiff.operators.inclusions;

import java.util.Objects;
import java.util.Set;

import com.github.tno.gltsdiff.glts.lts.automaton.diff.DiffKind;
import com.github.tno.gltsdiff.operators.combiners.Combiner;

/**
 * An operator for determining property inclusion.
 *
 * <p>
 * Inclusion describes whether one property is included in another ({@link #isIncludedIn}). For example:
 * </p>
 * <ul>
 * <li>When comparing {@link Set}s of integer numbers, a set {1, 2, 4} is included in set {1, 2, 3, 4, 5}, as all the
 * integer numbers that are in the first set are also in the second set.</li>
 * <li>When comparing {@link DiffKind difference kinds}, if something is both added (in one input model) and removed (in
 * the other input model), then it is really unchanged. The unchanged difference kind thus is a combination of being
 * added and removed. Hence, {@link DiffKind#UNCHANGED} includes {@link DiffKind#ADDED} and
 * {@link DiffKind#REMOVED}.</li>
 * </ul>
 *
 * @param <T> The type of properties, for which to determine inclusion.
 */
@FunctionalInterface
public interface Inclusion<T> {
    /**
     * Binary operator for checking inclusion between two combinable properties.
     *
     * <p>
     * This operator must be:
     * </p>
     * <ul>
     * <li><u>reflexive</u>: Every property is included in itself, meaning {@link #isIncludedIn
     * isIncludedIn}{@code (x, x)} must always return {@code true}.</li>
     * <li><u>antisymmetric</u>: If {@link #isIncludedIn isIncludedIn}{@code (x, y)} returns {@code true} and
     * {@link Objects#equals}{@code (x, y)} returns {@code false}, then {@link #isIncludedIn isIncludedIn}{@code (y, x)}
     * must return {@code false}.</li>
     * <li><u>transitive</u>: If {@link #isIncludedIn isIncludedIn}{@code (x, y)} returns {@code true} and
     * {@link #isIncludedIn isIncludedIn}{@code (y, z)} returns {@code true}, then also {@link #isIncludedIn
     * isIncludedIn}{@code (x, z)} must return {@code true}.</li>
     * </ul>
     *
     * @param property1 The first property, to check whether it is included in the second property. Must not be
     *     {@code null}.
     * @param property2 The second property, to check whether it includes the first property. Must not be {@code null}.
     * @param combiner The property combiner.
     * @return {@code true} if the first property is included in the second property, {@code false} otherwise.
     */
    public boolean isIncludedIn(T property1, T property2, Combiner<T> combiner);
}
