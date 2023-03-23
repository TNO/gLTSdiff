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

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.math3.util.Pair;

import com.github.tno.gltsdiff.glts.GLTS;
import com.github.tno.gltsdiff.glts.State;
import com.github.tno.gltsdiff.operators.combiners.Combiner;
import com.github.tno.gltsdiff.utils.GLTSUtils;
import com.github.tno.gltsdiff.utils.Maps;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

/**
 * A brute force matching algorithm for {@link GLTS GLTSs} that calculates a best possible maximal (LHS, RHS)-state
 * matching.
 *
 * <p>
 * The results are <i>best possible</i> (or optimal) in the sense that matchings are computed with the objective to
 * maximize the number of transitions that would be combined in the final merged GLTS. Or, equivalently, it minimizes
 * the number of uncombined transitions. Moreover, the computed matching is guaranteed not to introduce any tangles.
 * </p>
 *
 * <p>
 * This algorithm explores all the possible choices of relevant state matchings, making it brute force. The worst case
 * time complexity is therefore O(N!) with N = min{|LHS|, |RHS|}, where |LHS| and |RHS| are the number of states in the
 * LHS and RHS, respectively. (This can be proven by considering the number possible matching choices in complete
 * bipartite graphs, K<sub>{N,N}</sub>.) However, in practice such instances hardly ever occur.
 * </p>
 *
 * @param <S> The type of state properties.
 * @param <T> The type of transition properties.
 * @param <U> The type of GLTSs.
 */
public class BruteForceGLTSMatcher<S, T, U extends GLTS<S, T>> implements Matcher<S, T, U> {
    /** The combiner for state properties. */
    protected final Combiner<S> statePropertyCombiner;

    /** The combiner for transition properties. */
    protected final Combiner<T> transitionPropertyCombiner;

    /**
     * Instantiates a new brute force matcher for GLTSs.
     *
     * @param statePropertyCombiner The combiner for state properties.
     * @param transitionPropertyCombiner The combiner for transition properties.
     */
    public BruteForceGLTSMatcher(Combiner<S> statePropertyCombiner, Combiner<T> transitionPropertyCombiner) {
        this.statePropertyCombiner = statePropertyCombiner;
        this.transitionPropertyCombiner = transitionPropertyCombiner;
    }

    @Override
    public Map<State<S>, State<S>> compute(U lhs, U rhs) {
        Set<Pair<State<S>, State<S>>> matching = findAnOptimalMatching(lhs, rhs, allStatePairsWithPotential(lhs, rhs));
        return matching.stream()
                .collect(Collectors.toMap(Pair::getFirst, Pair::getSecond, Maps.throwingMerger(), LinkedHashMap::new));
    }

    /**
     * Returns all (LHS, RHS)-state pairs with {@link #hasPotential potential}.
     *
     * @param lhs The left-hand-side (LHS) GLTS.
     * @param rhs The right-hand-side (RHS) GLTS.
     * @return The set of all (LHS, RHS)-state pairs with potential.
     */
    private Set<Pair<State<S>, State<S>>> allStatePairsWithPotential(U lhs, U rhs) {
        Set<Pair<State<S>, State<S>>> pairs = new LinkedHashSet<>(lhs.size() * rhs.size());

        for (State<S> lhsState: lhs.getStates()) {
            for (State<S> rhsState: rhs.getStates()) {
                if (hasPotential(lhs, rhs, lhsState, rhsState)) {
                    pairs.add(Pair.create(lhsState, rhsState));
                }
            }
        }

        return pairs;
    }

    /**
     * Indicates whether {@code lhsState} and {@code rhsState} have potential to be matched to one another.
     *
     * <p>
     * Any (LHS, RHS)-state pair is defined to have <i>potential</i> if (1) they have combinable state properties, and
     * (2) have incoming or outgoing transition properties that are combinable. State pairs without potential are not to
     * be matched to one another, since their matchings would be invalid or would lead to tangles.
     * </p>
     *
     * @param lhs The left-hand-side (LHS) GLTS.
     * @param rhs The right-hand-side (RHS) GLTS.
     * @param lhsState The input LHS state.
     * @param rhsState The input RHS state.
     * @return {@code true} if the state pair ({@code lhsState}, {@code rhsState}) has potential according to the above
     *     definition, {@code false} otherwise.
     */
    private boolean hasPotential(U lhs, U rhs, State<S> lhsState, State<S> rhsState) {
        if (!statePropertyCombiner.areCombinable(lhsState.getProperty(), rhsState.getProperty())) {
            return false;
        }

        Pair<State<S>, State<S>> statePair = Pair.create(lhsState, rhsState);

        return GLTSUtils.hasCommonIncomingTransitions(lhs, rhs, transitionPropertyCombiner, statePair)
                || GLTSUtils.hasCommonOutgoingTransitions(lhs, rhs, transitionPropertyCombiner, statePair);
    }

    /**
     * Determines the best possible matching out of a set {@code candidateMatches} of possible candidate matchings.
     *
     * @param lhs The left-hand-side (LHS) GLTS.
     * @param rhs The right-hand-side (RHS) GLTS.
     * @param candidateMatches A set of all state pairs to consider as potential matches.
     * @return The best possible maximal matching out of all the potential matches in {@code candidateMatches}.
     */
    private Set<Pair<State<S>, State<S>>> findAnOptimalMatching(U lhs, U rhs,
            Set<Pair<State<S>, State<S>>> candidateMatches)
    {
        return findAnOptimalMatching(lhs, rhs, new LinkedHashSet<>(), candidateMatches).getSecond();
    }

    /**
     * Determines the best possible matching out of a set {@code candidateMatches} of possible candidate matchings, that
     * contains at least all the matchings in {@code fixedMatches} (which are thus fixed).
     *
     * <p>
     * Note that, since this method is recursive, stack depth issues may occur for larger input GLTSs. The exact limits
     * depend on the Java Virtual Machine that is used, and its settings.
     * </p>
     *
     * @param lhs The left-hand-side (LHS) GLTS.
     * @param rhs The right-hand-side (RHS) GLTS.
     * @param fixedMatches The set of matches that are already fixed.
     * @param candidateMatches A set of all state pairs to still consider as potential matches.
     * @return A pair consisting of the best possible maximal matching out of all the potential matches in
     *     {@code candidateMatches} that contains at least {@code fixedMatches}, together with the number of combined
     *     transitions that the merge of the LHS and RHS would have as result of using this matching.
     */
    private Pair<Integer, Set<Pair<State<S>, State<S>>>> findAnOptimalMatching(U lhs, U rhs,
            Set<Pair<State<S>, State<S>>> fixedMatches, Set<Pair<State<S>, State<S>>> candidateMatches)
    {
        // Remove all impossible candidate matchings.
        candidateMatches = Sets.difference(candidateMatches, getConflictingPairs(fixedMatches, candidateMatches));

        // Fixate all matchings that are forced.
        Set<Pair<State<S>, State<S>>> forcedMatches = getForcedMatches(lhs, rhs, candidateMatches);
        candidateMatches = Sets.difference(candidateMatches, forcedMatches);
        fixedMatches = Sets.union(fixedMatches, forcedMatches);

        // If there are no more candidate matchings left to consider, then return the current matching.
        if (candidateMatches.isEmpty()) {
            return Pair.create(optimizationObjective(lhs, rhs, fixedMatches), fixedMatches);
        }

        // Otherwise, recursively explore all possible matchings that can be obtained from 'candidateMatches', to search
        // for the best such matching. We do this exploration by (1) first picking a RHS state that has candidate
        // matches, which will be used as a starting point, (2) try out all candidate matchings that involve the chosen
        // RHS state one by one, by fixating them and recursively exploring all left-over candidates, and (3) consider
        // the case in which the RHS state is not matched at all, by recursively exploring all candidates that do not
        // involve the RHS state.

        // Note that, for (1) we could in principle pick any RHS state at random. However, for performance reasons we
        // pick the RHS state that has the fewest options out of all the candidate matches. Selecting the most
        // restricted state reduces the amount of branching and thereby the (recursive) search space.

        // From 'candidateMatches' construct a map from RHS states to the set of all its candidate LHS state matches.
        Multimap<State<S>, State<S>> candidateMap = projectToRight(candidateMatches);

        // Out of 'candidateMap' find the most restricted RHS state: the one with the fewest matching options.
        State<S> rhsState = candidateMap.asMap().entrySet().stream()
                .min(Comparator.comparingInt(e -> e.getValue().size())).get().getKey();

        // Try all candidate matchings for 'rhsState', and keep track of the best matching found.
        Pair<Integer, Set<Pair<State<S>, State<S>>>> currentBestMatching = Pair.create(-1, ImmutableSet.of());

        for (State<S> lhsState: candidateMap.get(rhsState)) {
            // Recursively find the best matching containing (at least) 'fixedMatches' and '(lhsState, rhsState)'.
            Set<Pair<State<S>, State<S>>> candidateSingleton = ImmutableSet.of(Pair.create(lhsState, rhsState));
            Set<Pair<State<S>, State<S>>> newFixedMatches = Sets.union(fixedMatches, candidateSingleton);
            Set<Pair<State<S>, State<S>>> newCandidateMatches = Sets.difference(candidateMatches, candidateSingleton);
            Pair<Integer, Set<Pair<State<S>, State<S>>>> result = findAnOptimalMatching(lhs, rhs, newFixedMatches,
                    newCandidateMatches);

            // If the matching found is better than the current best matching, then keep track of it.
            if (currentBestMatching.getFirst() < result.getFirst()) {
                currentBestMatching = result;
            }
        }

        // Finally we should consider the case in which 'rhsState' is not matched at all.
        Set<Pair<State<S>, State<S>>> candidatesToDiscard = candidateMap.get(rhsState).stream()
                .map(lhsState -> Pair.create(lhsState, rhsState)).collect(Collectors.toCollection(LinkedHashSet::new));
        Set<Pair<State<S>, State<S>>> newCandidateMatches = Sets.difference(candidateMatches, candidatesToDiscard);
        Pair<Integer, Set<Pair<State<S>, State<S>>>> result = findAnOptimalMatching(lhs, rhs, fixedMatches,
                newCandidateMatches);

        if (currentBestMatching.getFirst() < result.getFirst()) {
            currentBestMatching = result;
        }

        return currentBestMatching;
    }

    /**
     * Given a set {@code fixed} of fixed matches and a set {@code candidates} of candidate matches, determines the set
     * of candidate matches that are impossible by the specified choice of fixed matches.
     *
     * <p>
     * The set returned by this function is a subset of {@code candidates}. Moreover, every pair in the returned set
     * contains a LHS state or RHS state that occurs in {@code fixed}. This is linear-time O(|{@code fixed}| +
     * |{@code candidates}|) operation.
     * </p>
     *
     * @param fixed The set of fixes matches, i.e., state pairs that have been chosen to match on one another.
     * @param candidates The set of candidate matches, i.e., the choices that are still available.
     * @return A subset of {@code candidates} containing all states that cannot be added to {@code fixed}.
     */
    private Set<Pair<State<S>, State<S>>> getConflictingPairs(Set<Pair<State<S>, State<S>>> fixed,
            Set<Pair<State<S>, State<S>>> candidates)
    {
        Pair<Set<State<S>>, Set<State<S>>> fixedStates = unzip(fixed);
        Set<State<S>> lhsStates = fixedStates.getFirst();
        Set<State<S>> rhsStates = fixedStates.getSecond();

        Preconditions.checkArgument(lhsStates.size() == fixed.size(), "Detected duplicate LHS states.");
        Preconditions.checkArgument(rhsStates.size() == fixed.size(), "Detected duplicate RHS states.");

        return candidates.stream()
                .filter(pair -> lhsStates.contains(pair.getFirst()) || rhsStates.contains(pair.getSecond()))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * Given a set {@code candidates} of candidate (LHS, RHS)-state matches, determines the set of all candidate matches
     * that are forced. Any candidate match is <i>forced</i> if both its LHS state and RHS state do not occur elsewhere
     * in {@code candidates}.
     *
     * <p>
     * This function returns a subset of {@code candidates}. Every pair in the returned set contains a LHS and RHS state
     * that both occur only once in {@code candidates}. This is a linear-time O(|{@code candidates}|) algorithm.
     * </p>
     *
     * @param lhs The left-hand-side (LHS) GLTS.
     * @param rhs The right-hand-side (RHS) GLTS.
     * @param candidates The input set of candidates.
     * @return A subset of {@code candidates} consisting of all forced matchings according to the above definition.
     */
    private Set<Pair<State<S>, State<S>>> getForcedMatches(U lhs, U rhs, Set<Pair<State<S>, State<S>>> candidates) {
        // For every LHS and RHS state, count how often they occur in 'candidates'.
        Map<State<S>, Integer> lhsStateCounts = new LinkedHashMap<>(lhs.size());
        Map<State<S>, Integer> rhsStateCounts = new LinkedHashMap<>(rhs.size());

        for (Pair<State<S>, State<S>> pair: candidates) {
            State<S> lhsState = pair.getFirst();
            State<S> rhsState = pair.getSecond();
            lhsStateCounts.put(lhsState, lhsStateCounts.getOrDefault(lhsState, 0) + 1);
            rhsStateCounts.put(rhsState, rhsStateCounts.getOrDefault(rhsState, 0) + 1);
        }

        // Return the set of all pairs with a LHS and RHS state that both occur only once in 'candidates'.
        return candidates.stream()
                .filter(pair -> lhsStateCounts.get(pair.getFirst()) == 1 && rhsStateCounts.get(pair.getSecond()) == 1)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * The objective function that is to be maximized by the brute force matcher. This function determines the number of
     * transitions that would be combined in the final merged GLTS of {@code lhs} and {@code rhs}, in which all (LHS,
     * RHS)-state pairs in {@code fixed} were matched and merged.
     *
     * <p>
     * The time complexity of this operation is O(|V1|*|V2|*|{@code fixed}|), with |V1| the number of states in
     * {@code lhs} and |V2| the number of states in {@code rhs}.
     * </p>
     *
     * <p>
     * The function may be adjusted by {@link #getOptimizationObjectiveAdjustment}.
     * </p>
     *
     * @param lhs The left-hand-side (LHS) GLTS.
     * @param rhs The right-hand-side (RHS) GLTS.
     * @param fixed The input set of fixed (LHS, RHS)-state matchings.
     * @return The number of transitions in the LHS and RHS that would collapse into a combined transition if all state
     *     pairs in {@code fixed} were matched and merged, with adjustments applied.
     */
    private int optimizationObjective(U lhs, U rhs, Set<Pair<State<S>, State<S>>> fixed) {
        int count = 0;

        for (Pair<State<S>, State<S>> pair: fixed) {
            // Account for combinable transitions.
            count += GLTSUtils.commonOutgoingTransitions(lhs, rhs, transitionPropertyCombiner, pair)
                    .map(t -> Pair.create(t.getFirst().getTarget(), t.getSecond().getTarget())).filter(fixed::contains)
                    .count();

            // Apply configurable adjustment.
            count += getOptimizationObjectiveAdjustment(lhs, rhs, pair.getFirst(), pair.getSecond());
        }

        return count;
    }

    /**
     * Gives an adjustment to the {@link #optimizationObjective objective function} for the given state pair.
     *
     * @param lhs The left-hand-side (LHS) GLTS.
     * @param rhs The right-hand-side (RHS) GLTS.
     * @param leftState A LHS state.
     * @param rightState A RHS state.
     * @return An adjustment to the objective function for the given state pair.
     */
    protected int getOptimizationObjectiveAdjustment(U lhs, U rhs, State<S> leftState, State<S> rightState) {
        return 0;
    }

    /**
     * Given a {@code set} of pairs, constructs a map from right elements (of the pairs) to the sets of all left
     * elements they are paired to.
     *
     * @param <L> The type of left elements of the pair.
     * @param <R> The type of right elements of the pair.
     * @param set The input set to construct the map for.
     * @return A mapping from right elements of pairs in {@code set} to all left elements they are paired with.
     */
    private static <L, R> Multimap<R, L> projectToRight(Set<Pair<L, R>> set) {
        Multimap<R, L> rightMap = LinkedHashMultimap.create();
        set.forEach(pair -> rightMap.put(pair.getSecond(), pair.getFirst()));
        return rightMap;
    }

    /**
     * Unzips the given {@code set} of pairs into a pair of sets containing all its left and right elements.
     *
     * @param <L> The type of left elements of the pair.
     * @param <R> The type of right elements of the pair.
     * @param set The input set of pairs to unzip.
     * @return The unzipped pair of sets.
     */
    private static <L, R> Pair<Set<L>, Set<R>> unzip(Set<Pair<L, R>> set) {
        Set<L> left = new LinkedHashSet<>(set.size());
        Set<R> right = new LinkedHashSet<>(set.size());

        for (Pair<L, R> entry: set) {
            left.add(entry.getFirst());
            right.add(entry.getSecond());
        }

        return Pair.create(left, right);
    }
}
