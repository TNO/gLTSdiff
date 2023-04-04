//////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2021-2023 Contributors to the GitHub community
//
// This program and the accompanying materials are made available
// under the terms of the MIT License which is available at
// https://opensource.org/licenses/MIT
//
// SPDX-License-Identifier: MIT
//////////////////////////////////////////////////////////////////////////////

package com.github.tno.gltsdiff.rewriters.lts.automaton.diff;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import com.github.tno.gltsdiff.glts.State;
import com.github.tno.gltsdiff.glts.Transition;
import com.github.tno.gltsdiff.glts.lts.automaton.diff.BaseDiffAutomaton;
import com.github.tno.gltsdiff.glts.lts.automaton.diff.DiffAutomatonStateProperty;
import com.github.tno.gltsdiff.glts.lts.automaton.diff.DiffKind;
import com.github.tno.gltsdiff.glts.lts.automaton.diff.DiffProperty;
import com.github.tno.gltsdiff.operators.combiners.Combiner;
import com.github.tno.gltsdiff.operators.hiders.Hider;
import com.github.tno.gltsdiff.operators.inclusions.Inclusion;
import com.github.tno.gltsdiff.utils.TriFunction;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

/**
 * A rewriter that rewrites skip join patterns in {@link BaseDiffAutomaton difference automata}.
 *
 * @param <S> The type of difference automaton state properties.
 * @param <T> The type of transition properties.
 * @param <U> The type of difference automata to rewrite.
 */
public class SkipJoinPatternRewriter<S extends DiffAutomatonStateProperty, T, U extends BaseDiffAutomaton<S, T>>
        extends SkipPatternRewriter<S, T, U>
{
    /** The combiner for state properties. */
    private final Combiner<S> statePropertyCombiner;

    /** The combiner for transition properties. */
    private final Combiner<DiffProperty<T>> transitionPropertyCombiner;

    /** The hider for transition properties. */
    private final Hider<DiffProperty<T>> transitionPropertyHider;

    /** The transition property inclusion operator. */
    private final Inclusion<DiffProperty<T>> inclusion;

    /**
     * Instantiates a new rewriter for rewriting skip join patterns in difference automata.
     *
     * <p>
     * The specified {@code transitionPropertyCombiner} and {@code transitionPropertyHider} are <u>required to
     * interact</u> in the following ways:
     * </p>
     * <ol>
     * <li>All hidden properties are combinable with each other: for any <i>e1</i> and <i>e2</i>, if <i>hidden(e1)</i>
     * and <i>hidden(e2)</i> then <i>combinable(e1, e2)</i>.</li>
     * <li>The combination of any two hidden properties is again hidden: for any <i>e1</i> and <i>e2</i>, if
     * <i>hidden(e1)</i> and <i>hidden(e2)</i> then <i>hidden(combine(e1, e2))</i>.</li>
     * </ol>
     *
     * @param statePropertyCombiner The combiner for state properties.
     * @param transitionPropertyCombiner The combiner for transition properties.
     * @param transitionPropertyHider The hider for transition properties.
     * @param inclusion The transition property inclusion operator.
     * @param statePropertyTransformer Function to transform a difference automaton state property. Given an existing
     *     difference automaton state property, a new state difference kind, and a new initial state difference kind
     *     (present if state is an initial state, absent otherwise), returns a difference automaton state property that
     *     has the new (initial) state difference kinds. The function should not modify the existing state property.
     */
    public SkipJoinPatternRewriter(Combiner<S> statePropertyCombiner,
            Combiner<DiffProperty<T>> transitionPropertyCombiner, Hider<DiffProperty<T>> transitionPropertyHider,
            Inclusion<DiffProperty<T>> inclusion,
            TriFunction<S, DiffKind, Optional<DiffKind>, S> statePropertyTransformer)
    {
        super(statePropertyTransformer);
        this.statePropertyCombiner = statePropertyCombiner;
        this.transitionPropertyCombiner = transitionPropertyCombiner;
        this.transitionPropertyHider = transitionPropertyHider;
        this.inclusion = inclusion;
    }

    @Override
    public boolean rewrite(U diff) {
        boolean effective = false;

        for (State<S> state: diff.getStates()) {
            effective |= apply(diff, state);
        }

        return effective;
    }

    /**
     * Rewrites all skip join patterns in {@code diff} that originate from {@code state}.
     *
     * @param diff The difference automaton to be rewritten.
     * @param state The potential origin state of skip join patterns.
     * @return {@code true} if any rewrites have been performed, {@code false} otherwise.
     */
    private boolean apply(U diff, State<S> state) {
        boolean effective = false;

        List<Transition<S, DiffProperty<T>>> transitions = new LinkedList<>(diff.getIncomingTransitions(state));

        for (Transition<S, DiffProperty<T>> left: transitions) {
            for (Transition<S, DiffProperty<T>> right: transitions) {
                if (!left.equals(right) && isSkipJoinPattern(diff, left, right)) {
                    rewriteSkipJoinPattern(diff, left, right);
                    effective = true;
                }
            }
        }

        return effective;
    }

    /**
     * Determines whether {@code left} and {@code right} together form a valid skip join pattern. If so, then these two
     * transitions can be rewritten by {@link #rewriteSkipJoinPattern} without changing the language of {@code diff}.
     *
     * <p>
     * The input transitions form a skip join pattern if:
     * </p>
     * <ol>
     * <li>{@code left} and {@code right} have the same target state, and have different source states (i.e., they form
     * a proper join pattern).</li>
     * <li>{@code left} and {@code right} have combinable transition properties.</li>
     * <li>There exists a path from the source state of {@code left} to the source state of {@code right} (that does not
     * include the shared target state of {@code left} and {@code right}).</li>
     * <li>All structure (states and transitions) that is in-between the source state of {@code left} and the source
     * state of {@code right} can intuitively be skipped, from the perspective of the shared target state.</li>
     * <li>The initial state information, state acceptance information, difference information, incoming/outgoing
     * transitions, etc., of the source and target states of {@code left} and {@code right} are such that the
     * introduction of a new skip transition would not change the language of {@code diff}.</li>
     * </ol>
     *
     * @param diff The contextual input difference automaton.
     * @param left The left transition, which must be different from {@code right} but must have the same target state.
     * @param right The right transition, which must be different from {@code left} but must have the same target state.
     * @return {@code true} if {@code left} and {@code right} together form a valid skip join pattern.
     */
    private boolean isSkipJoinPattern(U diff, Transition<S, DiffProperty<T>> left,
            Transition<S, DiffProperty<T>> right)
    {
        Preconditions.checkArgument(!left.equals(right), "Expected the two given transitions to be different.");

        // The transition properties of 'left' and 'right' must be combinable.
        if (!transitionPropertyCombiner.areCombinable(left.getProperty(), right.getProperty())) {
            return false;
        }

        Preconditions.checkArgument(left.getTarget() == right.getTarget(),
                "Expected the two given transitions to have the same target state.");

        State<S> leftSource = left.getSource();
        State<S> rightSource = right.getSource();
        State<S> target = left.getTarget();

        // It is required that 'leftSource', 'rightSource' and 'target' are all different states.
        // Otherwise this skip join pattern would either contain self-loops, or patterns of local redundancy.
        if (ImmutableSet.of(leftSource, rightSource, target).size() != 3) {
            return false;
        }

        // The 'rightSource' must not be initial, otherwise adding the skip transition might not be language preserving.
        if (diff.getInitialStates().contains(rightSource)) {
            return false;
        }

        // The 'leftSource' and 'rightSource' must have combinable properties.
        if (!statePropertyCombiner.areCombinable(leftSource.getProperty(), rightSource.getProperty())) {
            return false;
        }

        // The 'rightSource' state must not have any outgoing transitions other than 'right'.
        // Otherwise adding the skip transition may change the languages of projections of 'diff'.
        if (diff.getOutgoingTransitions(rightSource).size() > 1) {
            return false;
        }

        // The difference kinds of all incoming transitions into 'rightSource' must be included in the difference kind
        // of 'right', modulo non-hidable properties. Otherwise upgrading 'right' may be incorrect.
        if (!diff.getIncomingTransitionProperties(rightSource).stream()
                .allMatch(property -> inclusion.isIncludedIn(transitionPropertyHider.hide(property),
                        transitionPropertyHider.hide(right.getProperty()), transitionPropertyCombiner)))
        {
            return false;
        }

        // Check whether there is skippable structure in between 'leftSource' and 'rightSource'.
        return existSkippableStructure(diff, leftSource, rightSource, ImmutableSet.of(target));
    }

    /**
     * Rewrites the join pattern that is formed by {@code left} and {@code right}. This rewriter assumes that
     * {@code left} and {@code right} form a skip join pattern with respect to {@link #isSkipJoinPattern}.
     *
     * @param diff The contextual input difference automaton.
     * @param left The left transition, which must have the same target state as {@code right}.
     * @param right The right transition, which must have the same target state as {@code left}.
     */
    private void rewriteSkipJoinPattern(U diff, Transition<S, DiffProperty<T>> left,
            Transition<S, DiffProperty<T>> right)
    {
        Preconditions.checkArgument(left.getTarget() == right.getTarget(),
                "Expected the two given transitions to have the same target state.");

        State<S> leftSource = left.getSource();
        State<S> rightSource = right.getSource();
        State<S> target = left.getTarget();

        Preconditions.checkArgument(ImmutableSet.of(leftSource, rightSource, target).size() == 3,
                "Expected the join pattern to consist of three different non-overlapping states.");

        // 1. Upgrade the property of 'rightSource'.
        S rightSourceProperty = rightSource.getProperty();
        S otherProperty = statePropertyTransformer.apply(rightSourceProperty, left.getProperty().getDiffKind(),
                Optional.empty());
        diff.setStateProperty(rightSource, statePropertyCombiner.combine(rightSourceProperty, otherProperty));

        // 2. Upgrade the property of the 'right' transition.
        diff.removeTransition(right);
        diff.addTransition(rightSource, transitionPropertyCombiner.combine(left.getProperty(), right.getProperty()),
                target);

        // 3. Add the skip transition.
        diff.addTransition(leftSource, transitionPropertyHider.hide(left.getProperty()), rightSource);

        // 4. Remove the 'left' transition.
        diff.removeTransition(left);
    }
}
