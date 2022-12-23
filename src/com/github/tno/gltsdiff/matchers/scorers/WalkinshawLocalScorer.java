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
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.apache.commons.math3.linear.OpenMapRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.util.Pair;

import com.github.tno.gltsdiff.glts.GLTS;
import com.github.tno.gltsdiff.glts.State;
import com.github.tno.gltsdiff.glts.Transition;
import com.github.tno.gltsdiff.operators.combiners.Combiner;
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
 * @param <U> The type of GLTSs.
 */
public class WalkinshawLocalScorer<S, T, U extends GLTS<S, T>> extends WalkinshawScorer<S, T, U> {
    /** The number of refinements that the scoring algorithm should perform. This number must be at least 1. */
    private final int nrOfRefinements;

    /**
     * Instantiates a new Walkinshaw local similarity scorer that performs only a single refinement.
     * 
     * @param lhs The left-hand-side GLTS, which has at least one state.
     * @param rhs The right-hand-side GLTS, which has at least one state.
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
     * @param lhs The left-hand-side GLTS, which has at least one state.
     * @param rhs The right-hand-side GLTS, which has at least one state.
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
        return computeScores((glts, state) -> glts.getOutgoingTransitions(state), Transition::getTarget, true);
    }

    @Override
    protected RealMatrix computeBackwardSimilarityScores() {
        return computeScores((glts, state) -> glts.getIncomingTransitions(state), Transition::getSource, false);
    }

    /**
     * Computes local similarity scores for every pair of (LHS, RHS)-state pairs. Details of this computation are in the
     * TOSEM 2013 article of Walkinshaw et al. However, this implementation has been generalized by a notion of
     * combinability (see {@link Combiner}) to determine the amount of overlap between transition properties.
     * <p>
     * More specifically, this function implements Equation (6) of the article of Walkinshaw et al. (and its "Prev"
     * counterpart, depending on how {@code commonNeighbors} and {@code relevantProperties} are specified), as a
     * refinement operation. The equation system as presented in the paper is recursive, and this implementation limits
     * the recursion depth to be {@code nrOfRefinements} (which must be positive). If only a single refinement is
     * performed, then the computed similarity scores should be the same as come out of Equations (2) and (4) in the
     * article. However, by increasing the number of refinements further, the scores as computed by
     * {@link WalkinshawGlobalScorer} can be approximated more closely.
     * </p>
     * 
     * @param relevantTransitions A function that, given an GLTS and a state of that GLTS, determines the list of
     *     transitions of the given state that are relevant for computing state similarity scores. This function should
     *     be unidirectional, in the sense that it should consistently give either all incoming transitions or all
     *     outgoing transitions of the given state.
     * @param stateSelector A function that indicates the endpoint state of any given transition that is relevant for
     *     computing state similarity scores. This function should consistently give either the source or the target
     *     state of any given transition, and should be consistent with {@code relevantTransitions}, in the sense that,
     *     if {@code relevantTransitions} gives all outgoing transitions, then this function should select the target
     *     state of any given transition (and likewise it should select source states in case of incoming transitions).
     * @param isForward Whether the state similarity score equation is for the forward or the backward direction.
     * @return A matrix of local state similarity scores, all of which are in the range [-1,1].
     */
    private RealMatrix computeScores(BiFunction<U, State<S>, List<Transition<S, T>>> relevantTransitions,
            Function<Transition<S, T>, State<S>> stateSelector, boolean isForward)
    {
        // Define an initial similarity score matrix with score 0 for every (LHS, RHS)-state pair.
        RealMatrix scores = new OpenMapRealMatrix(lhs.size(), rhs.size());

        // Refine the scores a number of times. At least one refinement will always be performed.
        for (int i = 0; i < nrOfRefinements; i++) {
            RealMatrix refinedScores = new OpenMapRealMatrix(lhs.size(), rhs.size());

            // Iterate over all pairs of (LHS, RHS)-states.
            for (State<S> leftState: lhs.getStates()) {
                for (State<S> rightState: rhs.getStates()) {
                    // Refine the similarity score of the current state pair.
                    double refinedScore = similarityScore(leftState, rightState, scores, relevantTransitions,
                            stateSelector, isForward);

                    // Store the refined similarity score in 'refinedScores'.
                    refinedScores.setEntry(leftState.getId(), rightState.getId(), refinedScore);
                }
            }

            scores = refinedScores;
        }

        return scores;
    }

    /**
     * Given a pair ({@code leftState}, {@code rightState}) of (LHS, RHS)-states, calculate a similarity score between
     * them as indicated by Equation (6) in the TOSEM 2013 article of Walkinshaw et al., where the similarity scores of
     * all state pairs on the right-hand side of this equation are resolved using {@code scores}.
     * 
     * @param leftState A LHS state.
     * @param rightState A RHS state.
     * @param scores The current matrix of similarity scores that is to be refined.
     * @param relevantTransitions A function that, given an GLTS and a state of that GLTS, determines the list of
     *     transitions of the given state that are relevant for computing state similarity scores. This function should
     *     be unidirectional, in the sense that it should consistently give either all incoming transitions or all
     *     outgoing transitions of the given state.
     * @param stateSelector A function that indicates the endpoint state of any given transition that is relevant for
     *     computing state similarity scores. This function should consistently give either the source or the target
     *     state of any given transition, and should be consistent with {@code relevantTransitions}, in the sense that,
     *     if {@code relevantTransitions} gives all outgoing transitions, then this function should select the target
     *     state of any given transition (and likewise it should select source states in case of incoming transitions).
     * @param isForward Whether the state similarity score equation is for the forward or the backward direction.
     * @return A state similarity score for the given pair of (LHS, RHS)-states, in the range [-1,1].
     */
    private double similarityScore(State<S> leftState, State<S> rightState, RealMatrix scores,
            BiFunction<U, State<S>, List<Transition<S, T>>> relevantTransitions,
            Function<Transition<S, T>, State<S>> stateSelector, boolean isForward)
    {
        // If 'leftState' and 'rightState' are uncombinable, then their similarity score is -1.
        if (!statePropertyCombiner.areCombinable(leftState.getProperty(), rightState.getProperty())) {
            return -1d;
        }

        // Get all relevant neighbors of 'leftState' and 'rightState' for computing the similarity score.
        // The similarity score is a fraction.
        Collection<Transition<S, T>> leftTransitions = relevantTransitions.apply(lhs, leftState);
        Collection<Transition<S, T>> rightTransitions = relevantTransitions.apply(rhs, rightState);

        List<Pair<State<S>, State<S>>> neighbors = getCommonNeighborStatePairs(leftTransitions, rightTransitions,
                stateSelector);

        // Shortcut to improve performance (having no neighbors means that the numerator will always be zero).
        if (neighbors.isEmpty()) {
            return 0d;
        }

        // First calculate its denominator. Details are in the paper.

        // Note that, with respect to the original paper we change the notion of transition properties
        // "that do not match each other" to be transition properties "that do not combine with each other".
        double denominator = 2 * (numberOfUncombinableTransitions(leftTransitions, rightTransitions)
                + numberOfUncombinableTransitions(rightTransitions, leftTransitions) + neighbors.size()
                + getDenominatorAdjustment(leftState, rightState, isForward));

        // Shortcut to improve performance.
        if (denominator == 0) {
            return 0d;
        }

        // Second, calculate its numerator. Details are in the paper.
        double numerator = getNumeratorAdjustment(leftState, rightState, isForward);

        for (Pair<State<S>, State<S>> neighbor: neighbors) {
            int lhsIdx = neighbor.getFirst().getId();
            int rhsIdx = neighbor.getSecond().getId();
            numerator += 1d + attenuationFactor * scores.getEntry(lhsIdx, rhsIdx);
        }

        // Calculate the new score.
        return numerator / denominator;
    }

    /**
     * Gives an adjustment to the numerator of the fractional similarity score equation for the given state pair. This
     * adjustment, together with {@code #getDenominatorAdjustment}, must ensure that state similarity scores stay within
     * the range [-1,1].
     * 
     * @param leftState A LHS state.
     * @param rightState A RHS state.
     * @param Whether the state similarity score equation is for the forward or the backward direction.
     * @return An adjustment to the numerator of the fractional similarity score equation for the given state pair.
     */
    protected double getNumeratorAdjustment(State<S> leftState, State<S> rightState, boolean isForward) {
        return 0d;
    }

    /**
     * Gives an adjustment to the denominator of the fractional similarity score equation for the given state pair. This
     * adjustment, together with {@code #getNumeratorAdjustment}, must ensure that state similarity scores stay within
     * the range [-1,1].
     * 
     * @param leftState A LHS state.
     * @param rightState A RHS state.
     * @param Whether the state similarity score equation is for the forward or the backward direction.
     * @return An adjustment to the denominator of the fractional similarity score equation for the given state pair.
     */
    protected double getDenominatorAdjustment(State<S> leftState, State<S> rightState, boolean isForward) {
        return 0d;
    }
}
