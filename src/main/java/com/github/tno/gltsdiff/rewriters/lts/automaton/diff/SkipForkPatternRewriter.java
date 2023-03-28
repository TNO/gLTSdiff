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
import com.github.tno.gltsdiff.glts.lts.automaton.diff.DiffAutomaton;
import com.github.tno.gltsdiff.glts.lts.automaton.diff.DiffAutomatonStateProperty;
import com.github.tno.gltsdiff.glts.lts.automaton.diff.DiffProperty;
import com.github.tno.gltsdiff.operators.combiners.Combiner;
import com.github.tno.gltsdiff.operators.combiners.lts.automaton.diff.DiffAutomatonStatePropertyCombiner;
import com.github.tno.gltsdiff.operators.hiders.Hider;
import com.github.tno.gltsdiff.operators.inclusions.Inclusion;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

/**
 * A rewriter that rewrites skip fork patterns in {@link DiffAutomaton difference automata}.
 *
 * @param <T> The type of transition properties.
 */
public class SkipForkPatternRewriter<T>
        extends SkipPatternRewriter<DiffAutomatonStateProperty, DiffProperty<T>, DiffAutomaton<T>>
{
    /** The combiner for state properties. */
    private final Combiner<DiffAutomatonStateProperty> statePropertyCombiner = new DiffAutomatonStatePropertyCombiner();

    /** The combiner for transition properties. */
    private final Combiner<DiffProperty<T>> transitionPropertyCombiner;

    /** The hider for transition properties. */
    private final Hider<DiffProperty<T>> transitionPropertyHider;

    /** The transition property inclusion operator. */
    private final Inclusion<DiffProperty<T>> inclusion;

    /**
     * Instantiates a new rewriter for rewriting skip fork patterns in difference automata.
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
     * @param transitionPropertyCombiner The combiner for transition properties.
     * @param transitionPropertyHider The hider for transition properties.
     * @param inclusion The transition property inclusion operator.
     */
    public SkipForkPatternRewriter(Combiner<DiffProperty<T>> transitionPropertyCombiner,
            Hider<DiffProperty<T>> transitionPropertyHider, Inclusion<DiffProperty<T>> inclusion)
    {
        this.transitionPropertyCombiner = transitionPropertyCombiner;
        this.transitionPropertyHider = transitionPropertyHider;
        this.inclusion = inclusion;
    }

    @Override
    public boolean rewrite(DiffAutomaton<T> diff) {
        boolean effective = false;

        for (State<DiffAutomatonStateProperty> state: diff.getStates()) {
            effective |= apply(diff, state);
        }

        return effective;
    }

    /**
     * Rewrites all skip fork patterns in {@code diff} that originate from {@code state}.
     *
     * @param diff The difference automaton to be rewritten.
     * @param state The potential origin state of skip fork patterns.
     * @return {@code true} if any rewrites have been performed, {@code false} otherwise.
     */
    private boolean apply(DiffAutomaton<T> diff, State<DiffAutomatonStateProperty> state) {
        boolean effective = false;

        List<Transition<DiffAutomatonStateProperty, DiffProperty<T>>> transitions = new LinkedList<>(
                diff.getOutgoingTransitions(state));

        for (Transition<DiffAutomatonStateProperty, DiffProperty<T>> left: transitions) {
            for (Transition<DiffAutomatonStateProperty, DiffProperty<T>> right: transitions) {
                if (!left.equals(right) && isSkipForkPattern(diff, left, right)) {
                    rewriteSkipForkPattern(diff, left, right);
                    effective = true;
                }
            }
        }

        return effective;
    }

    /**
     * Determines whether {@code left} and {@code right} together form a valid skip fork pattern. If so, then these two
     * transitions can be rewritten by {@link #rewriteSkipForkPattern} without changing the language of {@code diff}.
     *
     * <p>
     * The input transitions form a skip fork pattern if:
     * </p>
     * <ol>
     * <li>{@code left} and {@code right} have the same source state, and have different target states (i.e., they form
     * a proper fork pattern).</li>
     * <li>{@code left} and {@code right} have combinable transition properties.</li>
     * <li>There exists a path from the target state of {@code right} to the target state of {@code left} (that does not
     * include the shared source state of {@code left} and {@code right}).</li>
     * <li>All structure (states and transitions) that is in-between the target state of {@code right} and the target
     * state of {@code left} can intuitively be skipped, from the perspective of the shared source state.</li>
     * <li>The initial state information, state acceptance information, difference information, incoming/outgoing
     * transitions, etc., of the source and target states of {@code left} and {@code right} are such that the
     * introduction of a new skip transition would not change the language of {@code diff}.</li>
     * </ol>
     *
     * @param diff The contextual input difference automaton.
     * @param left The left transition, which must be different from {@code right} but must have the same source state.
     * @param right The right transition, which must be different from {@code left} but must have the same source state.
     * @return {@code true} if {@code left} and {@code right} together form a valid skip fork pattern.
     */
    private boolean isSkipForkPattern(DiffAutomaton<T> diff,
            Transition<DiffAutomatonStateProperty, DiffProperty<T>> left,
            Transition<DiffAutomatonStateProperty, DiffProperty<T>> right)
    {
        Preconditions.checkArgument(!left.equals(right), "Expected the two given transitions to be different.");

        // The transition properties of 'left' and 'right' must be combinable.
        if (!transitionPropertyCombiner.areCombinable(left.getProperty(), right.getProperty())) {
            return false;
        }

        Preconditions.checkArgument(left.getSource() == right.getSource(),
                "Expected the two given transitions to have the same source state.");

        State<DiffAutomatonStateProperty> source = left.getSource();
        State<DiffAutomatonStateProperty> leftTarget = left.getTarget();
        State<DiffAutomatonStateProperty> rightTarget = right.getTarget();

        // It is required that 'source', 'leftTarget' and 'rightTarget' are all different states.
        // Otherwise this skip fork pattern would either contain self-loops, or patterns of local redundancy.
        if (ImmutableSet.of(source, leftTarget, rightTarget).size() != 3) {
            return false;
        }

        // The 'rightTarget' must not be initial, otherwise adding the skip transition might not be language preserving.
        if (diff.getInitialStates().contains(rightTarget)) {
            return false;
        }

        // The 'leftTarget' and 'rightTarget' must have combinable properties.
        if (!statePropertyCombiner.areCombinable(leftTarget.getProperty(), rightTarget.getProperty())) {
            return false;
        }

        // The 'rightTarget' state must not have any incoming transitions other than 'right'.
        // Otherwise adding the skip transition may change the languages of projections of 'diff'.
        if (diff.getIncomingTransitions(rightTarget).size() > 1) {
            return false;
        }

        // The difference kinds of all outgoing transitions out of 'rightTarget' must be included in the difference kind
        // of 'right', modulo non-hidable properties. Otherwise upgrading 'right' may be incorrect.
        if (!diff.getOutgoingTransitionProperties(rightTarget).stream()
                .allMatch(property -> inclusion.isIncludedIn(transitionPropertyHider.hide(property),
                        transitionPropertyHider.hide(right.getProperty()), transitionPropertyCombiner)))
        {
            return false;
        }

        // Check whether there is skippable structure in between 'rightTarget' and 'leftTarget'.
        return existSkippableStructure(diff, rightTarget, leftTarget, ImmutableSet.of(source));
    }

    /**
     * Rewrites the fork pattern that is formed by {@code left} and {@code right}. This rewriter assumes that
     * {@code left} and {@code right} form a skip fork pattern with respect to {@link #isSkipForkPattern}.
     *
     * @param diff The contextual input difference automaton.
     * @param left The left transition, which must have the same source state as {@code right}.
     * @param right The right transition, which must have the same source state as {@code left}.
     */
    private void rewriteSkipForkPattern(DiffAutomaton<T> diff,
            Transition<DiffAutomatonStateProperty, DiffProperty<T>> left,
            Transition<DiffAutomatonStateProperty, DiffProperty<T>> right)
    {
        Preconditions.checkArgument(left.getSource() == right.getSource(),
                "Expected the two given transitions to have the same source state.");

        State<DiffAutomatonStateProperty> source = left.getSource();
        State<DiffAutomatonStateProperty> leftTarget = left.getTarget();
        State<DiffAutomatonStateProperty> rightTarget = right.getTarget();

        Preconditions.checkArgument(ImmutableSet.of(source, leftTarget, rightTarget).size() == 3,
                "Expected the fork pattern to consist of three different non-overlapping states.");

        // 1. Upgrade the property of 'rightTarget'.
        DiffAutomatonStateProperty rightTargetProperty = rightTarget.getProperty();
        DiffAutomatonStateProperty otherProperty = new DiffAutomatonStateProperty(rightTargetProperty.isAccepting(),
                left.getProperty().getDiffKind(), Optional.empty());
        diff.setStateProperty(rightTarget, statePropertyCombiner.combine(rightTargetProperty, otherProperty));

        // 2. Upgrade the property of the 'right' transition.
        diff.removeTransition(right);
        diff.addTransition(source, transitionPropertyCombiner.combine(left.getProperty(), right.getProperty()),
                rightTarget);

        // 3. Add the skip transition.
        diff.addTransition(rightTarget, transitionPropertyHider.hide(left.getProperty()), leftTarget);

        // 4. Remove the 'left' transition.
        diff.removeTransition(left);
    }
}
