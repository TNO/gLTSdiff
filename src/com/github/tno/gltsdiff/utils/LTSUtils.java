//////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2021-2022 Contributors to the GitHub community
//
// This program and the accompanying materials are made available
// under the terms of the MIT License which is available at
// https://opensource.org/licenses/MIT
//
// SPDX-License-Identifier: MIT
//////////////////////////////////////////////////////////////////////////////

package com.github.tno.gltsdiff.utils;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.math3.util.Pair;

import com.github.tno.gltsdiff.lts.LTS;
import com.github.tno.gltsdiff.lts.State;
import com.github.tno.gltsdiff.lts.Transition;
import com.github.tno.gltsdiff.operators.combiners.Combiner;

/** Utilities for working with LTSs. */
public class LTSUtils {
    private LTSUtils() {
    }

    /**
     * Determines the set of all pairs of combinable ({@code left}, {@code right})-transitions that go into
     * {@code statePair}, which is a pair of ({@code left}, {@code right})-states.
     * 
     * @param <S> The type of state properties.
     * @param <T> The type of transition properties.
     * @param left The left LTS.
     * @param right The right LTS.
     * @param combiner The combiner for transition properties.
     * @param statePair The state pair for which all common combinable incoming transitions are to be found.
     * @return The set of all pairs of combinable incoming transitions into {@code statePair}.
     */
    public static <S, T> Set<Pair<Transition<S, T>, Transition<S, T>>> commonIncomingTransitions(LTS<S, T> left,
            LTS<S, T> right, Combiner<T> combiner, Pair<State<S>, State<S>> statePair)
    {
        Set<Pair<Transition<S, T>, Transition<S, T>>> transitions = new LinkedHashSet<>();

        for (Transition<S, T> leftTransition: left.getIncomingTransitions(statePair.getFirst())) {
            for (Transition<S, T> rightTransition: right.getIncomingTransitions(statePair.getSecond())) {
                if (combiner.areCombinable(leftTransition.getProperty(), rightTransition.getProperty())) {
                    transitions.add(Pair.create(leftTransition, rightTransition));
                }
            }
        }

        return transitions;
    }

    /**
     * Determines the set of all pairs of combinable ({@code left}, {@code right})-transitions that go out of
     * {@code statePair}, which is a pair of ({@code left}, {@code right})-states.
     * 
     * @param <S> The type of state properties.
     * @param <T> The type of transition properties.
     * @param left The left LTS.
     * @param right The right LTS.
     * @param combiner The combiner for transition properties.
     * @param statePair The state pair for which all common combinable outgoing transitions are to be found.
     * @return The set of all pairs of combinable outgoing transitions out of {@code statePair}.
     */
    public static <S, T> Set<Pair<Transition<S, T>, Transition<S, T>>> commonOutgoingTransitions(LTS<S, T> left,
            LTS<S, T> right, Combiner<T> combiner, Pair<State<S>, State<S>> statePair)
    {
        Set<Pair<Transition<S, T>, Transition<S, T>>> transitions = new LinkedHashSet<>();

        for (Transition<S, T> leftTransition: left.getOutgoingTransitions(statePair.getFirst())) {
            for (Transition<S, T> rightTransition: right.getOutgoingTransitions(statePair.getSecond())) {
                if (combiner.areCombinable(leftTransition.getProperty(), rightTransition.getProperty())) {
                    transitions.add(Pair.create(leftTransition, rightTransition));
                }
            }
        }

        return transitions;
    }

    /**
     * Determines the set of all pairs of ({@code left}, {@code right})-states that can reach {@code statePair} by a
     * common combinable incoming transition.
     * 
     * @param <S> The type of state properties.
     * @param <T> The type of transition properties.
     * @param left The left LTS.
     * @param right The right LTS.
     * @param combiner The combiner for transition properties.
     * @param statePair The state pair for which all common predecessors are to be found.
     * @return The set of all common predecessors of {@code statePair}.
     */
    public static <S, T> Set<Pair<State<S>, State<S>>> commonPredecessors(LTS<S, T> left, LTS<S, T> right,
            Combiner<T> combiner, Pair<State<S>, State<S>> statePair)
    {
        return commonIncomingTransitions(left, right, combiner, statePair).stream()
                .map(pair -> Pair.create(pair.getFirst().getSource(), pair.getSecond().getSource()))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * Determines the set of all pairs of ({@code left}, {@code right})-states that {@code statePair} can reach by a
     * common combinable outgoing transition.
     * 
     * @param <S> The type of state properties.
     * @param <T> The type of transition properties.
     * @param left The left LTS.
     * @param right The right LTS.
     * @param combiner The combiner for transition properties.
     * @param statePair The state pair for which all common successors are to be found.
     * @return The set of all common successors of {@code statePair}.
     */
    public static <S, T> Set<Pair<State<S>, State<S>>> commonSuccessors(LTS<S, T> left, LTS<S, T> right,
            Combiner<T> combiner, Pair<State<S>, State<S>> statePair)
    {
        return commonOutgoingTransitions(left, right, combiner, statePair).stream()
                .map(pair -> Pair.create(pair.getFirst().getTarget(), pair.getSecond().getTarget()))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
