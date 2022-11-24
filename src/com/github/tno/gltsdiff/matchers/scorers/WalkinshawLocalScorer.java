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

import org.apache.commons.math3.linear.OpenMapRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.util.Pair;

import com.github.tno.gltsdiff.lts.LTS;
import com.github.tno.gltsdiff.lts.State;
import com.github.tno.gltsdiff.operators.combiners.Combiner;
import com.github.tno.gltsdiff.utils.LTSUtils;
import com.google.common.base.Preconditions;

/**
 * Contains functionality for computing local similarity scores for pairs of (LHS, RHS)-states. These scores are local
 * in the sense that they are determined by the amount of overlap in incoming and outgoing transitions.
 * <p>
 * The complexity of computing local similarity scores is about O(|LHS|*|RHS|), with |LHS| and |RHS| the number of
 * states plus transitions in the LHS and RHS, respectively.
 * </p>
 * <p>
 * By performing more than one refinement, this method does allow to take further away neighbors into account, for
 * better quality scoring, at higher computation costs.
 * </p>
 *
 * @param <S> The type of state properties.
 * @param <T> The type of transition properties.
 * @param <U> The type of LTSs.
 */
public class WalkinshawLocalScorer<S, T, U extends LTS<S, T>> extends WalkinshawScorer<S, T, U> {
    /** The number of refinements that the scoring algorithm should perform. This number must be at least 1. */
    private final int nrOfRefinements;

    /**
     * Instantiates a new Walkinshaw local similarity scorer that performs only a single refinement.
     * 
     * @param lhs The left-hand-side LTS, which has at least one state.
     * @param rhs The right-hand-side LTS, which has at least one state.
     * @param statePropertyCombiner The combiner for state properties.
     * @param transitionPropertyCombiner The combiner for transition properties.
     */
    public WalkinshawLocalScorer(U lhs, U rhs, Combiner<S> statePropertyCombiner,
            Combiner<T> transitionPropertyCombiner)
    {
        this(lhs, rhs, statePropertyCombiner, transitionPropertyCombiner, 1);
    }

    /**
     * Instantiates a new Walkinshaw local similarity scorer.
     * 
     * @param lhs The left-hand-side LTS, which has at least one state.
     * @param rhs The right-hand-side LTS, which has at least one state.
     * @param statePropertyCombiner The combiner for state properties.
     * @param transitionPropertyCombiner The combiner for transition properties.
     * @param nrOfRefinements The number of refinements to perform, which must be at least 1.
     */
    public WalkinshawLocalScorer(U lhs, U rhs, Combiner<S> statePropertyCombiner,
            Combiner<T> transitionPropertyCombiner, int nrOfRefinements)
    {
        super(lhs, rhs, statePropertyCombiner, transitionPropertyCombiner);
        this.nrOfRefinements = nrOfRefinements;

        Preconditions.checkArgument(nrOfRefinements > 0, "Expected a positive number of refinements.");
    }

    @Override
    protected RealMatrix computeForwardSimilarityScores() {
        return computeScores(pair -> LTSUtils.commonSuccessors(lhs, rhs, transitionPropertyCombiner, pair),
                lts -> state -> lts.getOutgoingTransitionProperties(state), false);
    }

    @Override
    protected RealMatrix computeBackwardSimilarityScores() {
        return computeScores(pair -> LTSUtils.commonPredecessors(lhs, rhs, transitionPropertyCombiner, pair),
                lts -> state -> lts.getIncomingTransitionProperties(state), true);
    }

    /**
     * Computes local similarity scores for every pair of (LHS, RHS)-state pairs.
     * 
     * @param commonNeighbors A function that determines the common neighboring state pairs for a given pair of (LHS,
     *     RHS)-states, that are relevant for computing local similarity scores.
     * @param relevantProperties A function that determines the transition properties for a given LTS and a state of
     *     that LTS, that are relevant for computing local similarity scores.
     * @param accountForInitialStateArrows Whether the scoring calculation should take initial state arrows into
     *     account. Note that the original paper does not take initial states into account.
     * @return A matrix of local state similarity scores, all of which are in the range [-1,1].
     */
    private RealMatrix computeScores(
            Function<Pair<State<S>, State<S>>, Collection<Pair<State<S>, State<S>>>> commonNeighbors,
            Function<U, Function<State<S>, Set<T>>> relevantProperties, boolean accountForInitialStateArrows)
    {
        // Define an initial similarity score matrix with score 0 for every (LHS, RHS)-state pair.
        RealMatrix scores = new OpenMapRealMatrix(lhs.size(), rhs.size());

        // Refine the scores a number of times. At least one refinement will always be performed.
        for (int i = 0; i < nrOfRefinements; i++) {
            // Iterate over all pairs of (LHS, RHS)-states.
            for (State<S> leftState: lhs.getStates()) {
                for (State<S> rightState: rhs.getStates()) {
                    // Refine the similarity score of the current state pair.
                    double refinedScore = similarityScore(leftState, rightState, scores, commonNeighbors,
                            relevantProperties, accountForInitialStateArrows);

                    // Store the refine similarity score in 'scores'.
                    scores.setEntry(leftState.getId(), rightState.getId(), refinedScore);
                }
            }
        }

        return scores;
    }

    /**
     * Given a pair ({@code leftState}, {@code rightState}) of (LHS, RHS)-states, calculate a (local) similarity score
     * between them by only considering the transitions that are relevant. Moreover, the similarity score calculation is
     * based on refinement, in the sense that it refines (improves) a given matrix of current scores.
     * <p>
     * Details are in the paper. In particular Section 4.1 (Equations 2 and 4) and Section 4.2 (Equation 5). However,
     * this implementation has been generalized by a notion of combinability (see {@link Combiner}) to determine the
     * amount of overlap between transition properties.
     * </p>
     * 
     * @param leftState A LHS state.
     * @param rightState A RHS state.
     * @param scores The current matrix of similarity scores that is to be refined.
     * @param commonNeighbors A function giving the relevant neighboring state pairs for a pair of (LHS, RHS)-states.
     * @param relevantProperties A function that determines the relevant transition properties based on an LTS and a
     *     state.
     * @param accountForInitialStateArrows Whether the scoring calculation should take initial state arrows into
     *     account. Note that the original paper does not take initial states into account.
     * @return A (refined) similarity score for the given pair of (LHS, RHS)-states, in the range [-1,1].
     */
    private double similarityScore(State<S> leftState, State<S> rightState, RealMatrix scores,
            Function<Pair<State<S>, State<S>>, Collection<Pair<State<S>, State<S>>>> commonNeighbors,
            Function<U, Function<State<S>, Set<T>>> relevantProperties, boolean accountForInitialStateArrows)
    {
        // If 'leftState' and 'rightState' are uncombinable, then their similarity score is -1.
        if (!statePropertyCombiner.areCombinable(leftState.getProperty(), rightState.getProperty())) {
            return -1d;
        }

        // Get all relevant neighbors of 'leftState' and 'rightState' for computing the similarity score.
        // The similarity score is a fraction.
        Collection<Pair<State<S>, State<S>>> neighbors = commonNeighbors.apply(Pair.create(leftState, rightState));

        // Shortcut to improve performance (having no neighbors means that the numerator will always be zero).
        if (neighbors.isEmpty()) {
            return 0d;
        }

        // First calculate its denominator. Details are in the paper.
        Set<T> propertiesLeft = relevantProperties.apply(lhs).apply(leftState);
        Set<T> propertiesRight = relevantProperties.apply(rhs).apply(rightState);

        // If 'leftState' and/or 'rightState' is initial and if initial state arrows should be accounted for,
        // then adjust the denominator by 'initialStateAdjustment' to indicate that there are initial states.
        boolean isLeftStateInitial = lhs.isInitialState(leftState);
        boolean isRightStateInitial = rhs.isInitialState(rightState);
        int initialStateAdjustment = 0;

        if (accountForInitialStateArrows && (isLeftStateInitial || isRightStateInitial)) {
            initialStateAdjustment = 1;
        }

        // Note that, with respect to the original paper we change the notion of transition properties
        // "that do not match each other" to be transition properties "that do not combine with each other".
        int denominator = 2 * (uncombinableTransitionProperties(propertiesLeft, propertiesRight).size()
                + uncombinableTransitionProperties(propertiesRight, propertiesLeft).size() + neighbors.size()
                + initialStateAdjustment);

        // Shortcut to improve performance.
        if (denominator == 0) {
            return 0d;
        }

        // Second, calculate its numerator. Details are in the paper.
        double numerator = 0;

        for (Pair<State<S>, State<S>> neighbor: neighbors) {
            int lhsIdx = neighbor.getFirst().getId();
            int rhsIdx = neighbor.getSecond().getId();
            numerator += 1 + attenuationFactor * scores.getEntry(lhsIdx, rhsIdx);
        }

        // If initial state arrows should be accounted for and if 'leftState' and 'rightState' are both initial,
        // then increase the numerator by 1 to increase the similarity score for this state pair.
        if (accountForInitialStateArrows && isLeftStateInitial && isRightStateInitial) {
            numerator += 1.0d;
        }

        // Calculate the new score.
        return numerator / denominator;
    }
}
