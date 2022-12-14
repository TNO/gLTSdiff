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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.OpenMapRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.util.Pair;

import com.github.tno.gltsdiff.lts.LTS;
import com.github.tno.gltsdiff.lts.State;
import com.github.tno.gltsdiff.operators.combiners.Combiner;
import com.github.tno.gltsdiff.utils.LTSUtils;
import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

/**
 * Contains functionality for computing global similarity scores for pairs of (LHS, RHS)-states. These scores are
 * computed by transforming the problem of finding global similarity scores to a problem of solving a system of linear
 * equations, as proposed by Walkinshaw et al. in their TOSEM 2013 article. However, this implementation generalizes the
 * approach of Walkinshaw et al. by a more general concept of combinability (see {@link Combiner}).
 * <p>
 * Note that, since computing global similarity scores requires solving systems of linear equations, the complexity of
 * this computation is about O((|LHS|*|RHS|)^3), with |LHS| and |RHS| the number of states in the LHS and RHS,
 * respectively. So when performance problems are encountered, consider switching to a more lightweight scoring system
 * instead, like for example {@link WalkinshawLocalScorer}.
 * </p>
 * <p>
 * However, {@link WalkinshawGlobalScorer} has shown to perform well in practice even with big LTSs (say, a few dozens
 * of states each) as long as they are sparse. That is, as long as states typically only have a few neighbors.
 * </p>
 *
 * @param <S> The type of state properties.
 * @param <T> The type of transition properties.
 * @param <U> The type of LTSs.
 */
public class WalkinshawGlobalScorer<S, T, U extends LTS<S, T>> extends WalkinshawScorer<S, T, U> {
    /**
     * Instantiates a new Walkinshaw global scorer.
     * 
     * @param lhs The left-hand-side LTS, which has at least one state.
     * @param rhs The right-hand-side LTS, which has at least one state.
     * @param statePropertyCombiner The combiner for state properties.
     * @param transitionPropertyCombiner The combiner for transition properties.
     */
    public WalkinshawGlobalScorer(U lhs, U rhs, Combiner<S> statePropertyCombiner,
            Combiner<T> transitionPropertyCombiner)
    {
        super(lhs, rhs, statePropertyCombiner, transitionPropertyCombiner);
    }

    @Override
    protected RealMatrix computeForwardSimilarityScores() {
        return computeScores(pair -> LTSUtils.commonSuccessors(lhs, rhs, transitionPropertyCombiner, pair),
                (lts, state) -> lts.getOutgoingTransitionProperties(state), false);
    }

    @Override
    protected RealMatrix computeBackwardSimilarityScores() {
        return computeScores(pair -> LTSUtils.commonPredecessors(lhs, rhs, transitionPropertyCombiner, pair),
                (lts, state) -> lts.getIncomingTransitionProperties(state), true);
    }

    /**
     * Computes a (global) similarity score matrix for all pairs of (LHS, RHS)-states, with scores in the range [-1,1].
     * <p>
     * This computation relies on a function {@code commonNeighbors} that gives a list of all relevant common
     * neighboring state pairs that are possible from the input pair of states. Furthermore, {@code relevantProperties}
     * gives the set of transition properties that are relevant to consider from a given state in a specified LTS.
     * </p>
     * <p>
     * The similarity scores are computed by constructing and solving a system of linear equations. Details on this can
     * be found in the TOSEM'13 article by Walkinshaw et al. However, this implementation generalizes the approach
     * described in that article by a more general concept of combinability (see {@link Combiner}).
     * </p>
     * 
     * @param commonNeighbors A function from (LHS, RHS)-state pairs to their common neighboring state pairs. This
     *     function should be unidirectional, i.e., should give all common predecessors or successors of the input pair.
     * @param relevantProperties A function that determines the relevant transition properties for an LTS and a state.
     * @param accountForInitialStateArrows Whether the scoring calculation should take initial state arrows into
     *     account. Note that the original paper does not take initial states into account.
     * @return A matrix of state similarity scores, all of which are in the range [-1,1].
     */
    private RealMatrix computeScores(
            Function<Pair<State<S>, State<S>>, Collection<Pair<State<S>, State<S>>>> commonNeighbors,
            BiFunction<U, State<S>, Set<T>> relevantProperties, boolean accountForInitialStateArrows)
    {
        // Collect all statically known similarity scores, as well as all state pairs with statically unknown scores.
        Map<Pair<State<S>, State<S>>, Double> staticallyKnownScores = new LinkedHashMap<>();
        Set<Pair<State<S>, State<S>>> statePairsWithUnknownScores = collectStaticallyKnownScores(staticallyKnownScores,
                commonNeighbors, relevantProperties, accountForInitialStateArrows);

        // Here it holds that the set of 'statePairsWithUnknownScores' unioned with the key set of
        // 'staticallyKnownScores' equals the set of all possible (LHS, RHS)-state pairs. Moreover, both these sets are
        // disjoint.

        // If we already know the similarity scores of all state pairs, then we can terminate early.
        if (statePairsWithUnknownScores.isEmpty()) {
            RealMatrix scores = new OpenMapRealMatrix(lhs.size(), rhs.size());

            for (Entry<Pair<State<S>, State<S>>, Double> entry: staticallyKnownScores.entrySet()) {
                int row = entry.getKey().getFirst().getId();
                int column = entry.getKey().getSecond().getId();
                double score = entry.getValue();
                scores.setEntry(row, column, score);
            }

            return scores;
        }

        // Otherwise we need to construct and solve a linear equation system, to find all missing similarity scores.
        // We construct this linear system as proposed by Walkinshaw et al. in their TOSEM 2013 article.

        // We start by indexing 'statePairsWithUnknownScores' to get fast 'indexOf' functionality, which would otherwise
        // take at least O(log n) time. This indexing is needed for encoding/locating state pairs in the linear system.
        BiMap<Pair<State<S>, State<S>>, Integer> statePairsToEncode = indexate(statePairsWithUnknownScores);

        // Set up the matrix and vector that together shall encode the system of linear equations.
        RealMatrix coefficients = new OpenMapRealMatrix(statePairsToEncode.size(), statePairsToEncode.size());
        RealVector constants = new ArrayRealVector(statePairsToEncode.size());

        // We iterate over all state pairs with unknown scores to encode their corresponding rows in the linear system.
        for (Entry<Pair<State<S>, State<S>>, Integer> entry: statePairsToEncode.entrySet()) {
            Pair<State<S>, State<S>> statePair = entry.getKey();
            State<S> leftState = statePair.getFirst();
            State<S> rightState = statePair.getSecond();
            int statePairIndex = entry.getValue();

            // If 'leftState' and 'rightState' are uncombinable, then encode that their similarity score is -1.
            if (!statePropertyCombiner.areCombinable(leftState.getProperty(), rightState.getProperty())) {
                coefficients.setEntry(statePairIndex, statePairIndex, 1d);
                constants.setEntry(statePairIndex, -1d);
                continue;
            }

            // Otherwise we first determine the coefficients for all neighboring state pairs with unknown scores.
            Collection<Pair<State<S>, State<S>>> neighborStatePairs = commonNeighbors.apply(statePair);

            for (Pair<State<S>, State<S>> neighborStatePair: neighborStatePairs) {
                Integer neighborStatePairIndex = statePairsToEncode.get(neighborStatePair);

                if (neighborStatePairIndex != null) {
                    coefficients.addToEntry(statePairIndex, neighborStatePairIndex, -attenuationFactor);
                }
            }

            // Next we calculate the diagonal of the matrix. Details are in the paper.

            Set<T> leftProperties = relevantProperties.apply(lhs, leftState);
            Set<T> rightProperties = relevantProperties.apply(rhs, rightState);

            // If 'leftState' and/or 'rightState' is initial and if initial state arrows should be accounted for,
            // then adjust the diagonal by 'initialStateAdjustment' to indicate that there are initial states.
            boolean isLeftStateInitial = lhs.getInitialStates().contains(leftState);
            boolean isRightStateInitial = rhs.getInitialStates().contains(rightState);
            int initialStateAdjustment = 0;

            if (accountForInitialStateArrows && (isLeftStateInitial || isRightStateInitial)) {
                initialStateAdjustment = 1;
            }

            double diagonal = coefficients.getEntry(statePairIndex, statePairIndex)
                    + 2 * (uncombinableTransitionProperties(leftProperties, rightProperties).size()
                            + uncombinableTransitionProperties(rightProperties, leftProperties).size()
                            + neighborStatePairs.size() + initialStateAdjustment);

            if (diagonal == 0.0d && neighborStatePairs.size() == 0) {
                diagonal = 1.0d;
            }

            coefficients.setEntry(statePairIndex, statePairIndex, diagonal);

            // Lastly determine the constant term in the linear system for the current state pair.
            double constant = neighborStatePairs.size() + neighborStatePairs.stream().map(staticallyKnownScores::get)
                    .filter(score -> score != null).mapToDouble(score -> attenuationFactor * score).sum();

            // If initial state arrows should be accounted for and if 'leftState' and 'rightState' are both initial,
            // then increase the constant of 'index' by 1 to increase the similarity score.
            if (accountForInitialStateArrows && isLeftStateInitial && isRightStateInitial) {
                constant += 1d;
            }

            constants.setEntry(statePairIndex, constant);
        }

        // Next step is solving the system of linear equations.
        RealVector solution = createSolver(coefficients).solve(constants);

        // Finally, convert 'solution' to a scores matrix.
        RealMatrix scores = new OpenMapRealMatrix(lhs.size(), rhs.size());

        for (Entry<Pair<State<S>, State<S>>, Double> entry: staticallyKnownScores.entrySet()) {
            State<S> leftState = entry.getKey().getFirst();
            State<S> rightState = entry.getKey().getSecond();
            double score = entry.getValue();
            scores.setEntry(leftState.getId(), rightState.getId(), score);
        }

        for (Entry<Pair<State<S>, State<S>>, Integer> entry: statePairsToEncode.entrySet()) {
            State<S> leftState = entry.getKey().getFirst();
            State<S> rightState = entry.getKey().getSecond();
            int statePairIndex = entry.getValue();
            double score = solution.getEntry(statePairIndex);
            scores.setEntry(leftState.getId(), rightState.getId(), score);
        }

        return scores;
    }

    /**
     * Collects all (LHS, RHS)-state pairs with a state similarity score that can statically (efficiently) be computed,
     * without having to solve expensive linear equation systems (i.e., {@link WalkinshawGlobalScorer}) or perform
     * refinement operations that may be expensive (i.e., {@link WalkinshawLocalScorer}).
     * 
     * @param staticallyKnownScores A mutable map from state pairs to statically known similarity scores, which will be
     *     updated by this method.
     * @param commonNeighbors A function from (LHS, RHS)-state pairs to their common neighboring state pairs. This
     *     function should be unidirectional, i.e., should give all common predecessors or successors of the input pair.
     * @param relevantProperties A function that determines the relevant transition properties for an LTS and a state.
     * @param accountForInitialStateArrows Whether the scoring calculation should take initial state arrows into
     *     account. Note that the original paper does not take initial states into account.
     * @return A set of all state pairs with statically unknown scores. This method guarantees that this returned set,
     *     unioned with the key set of {@code staticallyKnownScores}, equals the set of all possible state pairs.
     */
    private Set<Pair<State<S>, State<S>>> collectStaticallyKnownScores(
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
     * @param commonNeighbors A function from (LHS, RHS)-state pairs to their common neighboring state pairs. This
     *     function should be unidirectional, i.e., should give all common predecessors or successors of the input pair.
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
     * Gives a decomposition solver for the given matrix. (An overview of other available solvers can be found at:
     * https://commons.apache.org/proper/commons-math/userguide/linear.html.)
     * 
     * @param matrix The matrix that encodes the system of linear equations.
     * @return A solver for solving the system of linear equations.
     */
    protected DecompositionSolver createSolver(RealMatrix matrix) {
        return new LUDecomposition(matrix).getSolver();
    }

    /**
     * Indexates a given collection, by associating a unique index between {@code 0} and {@code collection.size() - 1}
     * to every element.
     * <p>
     * Note that, as an alternative to using this method, one could also simply convert {@code collection} to a list and
     * use list indices instead. However, {@link List#indexOf} is (typically) not a constant operation, whereas the
     * returned map can give indices of elements effectively in constant time. And since the number of possible state
     * pairs to consider may be huge, this improvement may well pay off.
     * </p>
     * 
     * @param <E> The type of elements in the collection.
     * @param collection The collection to indexate.
     * @return A bidirectional map from the elements of {@code collection} to their unique indices.
     */
    private static <E> BiMap<E, Integer> indexate(Collection<E> collection) {
        Preconditions.checkNotNull(collection, "Expected a non-null collection.");
        BiMap<E, Integer> map = HashBiMap.create(collection.size());
        collection.forEach(element -> map.put(element, map.size()));
        return map;
    }
}
