//////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2021-2022 Contributors to the GitHub community
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

import com.github.tno.gltsdiff.lts.LTS;
import com.github.tno.gltsdiff.lts.State;
import com.github.tno.gltsdiff.matchers.scorers.SimilarityScorer;
import com.github.tno.gltsdiff.operators.combiners.Combiner;
import com.github.tno.gltsdiff.utils.LTSUtils;

/**
 * Functionality for computing (LHS, RHS)-state matchings based on heuristics proposed by Walkinshaw et al. (TOSEM 2014;
 * see Section 4.3.1). However, this implementation has been generalized with respect to the approach of Walkinshaw et
 * al. by a more general notion of combinability (see {@link Combiner}). Transitions are now considered to be "common"
 * if they have combinable properties, rather than the stronger condition that they must be equal.
 *
 * @param <S> The type of state properties.
 * @param <T> The type of transition properties.
 * @param <U> The type of LTSs.
 */
public class WalkinshawMatcher<S, T, U extends LTS<S, T>> extends ScoringMatcher<S, T, U> {
    /** The left-hand-side LTS. */
    private final U lhs;

    /** The right-hand-side LTS. */
    private final U rhs;

    /**
     * Of all the possible pairs of (LHS, RHS)-states, Walkinshaw et al. propose the heuristic to only consider the top
     * so-many scoring pairs, which is this threshold. (For example, 0.25d means the top 25%).
     * <p>
     * This threshold can be tweaked a bit if the state matchings appear too arbitrary, but should stay within the
     * interval [0,1]. A threshold of 0 would mean that no landmarks will be picked. A threshold of 1 would would mean
     * that all state combinations are potential landmarks. Any value lower than {@code 0.1d} or higher than
     * {@code 0.5d} will likely give undesired results.
     * </p>
     */
    private final double landmarkThreshold = 0.25d;

    /**
     * If, during state matching, there are multiple (conflicting) candidate matches to be considered, Walkinshaw et al.
     * propose to continue with the highest of these candidate matches but only if it is significantly better than any
     * other candidate, where the significance is determined by this ratio.
     * <p>
     * This factor can be tweaked a bit if the matching results turn out unsatisfactory, or if there happen to be many
     * conflicting matches. In such a scenario, lowering this ratio might help. It does not make sense to have it lower
     * than {@code 1.0d}.
     * </p>
     */
    private final double landmarkRatio = 1.5d;

    /** The combiner for state properties. */
    private final Combiner<S> statePropertyCombiner;

    /** The combiner for transition properties. */
    private final Combiner<T> transitionPropertyCombiner;

    /**
     * Instantiates a new Walkinshaw matcher.
     * 
     * @param lhs The left-hand-side LTS.
     * @param rhs The right-hand-side LTS.
     * @param scoring The algorithm for computing state similarity scores.
     * @param statePropertyCombiner The combiner for state properties.
     * @param transitionPropertyCombiner The combiner for transition properties.
     */
    public WalkinshawMatcher(U lhs, U rhs, SimilarityScorer<S, T, U> scoring, Combiner<S> statePropertyCombiner,
            Combiner<T> transitionPropertyCombiner)
    {
        super(scoring);
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

    /**
     * Identifies a set of landmarks. A landmark is a pair of (LHS, RHS)-states that make a very clear match, with
     * respect to the given state similarity scoring function.
     * 
     * @param scores A scoring function that expresses for all (LHS, RHS)-state pairs how structurally similar they are.
     *     All state similarity scores must either be within the range [0,1] or be {@link Double#POSITIVE_INFINITY}.
     * @return A set of state pairs that are clear matches with respect to {@code scores}.
     *     <p>
     *     It may be that no landmarks can be found, in which case the returned set is empty. This only happens if the
     *     LHS and RHS are completely incompatible, meaning that every possible pair of (LHS, RHS)-states are a mismatch
     *     with respect to {@code scores}.
     *     </p>
     *     <p>
     *     All returned landmarks are guaranteed to be non-{@code null}. Furthermore, the returned landmarks are
     *     guaranteed not to overlap (any LHS/RHS state is involved in at most one landmark), and their state properties
     *     are combinable according to {@link #statePropertyCombiner}.
     *     </p>
     */
    private Set<Pair<State<S>, State<S>>> identifyLandmarks(BiFunction<State<S>, State<S>, Double> scores) {
        // First determine and filter-out the best so-many scoring state pairs. We will only consider these.
        int nrOfPairsToConsider = (int)Math.ceil(landmarkThreshold * lhs.size() * rhs.size());

        List<Pair<Pair<State<S>, State<S>>, Double>> bestScoringPairs = getScorePairs(scores).stream()
                .sorted((s1, s2) -> s2.getSecond().compareTo(s1.getSecond())) // Sort on scores in descending order.
                .limit(nrOfPairsToConsider) // Only take the best so-many scoring pairs.
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

                // Filter-out the best two possible matches (i.e., the two with the highest scores).
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

        // It might be that, at this point, no landmarks have been found. If that is the case, try to identify a
        // "fallback" landmark that is defined as the pair of best scoring initial states.

        if (landmarks.isEmpty()) {
            // Iterate over all combinations of initial states, and keep track of the highest scoring combination.
            Pair<State<S>, State<S>> bestCurrentPair = null;

            for (State<S> leftInitialState: lhs.getInitialStates()) {
                for (State<S> rightInitialState: rhs.getInitialStates()) {
                    if (!statePropertyCombiner.areCombinable(leftInitialState.getProperty(),
                            rightInitialState.getProperty()))
                    {
                        continue;
                    }

                    Pair<State<S>, State<S>> pair = Pair.create(leftInitialState, rightInitialState);

                    if (bestCurrentPair == null) {
                        bestCurrentPair = pair;
                    }

                    if (scores.apply(pair.getFirst(), pair.getSecond()) > scores.apply(bestCurrentPair.getFirst(),
                            bestCurrentPair.getSecond()))
                    {
                        bestCurrentPair = pair;
                    }
                }
            }

            // If there turns out to be a highest scoring pair of compatible initial (LHS, RHS)-states,
            // then that will be our landmark. (Otherwise the final merged LTS shall not contain any combined states.)
            if (bestCurrentPair != null) {
                landmarks.add(bestCurrentPair);
            }
        }

        return landmarks;
    }

    /**
     * Gives the set of compatible (LHS, RHS)-state pairs that surround ({@code leftState}, {@code rightState}), where
     * <i>surround</i> means: reachable from or by a transition with a combinable property. This function follows the
     * definition of 'Surr_{leftState, rightState}' as proposed by Walkinshaw et al. (see page 17), but uses a notion of
     * combinability instead of equality in determining reachable surrounding state pairs.
     * <p>
     * Intuitively, the set of surrounding (LHS, RHS)-state pairs is the set of all pairs of states that
     * ({@code leftState}, {@code rightState}) can "see" by following an incoming/outgoing edge with combinable
     * properties.
     * </p>
     */
    private Set<Pair<State<S>, State<S>>> surroundingPairs(Pair<State<S>, State<S>> statePair) {
        Stream<Pair<State<S>, State<S>>> predecessors = LTSUtils
                .commonPredecessors(lhs, rhs, transitionPropertyCombiner, statePair).stream();
        Stream<Pair<State<S>, State<S>>> successors = LTSUtils
                .commonSuccessors(lhs, rhs, transitionPropertyCombiner, statePair).stream();

        // Return all compatible predecessors and successors.
        return Stream
                .concat(predecessors, successors).filter(pair -> statePropertyCombiner
                        .areCombinable(pair.getFirst().getProperty(), pair.getSecond().getProperty()))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * Internal utility function for unzipping a given {@code set} of pairs.
     */
    private <A, B> Pair<Set<A>, Set<B>> unzip(Set<Pair<A, B>> set) {
        Set<A> left = new LinkedHashSet<>();
        Set<B> right = new LinkedHashSet<>();

        for (Pair<A, B> element: set) {
            left.add(element.getFirst());
            right.add(element.getSecond());
        }

        return Pair.create(left, right);
    }

    /**
     * Determines all relevant neighboring (LHS, RHS)-state pairs for a given set of state pairs. A neighboring state
     * pair is said to be <i>relevant</i> in this context if it surrounds an element of {@code statePairs} and is
     * disjoint from {@code statePairs}.
     * 
     * @param statePairs The set of (LHS, RHS)-state pairs for which relevant neighboring are to be found.
     * @return The set of relevant neighboring state pairs of {@code statePairs}.
     */
    private Set<Pair<State<S>, State<S>>> relevantNeighboringPairs(Set<Pair<State<S>, State<S>>> statePairs) {
        Set<Pair<State<S>, State<S>>> foundPairs = new LinkedHashSet<>();

        // Get all LHS and RHS states occurring in 'statePairs'.
        Pair<Set<State<S>>, Set<State<S>>> unzippedPairs = unzip(statePairs);
        Set<State<S>> leftStates = unzippedPairs.getFirst();
        Set<State<S>> rightStates = unzippedPairs.getSecond();

        for (Pair<State<S>, State<S>> statePair: statePairs) {
            // Find all (LHS, RHS)-state pairs that surround 'statePair'.
            Set<Pair<State<S>, State<S>>> surrPairs = surroundingPairs(statePair);

            // Filter-out any surrounding pairs that have states that already occur in 'statePairs'.
            Set<Pair<State<S>, State<S>>> relevantPairs = surrPairs.stream()
                    .filter(p -> !leftStates.contains(p.getFirst()) && !rightStates.contains(p.getSecond()))
                    .collect(Collectors.toCollection(LinkedHashSet::new));

            foundPairs.addAll(relevantPairs);
        }

        return foundPairs;
    }

    /**
     * Determines whether two given pairs of (LHS, RHS)-states are disjoint (do not overlap on states).
     */
    private boolean areDisjoint(Pair<State<S>, State<S>> left, Pair<State<S>, State<S>> right) {
        return left.getFirst() != right.getFirst() && left.getSecond() != right.getSecond();
    }

    /**
     * Filters-out all elements of {@code statePairs} that overlap with {@code pair}.
     */
    private Set<Pair<State<S>, State<S>>> removeConflicts(Set<Pair<State<S>, State<S>>> statePairs,
            Pair<State<S>, State<S>> pair)
    {
        return statePairs.stream().filter(p -> areDisjoint(p, pair))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * Given a set of landmarks, determines which (LHS, RHS)-states should be matched to one another. These matched
     * states can then later be merged into a single state when computing the final merged LTS.
     * <p>
     * More details about the algorithm and its heuristics can be found on page 17 in the article of Walkinshaw et al.,
     * in particular Algorithm 1 and the accompanying explanation.
     * </p>
     */
    private Set<Pair<State<S>, State<S>>> identifyKeyPairs(Set<Pair<State<S>, State<S>>> landmarks,
            BiFunction<State<S>, State<S>, Double> scores)
    {
        Set<Pair<State<S>, State<S>>> keyPairs = new LinkedHashSet<>(landmarks);
        Set<Pair<State<S>, State<S>>> neighPairs = relevantNeighboringPairs(keyPairs);

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

            neighPairs = relevantNeighboringPairs(keyPairs);
        }

        return keyPairs;
    }

    /**
     * Converts a similarity scoring function into a list of (LHS, RHS)-state pairs, with their similarity scores,
     * thereby including only state pairs that have combinable properties and finite state similarity scores.
     * 
     * @param scores A similarity scoring function. All state similarity scores must either be within the range [0,1] or
     *     be {@link Double#POSITIVE_INFINITY}.
     * @return The conversion result, containing only (LHS, RHS)-state pairs with combinable properties and finite state
     *     similarity scores.
     */
    private List<Pair<Pair<State<S>, State<S>>, Double>> getScorePairs(BiFunction<State<S>, State<S>, Double> scores) {
        List<Pair<Pair<State<S>, State<S>>, Double>> pairs = new ArrayList<>(lhs.size() * rhs.size());

        for (State<S> leftState: lhs.getStates()) {
            for (State<S> rightState: rhs.getStates()) {
                double score = scores.apply(leftState, rightState);

                if (Double.isFinite(score)
                        && statePropertyCombiner.areCombinable(leftState.getProperty(), rightState.getProperty()))
                {
                    pairs.add(Pair.create(Pair.create(leftState, rightState), score));
                }
            }
        }

        return pairs;
    }

    @Override
    protected Map<State<S>, State<S>> computeInternal(RealMatrix scores) {
        // Define a function that gives the similarity score of every pair of (LHS, RHS)-states.
        BiFunction<State<S>, State<S>, Double> getScore = (l, r) -> scores.getEntry(l.getId(), r.getId());

        // Construct a matching by using Walkinshaw's approach.
        Set<Pair<State<S>, State<S>>> landmarks = identifyLandmarks(getScore);
        Set<Pair<State<S>, State<S>>> matches = identifyKeyPairs(landmarks, getScore);
        return matches.stream().collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));
    }
}
