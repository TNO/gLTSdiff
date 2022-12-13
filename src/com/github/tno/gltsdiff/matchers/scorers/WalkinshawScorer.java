//////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2021-2022 Contributors to the GitHub community
//
// This program and the accompanying materials are made available
// under the terms of the MIT License which is available at
// https://opensource.org/licenses/MIT
//
// SPDX-License-Identifier: MIT
//////////////////////////////////////////////////////////////////////////////

package com.github.tno.gltsdiff.matchers.scorers;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.util.Pair;

import com.github.tno.gltsdiff.lts.LTS;
import com.github.tno.gltsdiff.lts.State;
import com.github.tno.gltsdiff.operators.combiners.Combiner;
import com.google.common.base.Preconditions;

/**
 * Contains common functionality for the state similarity scoring approaches that are described in the article by
 * Walkinshaw et al. (TOSEM 2013). However, this implementation generalizes the approaches described in the article by a
 * more general concept of combinability (see {@link Combiner}).
 *
 * @param <S> The type of state properties.
 * @param <T> The type of transition properties.
 * @param <U> The type of LTSs.
 */
public abstract class WalkinshawScorer<S, T, U extends LTS<S, T>> implements SimilarityScorer<S, T, U> {
    /**
     * This is the ratio in the range [0,1] that determines how much the similarity scores of far-away states influence
     * the final similarity scores.
     * <p>
     * A ratio of 0 would mean that only local similarity scores are used. Note that, if one is only interested in local
     * similarity, {@link WalkinshawLocalScorer} should be used instead, which gives the same result but is much cheaper
     * in terms of computation. A ratio of 1 would mean that far-away state similarities contribute equally much as
     * local ones.
     * </p>
     * <p>
     * This factor can be tweaked a bit if the comparison results come out unsatisfactory.
     * </p>
     */
    protected final double attenuationFactor = 0.6d;

    /** The left-hand-side LTS, which has at least one state. */
    protected final U lhs;

    /** The right-hand-side LTS, which has at least one state. */
    protected final U rhs;

    /** The combiner for state properties. */
    protected final Combiner<S> statePropertyCombiner;

    /** The combiner for transition properties. */
    protected final Combiner<T> transitionPropertyCombiner;

    /**
     * Instantiates a new Walkinshaw similarity scorer.
     * 
     * @param lhs The left-hand-side LTS, which has at least one state.
     * @param rhs The right-hand-side LTS, which has at least one state.
     * @param statePropertyCombiner The combiner for state properties.
     * @param transitionPropertyCombiner The combiner for transition properties.
     */
    public WalkinshawScorer(U lhs, U rhs, Combiner<S> statePropertyCombiner, Combiner<T> transitionPropertyCombiner) {
        Preconditions.checkArgument(lhs.size() > 0, "Expected the LHS to have at least one state.");
        Preconditions.checkArgument(rhs.size() > 0, "Expected the RHS to have at least one state.");

        this.lhs = lhs;
        this.rhs = rhs;
        this.statePropertyCombiner = statePropertyCombiner;
        this.transitionPropertyCombiner = transitionPropertyCombiner;
    }

    @Override
    public U getLhs() {
        return lhs;
    }

    @Override
    public U getRhs() {
        return rhs;
    }

    @Override
    public RealMatrix compute() {
        // Calculate forward and backward similarity scores.
        RealMatrix forwardScores = computeForwardSimilarityScores();
        RealMatrix backwardScores = computeBackwardSimilarityScores();

        // Ensure that the scoring matrices have the right dimensions.
        Preconditions.checkArgument(forwardScores.getRowDimension() == lhs.size(),
                "Expected the number of rows in the forward score matrix to equal the number of LHS states.");
        Preconditions.checkArgument(forwardScores.getColumnDimension() == rhs.size(),
                "Expected the number of columns in the forward score matrix to equal the number of RHS states.");
        Preconditions.checkArgument(backwardScores.getRowDimension() == lhs.size(),
                "Expected the number of rows in the backward score matrix to equal the number of LHS states.");
        Preconditions.checkArgument(backwardScores.getColumnDimension() == rhs.size(),
                "Expected the number of columns in the backward score matrix to equal the number of RHS states.");

        // Calculate average similarity scores out of the forward and backward scores.
        // Note: here 'BlockRealMatrix' is used since 'OpenMapRealMatrix' does not deal well with infinite doubles.
        RealMatrix averageScores = new BlockRealMatrix(lhs.size(), rhs.size());

        for (int leftIndex = 0; leftIndex < lhs.size(); leftIndex++) {
            for (int rightIndex = 0; rightIndex < rhs.size(); rightIndex++) {
                double forwardScore = forwardScores.getEntry(leftIndex, rightIndex);
                double backwardScore = backwardScores.getEntry(leftIndex, rightIndex);

                // Walkinshaw et al. guarantee that the computed state similarity scores are within the range [-1,1].
                // (see the small paragraph right after Equation 6 on page 14 in their TOSEM 2013 article.)
                Preconditions.checkArgument(-1 <= forwardScore && forwardScore <= 1,
                        "Expected all forward state similarity scores to be within the range [-1,1].");
                Preconditions.checkArgument(-1 <= backwardScore && backwardScore <= 1,
                        "Expected all backward state similarity scores to be within the range [-1,1].");

                // Any negative score indicates an incompatible state pair. This is not explicit in the TOSEM 2013
                // article, but it is (to some extend) in the implementation of StateChum
                // (e.g., see statechum.analysis.learning.linear.GDLearnerGraph, line 498, on
                // https://github.com/kirilluk/statechum/blob/056163f301f27862d44f6eaa84ffbc30efb4bd48/src/statechum/analysis/learning/linear/GDLearnerGraph.java#L498).
                // Therefore any such score is marked as minus infinity.
                if (forwardScore < 0d || backwardScore < 0d) {
                    averageScores.setEntry(leftIndex, rightIndex, Double.NEGATIVE_INFINITY);
                } else {
                    averageScores.setEntry(leftIndex, rightIndex, (forwardScore + backwardScore) / 2d);
                }
            }
        }

        // At this point, all computed average scores are either negative infinity in case the corresponding state pair
        // turned out to be incompatible, or otherwise guaranteed to be within the range [0,1].
        return averageScores;
    }

    /**
     * @return The computed {@code lhs.size()} times {@code rhs.size()} matrix of forward state similarity scores, all
     *     of which are in the range [-1,1], where any negative score indicates an incompatible state pair.
     */
    protected abstract RealMatrix computeForwardSimilarityScores();

    /**
     * @return The computed {@code lhs.size()} times {@code rhs.size()} matrix of backward state similarity scores, all
     *     of which are in the range [-1,1], where any negative score indicates an incompatible state pair.
     */
    protected abstract RealMatrix computeBackwardSimilarityScores();

    /**
     * Collects all (LHS, RHS)-state pairs with a state similarity score that can statically (efficiently) be computed,
     * without having to solve expensive linear equation systems (i.e., {@link WalkinshawGlobalScorer}) or perform
     * refinement operations that may be expensive (i.e., {@link WalkinshawLocalScorer}).
     * 
     * @param staticallyKnownScores A mutable map from state pairs to statically known similarity scores, which will be
     *     updated by this method.
     * @param commonNeighbors A function from (LHS, RHS)-state pairs to their common neighboring state pairs.
     * @param relevantProperties A function that determines the relevant transition properties for an LTS and a state.
     * @param accountForInitialStateArrows Whether the scoring calculation should take initial state arrows into
     *     account. Note that the original paper does not take initial states into account.
     * @return A set of all state pairs with statically unknown scores. This method guarantees that this returned set,
     *     unioned with the key set of {@code staticallyKnownScores}, equals the set of all possible state pairs.
     */
    protected Set<Pair<State<S>, State<S>>> collectStaticallyKnownScores(
            Map<Pair<State<S>, State<S>>, Double> staticallyKnownScores,
            Function<Pair<State<S>, State<S>>, Collection<Pair<State<S>, State<S>>>> commonNeighbors,
            BiFunction<U, State<S>, Set<T>> relevantProperties, boolean accountForInitialStateArrows)
    {
        // Compute the set of all state pairs with unknown similarity scores.
        Set<Pair<State<S>, State<S>>> statePairsWithUnknownScores = new LinkedHashSet<>();

        // Also compute a mapping that is the inverse of 'commonNeighbors', with respect to all (LHS, RHS)-state pairs.
        // This mapping essentially maps state pairs to the set of state pairs whose similarity score depend on the
        // mapped (key) state pair.
        Map<Pair<State<S>, State<S>>, Set<Pair<State<S>, State<S>>>> commonNeighborsInverse = new LinkedHashMap<>();

        // Populate both 'statePairsWithUnknownScores' and 'commonNeighborsInverse'.
        for (State<S> leftState: lhs.getStates()) {
            for (State<S> rightState: rhs.getStates()) {
                Pair<State<S>, State<S>> statePair = Pair.create(leftState, rightState);

                // If the similarity score of 'statePair' is not yet statically known, then mark it as such.
                if (!staticallyKnownScores.containsKey(statePair)) {
                    statePairsWithUnknownScores.add(statePair);
                }

                // Record that the similarity scores of all common neighbors of 'statePair' depend on the similarity
                // score of 'statePair'.
                for (Pair<State<S>, State<S>> commonNeighbor: commonNeighbors.apply(statePair)) {
                    commonNeighborsInverse.computeIfAbsent(commonNeighbor, p -> new LinkedHashSet<>()).add(statePair);
                }

                // Make sure that 'commonNeighborsInverse' holds a mapping for every (LHS, RHS)-state pair.
                commonNeighborsInverse.computeIfAbsent(statePair, p -> new LinkedHashSet<>());
            }
        }

        // Next we try to determine the similarity scores of all state pairs whose scores are not (yet) known. Whenever
        // a new score has statically been found, all state pairs whose score depend on this newly found score are
        // considered again, in case they were already considered. Therefore this exploration is performed as a fixpoint
        // operation, which is guaranteed to terminate eventually since only a finite number of similarity scores can
        // ever statically be found.
        Set<Pair<State<S>, State<S>>> pairsToExplore = new LinkedHashSet<>(statePairsWithUnknownScores);

        // This loop maintains the invariant that 'pairsToExplore' only contains state pairs with unknown scores.
        while (!pairsToExplore.isEmpty()) {
            Pair<State<S>, State<S>> statePair = pairsToExplore.iterator().next();
            pairsToExplore.remove(statePair);

            // Try to statically determine the similarity score of 'statePair'.
            Optional<Double> possibleScore = tryToStaticallyDetermineSimilarityScore(statePair, staticallyKnownScores,
                    commonNeighbors, relevantProperties, accountForInitialStateArrows);

            if (possibleScore.isPresent()) {
                // Register that 'statePair' is now a state pair with a statically known score.
                staticallyKnownScores.put(statePair, possibleScore.get());
                statePairsWithUnknownScores.remove(statePair);
                pairsToExplore.remove(statePair);

                // Make sure that all state pairs with unknown similarity scores, whose score depends on the score of
                // 'statePair', are considered again in this exploration.
                for (Pair<State<S>, State<S>> dependentStatePair: commonNeighborsInverse.get(statePair)) {
                    if (!staticallyKnownScores.containsKey(dependentStatePair)) {
                        pairsToExplore.add(dependentStatePair);
                    }
                }
            }
        }

        return statePairsWithUnknownScores;
    }

    /**
     * Attempts to statically determine the similarity score of {@code statePair}. In particular:
     * <ul>
     * <li>Any state pair with uncombinable state properties has a statically known score of {@code -1d}.</li>
     * <li>Any state pair has a statically known score, if all its common neighbors have statically known scores.</li>
     * </ul>
     * 
     * @param statePair The state pair for which to attempt to statically determine the similarity score.
     * @param staticallyKnownScores A map from state pairs to statically known similarity scores, which will be updated
     *     by this method.
     * @param commonNeighbors A function from (LHS, RHS)-state pairs to their common neighboring state pairs.
     * @param relevantProperties A function that determines the relevant transition properties for an LTS and a state.
     * @param accountForInitialStateArrows Whether the scoring calculation should take initial state arrows into
     *     account. Note that the original paper does not take initial states into account.
     * @return A similarity score in the range [-1,1] in case it was statically determined, or {@link Optional#empty}
     *     otherwise.
     */
    private Optional<Double> tryToStaticallyDetermineSimilarityScore(Pair<State<S>, State<S>> statePair,
            Map<Pair<State<S>, State<S>>, Double> staticallyKnownScores,
            Function<Pair<State<S>, State<S>>, Collection<Pair<State<S>, State<S>>>> commonNeighbors,
            BiFunction<U, State<S>, Set<T>> relevantProperties, boolean accountForInitialStateArrows)
    {
        State<S> leftState = statePair.getFirst();
        State<S> rightState = statePair.getSecond();

        // If 'leftState' and 'rightState' have uncombinable state properties, then they have a similarity score of -1.
        if (!statePropertyCombiner.areCombinable(leftState.getProperty(), rightState.getProperty())) {
            return Optional.of(-1d);
        }

        // Otherwise, get all relevant neighbors of 'statePair' to attempt to statically compute the similarity score.
        Collection<Pair<State<S>, State<S>>> neighborStatePairs = commonNeighbors.apply(statePair);

        // We can statically determine the similarity score of 'statePair' if the similarity scores of all its common
        // neighbors are statically known.
        if (neighborStatePairs.stream().allMatch(staticallyKnownScores::containsKey)) {
            // We calculate the similarity score for 'statePair' as proposed by Walkinshaw et al.

            // The similarity score is a fraction. First we determine its numerator. Details are in the paper.
            double numerator = neighborStatePairs.stream()
                    .mapToDouble(pair -> 1d + attenuationFactor * staticallyKnownScores.get(pair)).sum();

            // If initial state arrows should be accounted for and if 'leftState' and 'rightState' are both
            // initial, then increase the numerator by 1 to increase the similarity score for this state pair.
            boolean isLeftStateInitial = lhs.isInitialState(leftState);
            boolean isRightStateInitial = rhs.isInitialState(rightState);

            if (accountForInitialStateArrows && isLeftStateInitial && isRightStateInitial) {
                numerator += 1d;
            }

            // Then we determine the denominator of the similarity score.
            Set<T> leftProperties = relevantProperties.apply(lhs, leftState);
            Set<T> rightProperties = relevantProperties.apply(rhs, rightState);

            // If 'leftState' and/or 'rightState' is initial and if initial state arrows should be accounted
            // for, then adjust the denominator by 'initialStateAdjustment' accordingly.
            int initialStateAdjustment = 0;

            if (accountForInitialStateArrows && (isLeftStateInitial || isRightStateInitial)) {
                initialStateAdjustment = 1;
            }

            double denominator = 2d * (uncombinableTransitionProperties(leftProperties, rightProperties).size()
                    + uncombinableTransitionProperties(rightProperties, leftProperties).size()
                    + neighborStatePairs.size() + initialStateAdjustment);

            // Determine and return the similarity score of 'statePair'.
            return denominator == 0d ? Optional.of(0d) : Optional.of(numerator / denominator);
        }

        return Optional.empty();
    }

    /**
     * Computes the set of all transition properties of {@code left} that cannot be combined with any property from
     * {@code right}.
     * 
     * @param left The set of transition properties to filter.
     * @param right The set of transition properties from which the filtering criteria is determined.
     * @return All properties of {@code left} minus the ones that can be combined with a property from {@code right}.
     */
    protected Set<T> uncombinableTransitionProperties(Set<T> left, Set<T> right) {
        return left.stream().filter(l -> right.stream().noneMatch(r -> transitionPropertyCombiner.areCombinable(l, r)))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
