//////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2021-2023 Contributors to the GitHub community
//
// This program and the accompanying materials are made available
// under the terms of the MIT License which is available at
// https://opensource.org/licenses/MIT
//
// SPDX-License-Identifier: MIT
//////////////////////////////////////////////////////////////////////////////

package com.github.tno.gltsdiff.operators.combiners;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

import com.google.common.base.Preconditions;

/**
 * An operator for combining properties.
 *
 * <p>
 * Combiners describe whether any two properties of type {@code T} are <i>combinable</i>, and if so, describes what
 * their <i>combination</i> is. These notions are implemented in terms of an equivalence relation
 * {@link #areCombinable(Object, Object) areCombinable(T, T)} and a binary combinability-preserving operation
 * {@link #combine(Object, Object) combine(T, T)}, respectively.
 * </p>
 *
 * @param <T> The type of combinable properties, for which value equality must be defined.
 */
public abstract class Combiner<T> {
    /**
     * Determines whether {@code left} and {@code right} can be combined into a single property.
     *
     * <p>
     * This binary relation must be implemented to satisfy the following two properties:
     * <ul>
     * <li>It must be an <u>equivalence relation</u>, meaning that it must be reflexive, symmetric and transitive.</li>
     * <li>It must <u>agree with Java value equality</u>: if {@code left} and {@code right} are {@link Object#equals
     * equal} in the Java sense then they must also necessarily be combinable.</li>
     * </ul>
     * </p>
     *
     * @param left The first input property, which must be non-{@code null}.
     * @param right The second input property, which must be non-{@code null}.
     * @return {@code true} if and only if {@code left} and {@code right} are combinable.
     */
    protected abstract boolean computeAreCombinable(T left, T right);

    /**
     * Computes the combination of {@code left} and {@code right}, which are required to be combinable with respect to
     * {@link #areCombinable(Object, Object) areCombinable(T, T)}.
     *
     * <p>
     * This binary operation must be implemented to satisfy the following four properties:
     * <ul>
     * <li><u>Combinability preserving</u>: for every two combinable properties <i>e1</i> and <i>e2</i> it must hold
     * that both these properties are again combinable with <i>combine(e1, e2)</i>.</li>
     * <li><u>Associative:</u> for every three combinable properties <i>e1</i>, <i>e2</i> and <i>e3</i> it holds that
     * <i>combine(e1, combine(e2, e3)) = combine(combine(e1, e2), e3)</i>.</li>
     * <li><u>Commutative:</u> for every two combinable properties <i>e1</i> and <i>e2</i> it holds that <i>combine(e1,
     * e2) = combine(e2, e1)</i>.</li>
     * </ul>
     * </p>
     *
     * @param left The first input property, which must be non-{@code null}.
     * @param right The second input property, which must be non-{@code null}.
     * @return The combination of {@code left} and {@code right}, which is non-{@code null}.
     */
    protected abstract T computeCombination(T left, T right);

    /**
     * Determines whether {@code left} and {@code right} can be combined (joined together) into a single property. This
     * is an equivalence relation over {@code T}-typed properties.
     *
     * @param left The first input property, which must be non-{@code null}.
     * @param right The second input property, which must be non-{@code null}.
     * @return {@code true} if and only if {@code left} and {@code right} are combinable.
     */
    public final boolean areCombinable(T left, T right) {
        Preconditions.checkNotNull(left, "Expected the left operand to be non-null.");
        Preconditions.checkNotNull(right, "Expected the right operand to be non-null.");
        return computeAreCombinable(left, right);
    }

    /**
     * Determines whether the properties in {@code properties} are all combinable with each other, with respect to
     * {@link #areCombinable(Object, Object) areCombinable(T, T)}.
     *
     * @param properties The input collection of properties, all of which must be non-{@code null}.
     * @return {@code true} if and only if every pair of properties in {@code properties} is combinable.
     */
    public final boolean areCombinable(Collection<T> properties) {
        Optional<T> property = properties.stream().findAny();
        return property.isPresent() ? properties.stream().allMatch(other -> areCombinable(property.get(), other))
                : true;
    }

    /**
     * Combines {@code left} and {@code right} into a single {@code T}-typed property.
     *
     * <p>
     * This operation requires that {@code left} and {@code right} are <u>combinable</u> with respect to
     * {@link #areCombinable(Object, Object) areCombinable(T, T)}.
     * </p>
     *
     * @param left The first input property, which must be non-{@code null}.
     * @param right The second input property, which must be non-{@code null}.
     * @return The combination of {@code left} and {@code right}, which is non-{@code null}.
     */
    public final T combine(T left, T right) {
        Preconditions.checkNotNull(left, "Expected the left operand to be non-null.");
        Preconditions.checkNotNull(right, "Expected the right operand to be non-null.");
        Preconditions.checkArgument(areCombinable(left, right), "Expected combinable properties.");

        T combination = computeCombination(left, right);
        Preconditions.checkNotNull(combination, "Expected the combined property to be non-null.");
        return combination;
    }

    /**
     * Combines all properties in {@code properties} into a single {@code T}-typed property using
     * {@link #combine(Object, Object) combine(T, T)}.
     *
     * <p>
     * This operation requires that {@code properties} is <u>not empty</u>, and that all properties in
     * {@code properties} <u>are combinable</u> with each other.
     * </p>
     *
     * @param properties The collection of combinable properties, all of which must be non-{@code null}.
     * @return The combination of all properties in {@code properties}, which is non-{@code null}.
     */
    public final T combine(Collection<T> properties) {
        return combine(properties.stream());
    }

    /**
     * Combines all properties in the given {@code stream} into a single {@code T}-typed property using
     * {@link #combine(Object, Object) combine(T, T)}.
     *
     * <p>
     * This operation requires that {@code stream} is <u>finite</u> and <u>not empty</u>. Moreover, this operation
     * requires that all properties in {@code stream} are <u>all combinable</u> with each other.
     * </p>
     *
     * @param stream The input stream of combinable properties, all of which must be non-{@code null}.
     * @return The combination of all properties in {@code stream}, which is non-{@code null}.
     */
    public final T combine(Stream<T> stream) {
        Optional<T> combination = stream.reduce((left, right) -> combine(left, right));
        Preconditions.checkArgument(combination.isPresent(), "Expected a non-empty stream.");
        return combination.get();
    }
}
