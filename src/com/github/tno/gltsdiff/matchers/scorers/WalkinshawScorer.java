//////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2021-2023 Contributors to the GitHub community
//
// This program and the accompanying materials are made available
// under the terms of the MIT License which is available at
// https://opensource.org/licenses/MIT
//
// SPDX-License-Identifier: MIT
//////////////////////////////////////////////////////////////////////////////

package com.github.tno.gltsdiff.matchers.scorers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.util.Pair;

import com.github.tno.gltsdiff.glts.GLTS;
import com.github.tno.gltsdiff.glts.State;
import com.github.tno.gltsdiff.glts.Transition;
import com.github.tno.gltsdiff.matchers.Matcher;
import com.github.tno.gltsdiff.operators.combiners.Combiner;
import com.google.common.base.Preconditions;

/**
 * Scorer that computes local similarity scores, using the approach described by Walkinshaw et al.
 *
 * <p>
 * Implements base functionality for scorers that are described in the article by Walkinshaw et al. (TOSEM 2013).
 * However, this implementation generalizes the approaches described in the article by a more general concept of
 * combinability (see {@link Combiner}).
 * </p>
 *
 * <p>
 * The equation system as described by Walkinshaw et al. produces similarity scores guaranteed to be in the range
 * [-1,1]. This implementation converts all negative similarity scores to {@link Double#NEGATIVE_INFINITY} instead after
 * having solved the equation systems, indicating to the {@link Matcher matchers} that the corresponding state pair is
 * incompatible and should never be merged (which is in line with StateChum). So the scores computed by this
 * implementation are guaranteed to be either {@link Double#NEGATIVE_INFINITY} or be in the range [0,1].
 * </p>
 *
 * @param <S> The type of state properties.
 * @param <T> The type of transition properties.
 * @param <U> The type of GLTSs.
 */
public abstract class WalkinshawScorer<S, T, U extends GLTS<S, T>> implements SimilarityScorer<S, T, U> {
    /**
     * The attenuation factor, the ratio in the range [0,1] that determines how much the similarity scores of far-away
     * states influence the final similarity scores.
     *
     * <p>
     * This factor can be tweaked a bit if the comparison results come out unsatisfactory. A ratio of 0 would mean that
     * only local similarity scores are used. Note that, if one is only interested in local similarity,
     * {@link WalkinshawLocalGLTSScorer} should be used instead, which gives the same result but is much faster. A ratio
     * of 1 would mean that far-away state similarities contribute equally much as local ones.
     * </p>
     */
    protected final double attenuationFactor;

    /** The combiner for state properties. */
    protected final Combiner<S> statePropertyCombiner;

    /** The combiner for transition properties. */
    protected final Combiner<T> transitionPropertyCombiner;

    /**
     * Instantiates a new Walkinshaw similarity scorer. Uses an attenuation factor of 0.6.
     *
     * @param statePropertyCombiner The combiner for state properties.
     * @param transitionPropertyCombiner The combiner for transition properties.
     */
    public WalkinshawScorer(Combiner<S> statePropertyCombiner, Combiner<T> transitionPropertyCombiner) {
        this(statePropertyCombiner, transitionPropertyCombiner, 0.6d);
    }

    /**
     * Instantiates a new Walkinshaw similarity scorer.
     *
     * @param statePropertyCombiner The combiner for state properties.
     * @param transitionPropertyCombiner The combiner for transition properties.
     * @param attenuationFactor The attenuation factor, the ratio in the range [0,1] that determines how much the
     *     similarity scores of far-away states influence the final similarity scores. This factor can be tweaked a bit
     *     if the comparison results come out unsatisfactory. A ratio of 0 would mean that only local similarity scores
     *     are used. Note that, if one is only interested in local similarity, {@link WalkinshawLocalGLTSScorer} should
     *     be used instead, which gives the same result but is much faster. A ratio of 1 would mean that far-away state
     *     similarities contribute equally much as local ones.
     */
    public WalkinshawScorer(Combiner<S> statePropertyCombiner, Combiner<T> transitionPropertyCombiner,
            double attenuationFactor)
    {
        Preconditions.checkArgument(0 <= attenuationFactor && attenuationFactor <= 1,
                "Expected attenuation factor to be in the range [0,1].");

        this.statePropertyCombiner = statePropertyCombiner;
        this.transitionPropertyCombiner = transitionPropertyCombiner;
        this.attenuationFactor = attenuationFactor;
    }

    @Override
    public RealMatrix compute(U lhs, U rhs) {
        // Check inputs.
        Preconditions.checkArgument(lhs.size() > 0, "Expected the LHS to have at least one state.");
        Preconditions.checkArgument(rhs.size() > 0, "Expected the RHS to have at least one state.");

        // Calculate forward and backward similarity scores.
        RealMatrix forwardScores = computeForwardSimilarityScores(lhs, rhs);
        RealMatrix backwardScores = computeBackwardSimilarityScores(lhs, rhs);

        // Ensure that the score matrices have the right dimensions.
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
     * Compute forward similarity scores.
     *
     * @param lhs The left-hand-side (LHS) GLTS.
     * @param rhs The right-hand-side (RHS) GLTS.
     * @return The computed {@code lhs.size()} times {@code rhs.size()} matrix of forward state similarity scores, all
     *     of which are in the range [-1,1], where any negative score indicates an incompatible state pair.
     */
    protected abstract RealMatrix computeForwardSimilarityScores(U lhs, U rhs);

    /**
     * Compute backward similarity scores.
     *
     * @param lhs The left-hand-side (LHS) GLTS.
     * @param rhs The right-hand-side (RHS) GLTS.
     * @return The computed {@code lhs.size()} times {@code rhs.size()} matrix of backward state similarity scores, all
     *     of which are in the range [-1,1], where any negative score indicates an incompatible state pair.
     */
    protected abstract RealMatrix computeBackwardSimilarityScores(U lhs, U rhs);

    /**
     * Returns an adjustment to the numerator of the fractional similarity score equation for the given state pair. This
     * adjustment, together with {@code #getDenominatorAdjustment}, must ensure that state similarity scores stay within
     * the range [-1,1].
     *
     * @param lhs The left-hand-side (LHS) GLTS.
     * @param rhs The right-hand-side (RHS) GLTS.
     * @param leftState A LHS state.
     * @param rightState A RHS state.
     * @param isForward Whether the state similarity score equation is for the forward or the backward direction.
     * @return An adjustment to the numerator of the fractional similarity score equation for the given state pair.
     */
    protected double getNumeratorAdjustment(U lhs, U rhs, State<S> leftState, State<S> rightState, boolean isForward) {
        return 0d;
    }

    /**
     * Returns an adjustment to the denominator of the fractional similarity score equation for the given state pair.
     * This adjustment, together with {@code #getNumeratorAdjustment}, must ensure that state similarity scores stay
     * within the range [-1,1].
     *
     * @param lhs The left-hand-side (LHS) GLTS.
     * @param rhs The right-hand-side (RHS) GLTS.
     * @param leftState A LHS state.
     * @param rightState A RHS state.
     * @param isForward Whether the state similarity score equation is for the forward or the backward direction.
     * @return An adjustment to the denominator of the fractional similarity score equation for the given state pair.
     */
    protected double getDenominatorAdjustment(U lhs, U rhs, State<S> leftState, State<S> rightState,
            boolean isForward)
    {
        return 0d;
    }

    /**
     * Counts the number of transitions in {@code first} for which there does not exist any transition in {@code second}
     * with a transition property that is combinable.
     *
     * @param first The first collection of transitions.
     * @param second The second collection of transitions.
     * @return The number of transitions in {@code first} with a property that is not combinable with the property of
     *     any transition in {@code second}.
     */
    protected long numberOfUncombinableTransitions(Collection<Transition<S, T>> first,
            Collection<Transition<S, T>> second)
    {
        return first.stream()
                .filter(f -> second.stream()
                        .noneMatch(s -> transitionPropertyCombiner.areCombinable(f.getProperty(), s.getProperty())))
                .count();
    }

    /**
     * Collects the list of endpoint states (i.e., common neighbors) of all pairs of transitions from {@code first} and
     * {@code second} whose transition properties are combinable. The endpoint states of all such transition pairs are
     * determined using {@code stateSelector}.
     *
     * @param first The first collection of transitions.
     * @param second The second collection of transitions.
     * @param stateSelector The selector function that determines which endpoint states are to be considered. This
     *     function should consistently give either the source state or the target state of any given transition.
     * @return The list of pairs of endpoint states, of all transition pairs with combinable transition properties.
     */
    protected List<Pair<State<S>, State<S>>> getCommonNeighborStatePairs(Collection<Transition<S, T>> first,
            Collection<Transition<S, T>> second, Function<Transition<S, T>, State<S>> stateSelector)
    {
        return first.stream()
                // Get all pairs of transitions with combinable properties, and obtain their relevant endpoint states.
                .flatMap(f -> second.stream()
                        .filter(s -> transitionPropertyCombiner.areCombinable(f.getProperty(), s.getProperty()))
                        .map(s -> Pair.create(stateSelector.apply(f), stateSelector.apply(s))))
                // Collect all such pairs of endpoint states (i.e., common neighbors) into an array list.
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
