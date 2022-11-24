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
import java.util.Set;
import java.util.function.Function;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
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

/**
 * Contains functionality for computing global similarity scores for pairs of (LHS, RHS)-states. These scores are
 * computed by transforming the problem of finding global similarity scores to a problem of solving a system of linear
 * equations, as proposed by Walkinshaw et al. in their TOSEM 2014 article. However, this implementation generalizes the
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
                p -> p.getFirst().getOutgoingTransitionProperties(p.getSecond()), false);
    }

    @Override
    protected RealMatrix computeBackwardSimilarityScores() {
        return computeScores(pair -> LTSUtils.commonPredecessors(lhs, rhs, transitionPropertyCombiner, pair),
                p -> p.getFirst().getIncomingTransitionProperties(p.getSecond()), true);
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
     * be found in the TOSEM'14 article by Walkinshaw et al. However, this implementation generalizes the approach
     * described in that article by a more general concept of combinability (see {@link Combiner}).
     * </p>
     * 
     * @param commonNeighbors A function from (LHS, RHS)-state pairs to their common neighboring state pairs.
     * @param relevantProperties A function that determines the relevant transition properties based on an LTS and a
     *     state.
     * @param accountForInitialStateArrows Whether the scoring calculation should take initial state arrows into
     *     account. Note that the original paper does not take initial states into account.
     * @return A matrix of state similarity scores, all of which are in the range [-1,1].
     */
    private RealMatrix computeScores(
            Function<Pair<State<S>, State<S>>, Collection<Pair<State<S>, State<S>>>> commonNeighbors,
            Function<Pair<U, State<S>>, Set<T>> relevantProperties, boolean accountForInitialStateArrows)
    {
        // How many state pairs do we have to account for?
        int lhsStateCount = lhs.size();
        int rhsStateCount = rhs.size();
        int nrOfStatePairs = lhsStateCount * rhsStateCount;

        // Set up the matrix and vector that together shall encode the system of linear equations.
        RealMatrix coefficients = new OpenMapRealMatrix(nrOfStatePairs, nrOfStatePairs);
        RealVector constants = new ArrayRealVector(nrOfStatePairs);

        // Now we encode the system of linear equations as described by Walkinshaw et al. into 'coefficients' and
        // 'constants'. The encoding is a bit technical. Details are explained in the paper.

        // Iterate over all (LHS, RHS)-state pairs.
        for (State<S> leftState: lhs.getStates()) {
            for (State<S> rightState: rhs.getStates()) {
                // Determine the row/column index within 'coefficients' and 'constants' corresponding to the current
                // state pair.
                int index = getEntryIndex(leftState.getId(), rightState.getId());

                // If 'leftState' and 'rightState' are uncombinable, then encode that their similarity score is -1.
                if (!statePropertyCombiner.areCombinable(leftState.getProperty(), rightState.getProperty())) {
                    coefficients.setEntry(index, index, 1d);
                    constants.setEntry(index, -1d);
                    continue;
                }

                // Otherwise, we first account for all common neighbors relevant for ('leftState', 'rightState').

                // Keep track of the number of iterations of the for-loop below. We need this later.
                int nrOfIteratedTransitions = 0;

                // Iterate over all relevant common neighbors for the current state pair.
                for (Pair<State<S>, State<S>> neighbor: commonNeighbors.apply(Pair.create(leftState, rightState))) {
                    // Determine the row/column within 'coefficients' and 'constants' of the neighbor we found.
                    int commonIndex = getEntryIndex(neighbor.getFirst().getId(), neighbor.getSecond().getId());

                    // Update the matrix to account for the common transition.
                    coefficients.addToEntry(index, commonIndex, -attenuationFactor);
                    nrOfIteratedTransitions++;
                }

                // Next we calculate the diagonal of the matrix. Details are in the paper.

                Set<T> propertiesLeft = relevantProperties.apply(Pair.create(lhs, leftState));
                Set<T> propertiesRight = relevantProperties.apply(Pair.create(rhs, rightState));

                // If 'leftState' and/or 'rightState' is initial and if initial state arrows should be accounted for,
                // then adjust the diagonal by 'initialStateAdjustment' to indicate that there are initial states.
                boolean isLeftStateInitial = lhs.getInitialStates().contains(leftState);
                boolean isRightStateInitial = rhs.getInitialStates().contains(rightState);
                int initialStateAdjustment = 0;

                if (accountForInitialStateArrows && (isLeftStateInitial || isRightStateInitial)) {
                    initialStateAdjustment = 1;
                }

                // Note that, with respect to the original paper we change the notion of transition properties
                // "that do not match each other" to be properties "that do not combine with each other".
                double diagonal = coefficients.getEntry(index, index)
                        + 2 * (uncombinableTransitionProperties(propertiesLeft, propertiesRight).size()
                                + uncombinableTransitionProperties(propertiesRight, propertiesLeft).size()
                                + nrOfIteratedTransitions + initialStateAdjustment);

                if (diagonal == 0.0d && nrOfIteratedTransitions == 0) {
                    diagonal = 1.0d;
                }

                coefficients.setEntry(index, index, diagonal);
                constants.setEntry(index, nrOfIteratedTransitions);

                // If initial state arrows should be accounted for and if 'leftState' and 'rightState' are both initial,
                // then increase the constant of 'index' by 1 to increase the similarity score.
                if (accountForInitialStateArrows && isLeftStateInitial && isRightStateInitial) {
                    constants.addToEntry(index, 1.0d);
                }
            }
        }

        // Next step is solving the system of linear equations.
        RealVector solution = createSolver(coefficients).solve(constants);

        // Finally, convert 'solution' to a scores matrix.
        RealMatrix scores = new Array2DRowRealMatrix(lhsStateCount, rhsStateCount);

        for (State<S> leftState: lhs.getStates()) {
            for (State<S> rightState: rhs.getStates()) {
                int lhsIdx = leftState.getId();
                int rhsIdx = rightState.getId();
                int index = getEntryIndex(lhsIdx, rhsIdx);
                double score = solution.getEntry(index);
                scores.setEntry(lhsIdx, rhsIdx, score);
            }
        }

        return scores;
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
     * Helper function for computing the row/column index of the (LHS, RHS)-state pair ({@code leftStateIndex},
     * {@code rightStateIndex}) in the matrix and vector that together encode the system of linear equations.
     * <p>
     * Note that, since the matrix is squared, the returned index is both a valid row and column index of the matrix. It
     * is also a valid index for the vector, since it has the same dimension as the matrix.
     * </p>
     */
    private int getEntryIndex(int leftStateIndex, int rightStateIndex) {
        return leftStateIndex * rhs.size() + rightStateIndex;
    }
}
