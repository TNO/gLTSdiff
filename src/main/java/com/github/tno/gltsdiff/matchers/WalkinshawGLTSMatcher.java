//////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2021-2023 Contributors to the GitHub community
//
// This program and the accompanying materials are made available
// under the terms of the MIT License which is available at
// https://opensource.org/licenses/MIT
//
// SPDX-License-Identifier: MIT
//////////////////////////////////////////////////////////////////////////////

package com.github.tno.gltsdiff.matchers;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.util.Pair;

import com.github.tno.gltsdiff.glts.GLTS;
import com.github.tno.gltsdiff.glts.State;
import com.github.tno.gltsdiff.operators.combiners.Combiner;
import com.github.tno.gltsdiff.scorers.SimilarityScorer;
import com.github.tno.gltsdiff.utils.GLTSUtils;
import com.github.tno.gltsdiff.utils.Maps;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

/**
 * Matcher for {@link GLTS GLTSs} based on landmarks, 'obviously' matching state pairs, as proposed by Walkinshaw et al.
 *
 * <p>
 * Implements the algorithm as proposed by Walkinshaw et al. (TOSEM 2013; see Section 4.3.1). However, this
 * implementation has been generalized with respect to the approach of Walkinshaw et al. by a more general notion of
 * combinability (see {@link Combiner}). Transitions are now considered to be "common" if they have combinable
 * properties, rather than the stronger condition that they must be equal.
 * </p>
 *
 * @param <S> The type of state properties.
 * @param <T> The type of transition properties.
 * @param <U> The type of GLTSs.
 */
public class WalkinshawGLTSMatcher<S, T, U extends GLTS<S, T>> extends ScoringMatcher<S, T, U> {
    /** The combiner for state properties. */
    private final Combiner<S> statePropertyCombiner;

    /** The combiner for transition properties. */
    private final Combiner<T> transitionPropertyCombiner;

    /**
     * The landmark threshold value, i.e., the fraction of best scoring state pairs to consider as landmarks. That is,
     * of all the possible pairs of (LHS, RHS)-states, only the top so-many scoring pairs are considered. For example,
     * 0.25 means the top 25%.
     *
     * <p>
     * This threshold can be tweaked a bit if the state matchings appear too arbitrary, but should stay within the
     * interval [0,1]. A threshold of 0 would mean that no landmarks will be picked. A threshold of 1.0 would would mean
     * that all state combinations are potential landmarks. Any value lower than 0.1 or higher than 0.5 will likely give
     * undesired results.
     * </p>
     */
    private final double landmarkThreshold;

    /**
     * The landmark ratio, indicating the ratio that a candidate landmark should be better than another one, to be
     * considered. That is, if during state matching, there are multiple (conflicting) candidate matches to be
     * considered, continue with the highest of these candidate matches, but only if it is significantly better than any
     * other candidate, where the significance is determined by this ratio.
     *
     * <p>
     * This factor can be tweaked a bit if the matching results turn out unsatisfactory, or if there happen to be many
     * conflicting matches. In such a scenario, lowering this ratio might help. It does not make sense to have it lower
     * than 1.0.
     * </p>
     */
    private final double landmarkRatio;

    /**
     * Instantiates a new Walkinshaw matcher for GLTSs. Uses a landmark threshold of 0.25 and a landmark ratio of 1.5.
     *
     * @param scorer The algorithm for computing state similarity scores.
     * @param statePropertyCombiner The combiner for state properties.
     * @param transitionPropertyCombiner The combiner for transition properties.
     */
    public WalkinshawGLTSMatcher(SimilarityScorer<S, T, U> scorer, Combiner<S> statePropertyCombiner,
            Combiner<T> transitionPropertyCombiner)
    {
        this(scorer, statePropertyCombiner, transitionPropertyCombiner, 0.25d, 1.5d);
    }

    /**
     * Instantiates a new Walkinshaw matcher for GLTSs.
     *
     * @param scorer The algorithm for computing state similarity scores.
     * @param statePropertyCombiner The combiner for state properties.
     * @param transitionPropertyCombiner The combiner for transition properties.
     * @param landmarkThreshold The landmark threshold value, i.e., the fraction of best scoring state pairs to consider
     *     as landmarks. That is, of all the possible pairs of (LHS, RHS)-states, only the top so-many scoring pairs are
     *     considered. For example, 0.25 means the top 25%. This threshold can be tweaked a bit if the state matchings
     *     appear too arbitrary, but should stay within the interval [0,1]. A threshold of 0 would mean that no
     *     landmarks will be picked. A threshold of 1.0 would would mean that all state combinations are potential
     *     landmarks. Any value lower than 0.1 or higher than 0.5 will likely give undesired results.
     * @param landmarkRatio The landmark ratio, indicating the ratio that a candidate landmark should be better than
     *     another one, to be considered. That is, if during state matching, there are multiple (conflicting) candidate
     *     matches to be considered, continue with the highest of these candidate matches, but only if it is
     *     significantly better than any other candidate, where the significance is determined by this ratio. This
     *     factor can be tweaked a bit if the matching results turn out unsatisfactory, or if there happen to be many
     *     conflicting matches. In such a scenario, lowering this ratio might help. It does not make sense to have it
     *     lower than 1.0.
     */
    public WalkinshawGLTSMatcher(SimilarityScorer<S, T, U> scorer, Combiner<S> statePropertyCombiner,
            Combiner<T> transitionPropertyCombiner, double landmarkThreshold, double landmarkRatio)
    {
        super(scorer);
        this.statePropertyCombiner = statePropertyCombiner;
        this.transitionPropertyCombiner = transitionPropertyCombiner;
        this.landmarkThreshold = landmarkThreshold;
        this.landmarkRatio = landmarkRatio;
    }

    @Override
    protected Map<State<S>, State<S>> computeInternal(U lhs, U rhs, RealMatrix scores) {
        // Define a function that gives the similarity score of every pair of (LHS, RHS)-states.
        BiFunction<State<S>, State<S>, Double> getScore = (l, r) -> scores.getEntry(l.getId(), r.getId());

        // Construct a matching by using Walkinshaw's approach.
        Set<Pair<State<S>, State<S>>> landmarks = identifyLandmarks(lhs, rhs, getScore);
        Set<Pair<State<S>, State<S>>> matches = identifyKeyPairs(lhs, rhs, landmarks, getScore);

        // All matched pairs of states should be compatible.
        for (Pair<State<S>, State<S>> statePair: matches) {
            Preconditions.checkArgument(isCompatible(statePair, getScore),
                    "Expected all matched state pairs to be compatible.");
        }

        return matches.stream()
                .collect(Collectors.toMap(Pair::getFirst, Pair::getSecond, Maps.throwingMerger(), LinkedHashMap::new));
    }

    /**
     * Identifies a set of landmarks. A landmark is a pair of (LHS, RHS)-states that are compatible with respect to
     * {@link #isCompatible}, and make a very clear match with respect to the given state similarity scoring function.
     *
     * @param lhs The left-hand-side (LHS) GLTS.
     * @param rhs The right-hand-side (RHS) GLTS.
     * @param scores A scoring function that expresses for all (LHS, RHS)-state pairs how structurally similar they are.
     *     All state similarity scores must either be within the range [0,1] or be {@link Double#POSITIVE_INFINITY}.
     * @return A set of compatible state pairs that are clear matches with respect to {@code scores}.
     *     <p>
     *     It may be that no landmarks can be found, in which case the returned set is empty. This only happens if the
     *     LHS and RHS are completely incompatible, meaning that every possible pair of (LHS, RHS)-states are a mismatch
     *     with respect to {@code scores}.
     *     </p>
     *     <p>
     *     All returned landmarks are guaranteed to be non-{@code null}. Furthermore, the returned landmarks are
     *     guaranteed not to overlap (any LHS/RHS state is involved in at most one landmark), and be compatible
     *     according to {@link #isCompatible}.
     *     </p>
     */
    private Set<Pair<State<S>, State<S>>> identifyLandmarks(U lhs, U rhs,
            BiFunction<State<S>, State<S>, Double> scores)
    {
        // First determine and collect the so-many best scoring state pairs. We will only consider these.
        int nrOfPairsToConsider = (int)Math.ceil(landmarkThreshold * lhs.size() * rhs.size());

        List<Pair<Pair<State<S>, State<S>>, Double>> bestScoringPairs = //
                getScorePairs(lhs, rhs, scores).stream() // Get compatible state pairs.
                        .sorted((s1, s2) -> s2.getSecond().compareTo(s1.getSecond())) // Sort on scores in desc. order.
                        .limit(nrOfPairsToConsider) // Only take the so-many best scoring pairs.
                        .collect(Collectors.toList());

        // Out of 'bestScoringStatePairs' construct a map from LHS states to the list of compatible RHS states
        // to which they could be matched, paired with their similarity scores.
        Map<State<S>, List<Pair<State<S>, Double>>> candidateMap = new LinkedHashMap<>();

        for (Pair<Pair<State<S>, State<S>>, Double> statePair: bestScoringPairs) {
            State<S> leftState = statePair.getFirst().getFirst();
            State<S> rightState = statePair.getFirst().getSecond();

            List<Pair<State<S>, Double>> candidates = candidateMap.get(leftState);

            if (candidates == null) {
                candidates = new ArrayList<>();
                candidateMap.put(leftState, candidates);
            }

            candidates.add(Pair.create(rightState, statePair.getSecond()));
        }

        // Try to find landmarks, which will be stored in 'landmarks'.
        Set<Pair<State<S>, State<S>>> landmarks = new LinkedHashSet<>();

        // Iterate over all LHS states that are involved in at least one pair within 'bestScoringPairs', in the order in
        // which they occur. (The exploration order may be important here.)

        // The for-loop below maintains the following two invariants: (1) all LHS states in 'landmarks' are unique
        // (which is guaranteed since 'bestScoringLeftStates' contains only unique elements); and (2) all RHS states in
        // 'landmarks' are unique (which is guaranteed by maintaining the set 'matchedRightStates').
        // As a consequence, all landmarks that we store will be completely disjoint.

        Set<State<S>> matchedRightStates = new LinkedHashSet<>();

        // Obtain an order-preserved list of unique LHS states occurring in 'bestScoringStatePairs'.
        List<State<S>> bestScoringLeftStates = bestScoringPairs.stream().map(p -> p.getFirst().getFirst()).distinct()
                .collect(Collectors.toList());

        for (State<S> leftState: bestScoringLeftStates) {
            // Find all (compatible) RHS states that could still be potential matches for 'leftState'.
            List<Pair<State<S>, Double>> potentialMatches = candidateMap.get(leftState).stream()
                    .filter(p -> !matchedRightStates.contains(p.getFirst())) // Remove already matched RHS states.
                    .collect(Collectors.toList());

            if (potentialMatches.size() == 1) {
                // If there is only one potential match then add it to the set of current landmarks.
                State<S> rightState = potentialMatches.get(0).getFirst();
                landmarks.add(Pair.create(leftState, rightState));
                matchedRightStates.add(rightState);
            } else if (potentialMatches.size() > 1) {
                // If there are multiple possible matches, then things become slightly more difficult. The idea is to
                // select the best match, but only if it's significantly better than all other possible matches, where
                // the significance is determined by 'this.landmarkRatio'.

                // Collect the best two possible matches (i.e., the two with the highest scores).
                List<Pair<State<S>, Double>> bestPossibleMatches = potentialMatches.stream()
                        .sorted((m1, m2) -> m2.getSecond().compareTo(m1.getSecond())) // Sort by scores in desc order.
                        .limit(2) // Select the two highest scoring matches.
                        .collect(Collectors.toList());

                // Get the two individual filtered-out matches.
                Pair<State<S>, Double> bestMatch = bestPossibleMatches.get(0);
                Pair<State<S>, Double> secondBestMatch = bestPossibleMatches.get(1);

                // If the best match is more than 'landmarkRatio' times better than the second-best match,
                // only then add it to the set of current landmarks.
                if (bestMatch.getSecond() > secondBestMatch.getSecond() * landmarkRatio) {
                    State<S> rightState = bestMatch.getFirst();
                    landmarks.add(Pair.create(leftState, rightState));
                    matchedRightStates.add(rightState);
                }
            }
        }

        // It might be that no landmarks were found. In that case, try to identify fallback landmarks.
        if (landmarks.isEmpty()) {
            landmarks.addAll(getFallbackLandmarks(lhs, rhs, scores));
        }

        return landmarks;
    }

    /**
     * Converts a similarity scoring function into a list of (LHS, RHS)-state pairs, with their similarity scores,
     * thereby including only state pairs that are compatible with respect to {@link #isCompatible}.
     *
     * @param lhs The left-hand-side (LHS) GLTS.
     * @param rhs The right-hand-side (RHS) GLTS.
     * @param scores A scoring function that expresses for all (LHS, RHS)-state pairs how structurally similar they are.
     *     All state similarity scores must either be within the range [0,1] or be {@link Double#POSITIVE_INFINITY}.
     * @return The conversion result, containing only (LHS, RHS)-state pairs that are compatible.
     */
    private List<Pair<Pair<State<S>, State<S>>, Double>> getScorePairs(U lhs, U rhs,
            BiFunction<State<S>, State<S>, Double> scores)
    {
        List<Pair<Pair<State<S>, State<S>>, Double>> pairs = new ArrayList<>(lhs.size() * rhs.size());

        for (State<S> leftState: lhs.getStates()) {
            for (State<S> rightState: rhs.getStates()) {
                Pair<State<S>, State<S>> statePair = Pair.create(leftState, rightState);

                if (isCompatible(statePair, scores)) {
                    pairs.add(Pair.create(statePair, scores.apply(leftState, rightState)));
                }
            }
        }

        return pairs;
    }

    /**
     * Gives a set of fallback landmarks, which is used in case no ordinary landmark can be found.
     *
     * @param lhs The left-hand-side (LHS) GLTS.
     * @param rhs The right-hand-side (RHS) GLTS.
     * @param scores A scoring function that expresses for all (LHS, RHS)-state pairs how structurally similar they are.
     *     All state similarity scores must either be within the range [0,1] or be {@link Double#POSITIVE_INFINITY}.
     * @return A set of non-{@code null} fallback landmarks. These fallback landmarks are guaranteed not to overlap (any
     *     LHS/RHS state is involved in at most one landmark), and be compatible according to {@link #isCompatible}.
     */
    protected Set<Pair<State<S>, State<S>>> getFallbackLandmarks(U lhs, U rhs,
            BiFunction<State<S>, State<S>, Double> scores)
    {
        return ImmutableSet.of();
    }

    /**
     * Given a set of landmarks, determines which (LHS, RHS)-states should be matched to one another. These matched
     * states can then later be merged into a single state when computing the final merged GLTS.
     *
     * <p>
     * More details about the algorithm and its heuristics can be found on page 17 in the article of Walkinshaw et al.,
     * in particular Algorithm 1 and the accompanying explanation.
     * </p>
     *
     * @param lhs The left-hand-side (LHS) GLTS.
     * @param rhs The right-hand-side (RHS) GLTS.
     * @param landmarks The set of input landmarks.
     * @param scores A scoring function that expresses for all (LHS, RHS)-state pairs how structurally similar they are.
     *     All state similarity scores must either be within the range [0,1] or be {@link Double#POSITIVE_INFINITY}.
     * @return The set of state pairs to match to one another, which are all guaranteed to be compatible with respect to
     *     {@link #isCompatible}.
     */
    private Set<Pair<State<S>, State<S>>> identifyKeyPairs(U lhs, U rhs, Set<Pair<State<S>, State<S>>> landmarks,
            BiFunction<State<S>, State<S>, Double> scores)
    {
        Set<Pair<State<S>, State<S>>> keyPairs = new LinkedHashSet<>(landmarks);
        Set<Pair<State<S>, State<S>>> neighPairs = relevantNeighboringPairs(lhs, rhs, keyPairs, scores);

        while (!neighPairs.isEmpty()) {
            while (!neighPairs.isEmpty()) {
                // Pick the highest scoring pair in 'neighPairs', and add it to 'keyPairs'.
                Pair<State<S>, State<S>> highestScoringPair = neighPairs.stream().sorted((p1, p2) -> scores
                        // Sort in descending order.
                        .apply(p2.getFirst(), p2.getSecond()).compareTo(scores.apply(p1.getFirst(), p1.getSecond())))
                        // Take only the highest scoring pair of states.
                        .limit(1).collect(Collectors.toList()).get(0);

                keyPairs.add(highestScoringPair);
                neighPairs = removeConflicts(neighPairs, highestScoringPair);
            }

            neighPairs = relevantNeighboringPairs(lhs, rhs, keyPairs, scores);
        }

        return keyPairs;
    }

    /**
     * Determines all relevant neighboring (LHS, RHS)-state pairs for a given set of state pairs. A neighboring state
     * pair is said to be <i>relevant</i> in this context if it surrounds an element of {@code statePairs} and is
     * disjoint from {@code statePairs}.
     *
     * @param lhs The left-hand-side (LHS) GLTS.
     * @param rhs The right-hand-side (RHS) GLTS.
     * @param statePairs The set of (LHS, RHS)-state pairs for which relevant neighboring are to be found.
     * @param scores A scoring function that expresses for all (LHS, RHS)-state pairs how structurally similar they are.
     *     All state similarity scores must either be within the range [0,1] or be {@link Double#POSITIVE_INFINITY}.
     * @return The set of relevant neighboring state pairs of {@code statePairs} that are compatible with respect to
     *     {@link #isCompatible}.
     */
    private Set<Pair<State<S>, State<S>>> relevantNeighboringPairs(U lhs, U rhs,
            Set<Pair<State<S>, State<S>>> statePairs, BiFunction<State<S>, State<S>, Double> scores)
    {
        Set<Pair<State<S>, State<S>>> foundPairs = new LinkedHashSet<>();

        // Get all LHS and RHS states occurring in 'statePairs'.
        Pair<Set<State<S>>, Set<State<S>>> unzippedPairs = unzip(statePairs);
        Set<State<S>> leftStates = unzippedPairs.getFirst();
        Set<State<S>> rightStates = unzippedPairs.getSecond();

        for (Pair<State<S>, State<S>> statePair: statePairs) {
            // Find all compatible (LHS, RHS)-state pairs that surround 'statePair'.
            Set<Pair<State<S>, State<S>>> surrPairs = surroundingPairs(lhs, rhs, statePair, scores);

            // Collect any surrounding pairs that have states that already occur in 'statePairs'.
            Set<Pair<State<S>, State<S>>> relevantPairs = surrPairs.stream()
                    .filter(p -> !leftStates.contains(p.getFirst()) && !rightStates.contains(p.getSecond()))
                    .collect(Collectors.toCollection(LinkedHashSet::new));

            foundPairs.addAll(relevantPairs);
        }

        return foundPairs;
    }

    /**
     * Internal utility function for unzipping a given {@code set} of pairs.
     *
     * @param <A> The type of the first elements of the pairs.
     * @param <B> The type of the second elements of the pairs.
     * @param set The set of pairs to unzip.
     * @return The unzipped pairs.
     */
    private static <A, B> Pair<Set<A>, Set<B>> unzip(Set<Pair<A, B>> set) {
        Set<A> left = new LinkedHashSet<>();
        Set<B> right = new LinkedHashSet<>();

        for (Pair<A, B> element: set) {
            left.add(element.getFirst());
            right.add(element.getSecond());
        }

        return Pair.create(left, right);
    }

    /**
     * Gives the set of compatible (LHS, RHS)-state pairs that surround ({@code leftState}, {@code rightState}), where
     * <i>surround</i> means: reachable from or by a transition with a combinable property. This function follows the
     * definition of 'Surr_{leftState, rightState}' as proposed by Walkinshaw et al. (see page 17), but uses a notion of
     * combinability instead of equality in determining reachable surrounding state pairs.
     *
     * <p>
     * Intuitively, the set of surrounding (LHS, RHS)-state pairs is the set of all pairs of compatible states that
     * ({@code leftState}, {@code rightState}) can "see" by following an incoming/outgoing edge with combinable
     * properties.
     * </p>
     *
     * @param lhs The left-hand-side (LHS) GLTS.
     * @param rhs The right-hand-side (RHS) GLTS.
     * @param statePair The state pair for which to collect all surrounding state pairs.
     * @param scores A scoring function that expresses for all (LHS, RHS)-state pairs how structurally similar they are.
     *     All state similarity scores must either be within the range [0,1] or be {@link Double#POSITIVE_INFINITY}.
     * @return The set of surrounding state pairs of {@code statePair} that are compatible with respect to
     *     {@link #isCompatible}.
     */
    private Set<Pair<State<S>, State<S>>> surroundingPairs(U lhs, U rhs, Pair<State<S>, State<S>> statePair,
            BiFunction<State<S>, State<S>, Double> scores)
    {
        Stream<Pair<State<S>, State<S>>> predecessors = GLTSUtils
                .commonPredecessors(lhs, rhs, transitionPropertyCombiner, statePair).stream();
        Stream<Pair<State<S>, State<S>>> successors = GLTSUtils
                .commonSuccessors(lhs, rhs, transitionPropertyCombiner, statePair).stream();

        // Return all compatible predecessors and successors.
        return Stream.concat(predecessors, successors).filter(pair -> isCompatible(pair, scores))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * Filters-out all elements of {@code statePairs} that overlap with {@code pair}.
     *
     * @param statePairs The (LHS, RHS)-state pairs to filter.
     * @param pair The state pair against which to check state pairs for conflicts.
     * @return The filtered set of state pairs.
     */
    private Set<Pair<State<S>, State<S>>> removeConflicts(Set<Pair<State<S>, State<S>>> statePairs,
            Pair<State<S>, State<S>> pair)
    {
        return statePairs.stream().filter(p -> areDisjoint(p, pair))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * Determines whether two given pairs of (LHS, RHS)-states are disjoint (do not overlap on states).
     *
     * @param left The left (LHS, RHS)-state pair
     * @param right The right (LHS, RHS)-state pair
     * @return {@code true} if the state pairs are disjoint, or {@code false} if they overlap.
     */
    private boolean areDisjoint(Pair<State<S>, State<S>> left, Pair<State<S>, State<S>> right) {
        return left.getFirst() != right.getFirst() && left.getSecond() != right.getSecond();
    }

    /**
     * Determines whether a given pair of states is compatible, i.e., allowed to be matched to one another. A state pair
     * is compatible if it has a finite score, and has combinable state properties.
     *
     * @param statePair The state pair to check.
     * @param scores A scoring function that expresses for all (LHS, RHS)-state pairs how structurally similar they are.
     *     All state similarity scores must either be within the range [0,1] or be {@link Double#POSITIVE_INFINITY}.
     * @return {@code true} if the given state pair is compatible, {@code false} otherwise.
     */
    protected boolean isCompatible(Pair<State<S>, State<S>> statePair, BiFunction<State<S>, State<S>, Double> scores) {
        State<S> leftState = statePair.getFirst();
        State<S> rightState = statePair.getSecond();

        return Double.isFinite(scores.apply(leftState, rightState))
                && statePropertyCombiner.areCombinable(leftState.getProperty(), rightState.getProperty());
    }
}
