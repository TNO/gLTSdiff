//////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2021-2023 Contributors to the GitHub community
//
// This program and the accompanying materials are made available
// under the terms of the MIT License which is available at
// https://opensource.org/licenses/MIT
//
// SPDX-License-Identifier: MIT
//////////////////////////////////////////////////////////////////////////////

package com.github.tno.gltsdiff;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import com.github.tno.gltsdiff.glts.GLTS;
import com.github.tno.gltsdiff.glts.State;
import com.github.tno.gltsdiff.matchers.Matcher;
import com.github.tno.gltsdiff.mergers.Merger;
import com.github.tno.gltsdiff.rewriters.Rewriter;
import com.google.common.base.Preconditions;

/**
 * Structural comparator that allows comparing two or more GLTSs and merging them into a single GLTS that highlights
 * their differences.
 *
 * <p>
 * When comparing GLTSs, we refer to them as the left-hand-side (LHS) and the right-hand-side (RHS). They are compared
 * and merged into a single GLTS.
 * </p>
 *
 * <p>
 * When comparing more than two GLTSs, first the first two GLTSs are compare and merged. Then the result is compared and
 * merged with the third GLTS. Subsequently, that result is compared and merged with the fourth GLTS, and so on.
 * </p>
 *
 * @param <S> The type of state properties.
 * @param <T> The type of transition properties.
 * @param <U> The type of GLTSs to compare and merge.
 */
public class StructureComparator<S, T, U extends GLTS<S, T>> {
    /** The matcher that determines which (LHS, RHS)-state pairs should be merged into a single state. */
    private final Matcher<S, T, U> matcher;

    /** The merger that merges LHS and RHS into a single GLTS. */
    private final Merger<S, T, U> merger;

    /** The rewriter for rewriting the merged GLTS. */
    private final Rewriter<S, T, U> rewriter;

    /**
     * Initializes a structural comparator.
     *
     * @param matcher The matcher that determines which (LHS, RHS)-state pairs should be merged into a single state.
     * @param merger The merger that merges LHS and RHS into a single GLTS.
     * @param rewriter The rewriter for rewriting the merged GLTS.
     */
    public StructureComparator(Matcher<S, T, U> matcher, Merger<S, T, U> merger, Rewriter<S, T, U> rewriter) {
        this.matcher = matcher;
        this.merger = merger;
        this.rewriter = rewriter;
    }

    /**
     * Compare and merge two GLTSs.
     *
     * @param lhs The left-hand-side (LHS) GLTS.
     * @param rhs The right-hand-side (RHS) GLTS.
     * @return The GLTS produced by comparing and merging the LHS and RHS.
     */
    public U compare(U lhs, U rhs) {
        return compare(Stream.of(lhs, rhs));
    }

    /**
     * Compare and merge two or more GLTSs.
     *
     * @param gltss The GLTSs. At least two GLTSs must be given.
     * @return The GLTS produced by comparing and merging the GLTSs.
     */
    @SuppressWarnings("unchecked")
    public U compare(U... gltss) {
        return compare(Arrays.stream(gltss));
    }

    /**
     * Compare and merge two or more GLTSs.
     *
     * @param gltss The GLTSs. At least two GLTSs must be given.
     * @return The GLTS produced by comparing and merging the GLTSs.
     */
    public U compare(Collection<U> gltss) {
        return compare(gltss.stream());
    }

    /**
     * Compare and merge two or more GLTSs.
     *
     * @param gltss The GLTSs. At least two GLTSs must be given.
     * @return The GLTS produced by comparing and merging the GLTSs.
     */
    public U compare(Stream<U> gltss) {
        // Compare and merge.
        Optional<U> optionalResult = gltss.reduce(this::compareInternal);
        Preconditions.checkArgument(optionalResult.isPresent(), "Expected to compare at least two GLTSs to compare.");
        U result = optionalResult.get();

        // Rewrite, as post-processing.
        rewriter.rewrite(result);

        // Return result.
        return result;
    }

    /**
     * Compare and merge two GLTSs.
     *
     * @param lhs The left-hand-side (LHS) GLTS.
     * @param rhs The right-hand-side (RHS) GLTS.
     * @return The GLTS produced by comparing and merging the LHS and RHS.
     */
    private U compareInternal(U lhs, U rhs) {
        Map<State<S>, State<S>> matching = matcher.compute(lhs, rhs);
        return merger.merge(lhs, rhs, matching);
    }
}
