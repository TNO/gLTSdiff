//////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2021-2023 Contributors to the GitHub community
//
// This program and the accompanying materials are made available
// under the terms of the MIT License which is available at
// https://opensource.org/licenses/MIT
//
// SPDX-License-Identifier: MIT
//////////////////////////////////////////////////////////////////////////////

package com.github.tno.gltsdiff.utils;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.math3.util.Pair;

import com.github.tno.gltsdiff.glts.GLTS;
import com.github.tno.gltsdiff.glts.State;
import com.github.tno.gltsdiff.glts.Transition;
import com.github.tno.gltsdiff.operators.combiners.Combiner;

/** Utilities for working with GLTSs. */
public class GLTSUtils {
    /** Constructor for the {@link GLTSUtils} class. */
    private GLTSUtils() {
        // Static class.
    }

    /**
     * Determines whether {@code statePair} has any common incoming transitions with combinable properties.
     *
     * @param <S> The type of state properties.
     * @param <T> The type of transition properties.
     * @param left The left GLTS.
     * @param right The right GLTS.
     * @param combiner The combiner for transition properties.
     * @param statePair The pair of ({@code left}, {@code right})-states to consider.
     * @return {@code true} if the given state pair has common combinable incoming transitions, {@code false} otherwise.
     */
    public static <S, T> boolean hasCommonIncomingTransitions(GLTS<S, T> left, GLTS<S, T> right, Combiner<T> combiner,
            Pair<State<S>, State<S>> statePair)
    {
        return commonIncomingTransitions(left, right, combiner, statePair).findAny().isPresent();
    }

    /**
     * Determines whether {@code statePair} has any common outgoing transitions with combinable properties.
     *
     * @param <S> The type of state properties.
     * @param <T> The type of transition properties.
     * @param left The left GLTS.
     * @param right The right GLTS.
     * @param combiner The combiner for transition properties.
     * @param statePair The pair of ({@code left}, {@code right})-states to consider.
     * @return {@code true} if the given state pair has common combinable outgoing transitions, {@code false} otherwise.
     */
    public static <S, T> boolean hasCommonOutgoingTransitions(GLTS<S, T> left, GLTS<S, T> right, Combiner<T> combiner,
            Pair<State<S>, State<S>> statePair)
    {
        return commonOutgoingTransitions(left, right, combiner, statePair).findAny().isPresent();
    }

    /**
     * Gives a stream of all pairs of common combinable incoming transitions that go into {@code statePair}.
     *
     * @param <S> The type of state properties.
     * @param <T> The type of transition properties.
     * @param left The left GLTS.
     * @param right The right GLTS.
     * @param combiner The combiner for transition properties.
     * @param statePair The pair of ({@code left}, {@code right})-states to consider.
     * @return The stream containing all pairs of combinable incoming transitions into {@code statePair}.
     */
    public static <S, T> Stream<Pair<Transition<S, T>, Transition<S, T>>> commonIncomingTransitions(GLTS<S, T> left,
            GLTS<S, T> right, Combiner<T> combiner, Pair<State<S>, State<S>> statePair)
    {
        List<Transition<S, T>> leftTransitions = left.getIncomingTransitions(statePair.getFirst());
        List<Transition<S, T>> rightTransitions = right.getIncomingTransitions(statePair.getSecond());

        return leftTransitions.stream().flatMap(l -> rightTransitions.stream()
                .filter(r -> combiner.areCombinable(l.getProperty(), r.getProperty())).map(r -> Pair.create(l, r)));
    }

    /**
     * Gives a stream of all pairs of common combinable outgoing transitions that go out of {@code statePair}.
     *
     * @param <S> The type of state properties.
     * @param <T> The type of transition properties.
     * @param left The left GLTS.
     * @param right The right GLTS.
     * @param combiner The combiner for transition properties.
     * @param statePair The pair of ({@code left}, {@code right})-states to consider.
     * @return The stream containing all pairs of combinable outgoing transitions out of {@code statePair}.
     */
    public static <S, T> Stream<Pair<Transition<S, T>, Transition<S, T>>> commonOutgoingTransitions(GLTS<S, T> left,
            GLTS<S, T> right, Combiner<T> combiner, Pair<State<S>, State<S>> statePair)
    {
        List<Transition<S, T>> leftTransitions = left.getOutgoingTransitions(statePair.getFirst());
        List<Transition<S, T>> rightTransitions = right.getOutgoingTransitions(statePair.getSecond());

        return leftTransitions.stream().flatMap(l -> rightTransitions.stream()
                .filter(r -> combiner.areCombinable(l.getProperty(), r.getProperty())).map(r -> Pair.create(l, r)));
    }

    /**
     * Determines the set of all pairs of ({@code left}, {@code right})-states that can reach {@code statePair} by a
     * common combinable incoming transition.
     *
     * @param <S> The type of state properties.
     * @param <T> The type of transition properties.
     * @param left The left GLTS.
     * @param right The right GLTS.
     * @param combiner The combiner for transition properties.
     * @param statePair The state pair for which all common predecessors are to be found.
     * @return The set of all common predecessors of {@code statePair}.
     */
    public static <S, T> Set<Pair<State<S>, State<S>>> commonPredecessors(GLTS<S, T> left, GLTS<S, T> right,
            Combiner<T> combiner, Pair<State<S>, State<S>> statePair)
    {
        return commonIncomingTransitions(left, right, combiner, statePair)
                .map(pair -> Pair.create(pair.getFirst().getSource(), pair.getSecond().getSource()))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * Determines the set of all pairs of ({@code left}, {@code right})-states that {@code statePair} can reach by a
     * common combinable outgoing transition.
     *
     * @param <S> The type of state properties.
     * @param <T> The type of transition properties.
     * @param left The left GLTS.
     * @param right The right GLTS.
     * @param combiner The combiner for transition properties.
     * @param statePair The state pair for which all common successors are to be found.
     * @return The set of all common successors of {@code statePair}.
     */
    public static <S, T> Set<Pair<State<S>, State<S>>> commonSuccessors(GLTS<S, T> left, GLTS<S, T> right,
            Combiner<T> combiner, Pair<State<S>, State<S>> statePair)
    {
        return commonOutgoingTransitions(left, right, combiner, statePair)
                .map(pair -> Pair.create(pair.getFirst().getTarget(), pair.getSecond().getTarget()))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
