//////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2021-2023 Contributors to the GitHub community
//
// This program and the accompanying materials are made available
// under the terms of the MIT License which is available at
// https://opensource.org/licenses/MIT
//
// SPDX-License-Identifier: MIT
//////////////////////////////////////////////////////////////////////////////

package com.github.tno.gltsdiff.rewriters;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Stack;
import java.util.function.Function;

import com.github.tno.gltsdiff.glts.GLTS;
import com.github.tno.gltsdiff.glts.State;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

/**
 * Common functionality for rewriting (abstract) skip patterns.
 *
 * @param <S> The type of state properties.
 * @param <T> The type of transition properties.
 * @param <U> The type of GLTSs to rewrite.
 */
public abstract class SkipPatternRewriter<S, T, U extends GLTS<S, T>> implements Rewriter<S, T, U> {
    /**
     * Determines whether there exists structure between {@code source} and {@code target} in {@code glts} that can
     * intuitively be skipped. Such structure exists if either <b>(1)</b> there exists a pattern of intuitively
     * skippable structure, or <b>(2)</b> there are only direct transitions from {@code source} to {@code target}.
     *
     * <p>
     * A <u>pattern of intuitively skippable structure</u> is defined to be a non-empty set <i>X</i> of states such that
     * <ol>
     * <li><i>X</i> does not include {@code source}, {@code target} nor any states in {@code forbidden}.</li>
     * <li>All states in <i>X</i> are reachable from {@code source} without using {@code forbidden} states.</li>
     * <li>All states in <i>X</i> are co-reachable from {@code target} without using {@code forbidden} states.</li>
     * <li>All predecessors of states in <i>X</i> must be included in '<i>X</i> union {@code source}'.</li>
     * <li>All successors of states in <i>X</i> must be included in '<i>X</i> union {@code target}'.</li>
     * </ol>
     * </p>
     *
     * <p>
     * Note that this function does not check entire (fork/join) skip patterns, but just the parts of them that may (or
     * may not) be intuitively skippable. For example, in case of fork skip patterns, this function checks if the
     * pattern without the first two edges (that form the fork) can intuitively be skipped. In case of join skip
     * patterns, it checks if the pattern without the last two edges (that form the join) can intuitively be skipped.
     * </p>
     *
     * @param glts The contextual input GLTS.
     * @param source The source state, which must not be the same as {@code target}.
     * @param target The target state, which must not be the same as {@code source}.
     * @param forbidden The set of states that is not to be considered when determining if there is skippable structure.
     *     This set must not include {@code source} nor {@code target}.
     * @return {@code true} if there exists intuitively skippable structure as described above, {@code false} otherwise.
     */
    protected final boolean existSkippableStructure(U glts, State<S> source, State<S> target, Set<State<S>> forbidden) {
        Preconditions.checkArgument(glts.getStates().containsAll(ImmutableSet.of(source, target)),
                "Expected the given GLTS to contain the specified source and target states.");
        Preconditions.checkArgument(source != target, "Expected the given source and target states to not be equal.");
        Preconditions.checkArgument(Sets.intersection(forbidden, ImmutableSet.of(source, target)).isEmpty(),
                "Expected the given source and target states to not be forbidden themselves.");

        // If 'target' is not reachable from 'source' without using 'forbidden' states, then there is nothing to skip.
        Set<State<S>> reachable = explore(source, state -> glts.getSuccessorsOf(state),
                Sets.union(ImmutableSet.of(target), forbidden));

        if (!reachable.contains(target)) {
            return false;
        }

        // Determine the set of states reachable from 'source' and co-reachable from 'target', up to 'forbidden' states.
        Set<State<S>> coreachable = explore(target, state -> glts.getPredecessorsOf(state),
                Sets.union(ImmutableSet.of(source), forbidden));
        Set<State<S>> trim = Sets.intersection(reachable, coreachable);

        Preconditions.checkArgument(trim.contains(source),
                "Expected the source state to be co-reachable from the target state.");
        Preconditions.checkArgument(trim.contains(target),
                "Expected the target state to be reachable from the source state.");

        // If 'trim' contains only 'source' and 'target', then there are only direct transitions between these states.
        if (trim.size() == 2) {
            return true;
        }

        // Otherwise, determine the set 'X' of (intermediate) states, as described above in the JavaDoc.
        Set<State<S>> intermediate = Sets.difference(trim, Sets.union(forbidden, ImmutableSet.of(source, target)));

        // Recall from the JavaDoc that this set 'X' is required to be non-empty.
        if (intermediate.isEmpty()) {
            return false;
        }

        // All that is left to determine if 'intermediate' is a pattern of intuitively skippable structure,
        // is checking that this set cannot be entered (externally) via any state other than 'source',
        // and cannot be left via any state other than 'target'. (Corresponding to the last two bullets in the JavaDoc.)
        Set<State<S>> intermediateAndSource = Sets.union(intermediate, ImmutableSet.of(source));
        Set<State<S>> intermediateAndTarget = Sets.union(intermediate, ImmutableSet.of(target));

        return intermediate.stream().allMatch(state -> intermediateAndSource.containsAll(glts.getPredecessorsOf(state))
                && intermediateAndTarget.containsAll(glts.getSuccessorsOf(state)));
    }

    /**
     * Performs a DFS starting from {@code source} where {@code nextStates} is used as the next-state function. This DFS
     * will not explore beyond {@code barrier} states. So if any state is encountered that is in {@code barrier}, then
     * its next states will not be explored and the exploration of the barrier state will finish immediately.
     *
     * @param source The state from which the DFS exploration starts.
     * @param nextStates The next-state function.
     * @param barrier The set of barrier states, forming a fence for the DFS exploration.
     * @return The set of all visited states, which may include states of {@code barrier}.
     */
    private Set<State<S>> explore(State<S> source, Function<State<S>, Set<State<S>>> nextStates,
            Set<State<S>> barrier)
    {
        Set<State<S>> visited = new LinkedHashSet<>();
        Stack<State<S>> stack = new Stack<>();
        stack.push(source);

        while (!stack.isEmpty()) {
            State<S> current = stack.pop();

            if (visited.contains(current)) {
                continue;
            }

            visited.add(current);

            if (!barrier.contains(current)) {
                for (State<S> next: nextStates.apply(current)) {
                    if (!visited.contains(next)) {
                        stack.push(next);
                    }
                }
            }
        }

        return visited;
    }
}
