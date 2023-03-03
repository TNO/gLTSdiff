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
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.util.Pair;

import com.github.tno.gltsdiff.glts.GLTS;
import com.github.tno.gltsdiff.glts.State;
import com.github.tno.gltsdiff.matchers.scorers.SimilarityScorer;
import com.github.tno.gltsdiff.operators.combiners.Combiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashBiMap;

/**
 * Matcher that based on similarity scores computes a guaranteed maximal matching, in the sense that there does not
 * exist a (LHS, RHS)-state matching other than the one computed that has a higher summed-up score.
 *
 * <p>
 * Implements the Kuhn-Munkres algorithm, that is also known as the Hungarian algorithm. Its computational complexity is
 * about O((|LHS| + |RHS|)^3) with |LHS| and |RHS| the number of states on the left-hand-side and right-hand-side,
 * respectively. However, the runtime may be cheaper in case the input GLTSs are sparse.
 * </p>
 *
 * <p>
 * If one experiences performance problems with {@link KuhnMunkresMatcher}, for example because the input GLTSs are
 * large or dense, consider switching to a more lightweight matcher like for example {@link WalkinshawGLTSMatcher}.
 * </p>
 *
 * @param <S> The type of state properties.
 * @param <T> The type of transition properties.
 * @param <U> The type of GLTSs.
 */
public class KuhnMunkresMatcher<S, T, U extends GLTS<S, T>> extends ScoringMatcher<S, T, U> {
    /** The combiner for state properties. */
    private final Combiner<S> statePropertyCombiner;

    /**
     * Constructs a Kuhn-Munkres state matcher.
     *
     * @param scorer The algorithm for computing state similarity scores.
     * @param statePropertyCombiner The combiner for state properties.
     */
    public KuhnMunkresMatcher(SimilarityScorer<S, T, U> scorer, Combiner<S> statePropertyCombiner) {
        super(scorer);
        this.statePropertyCombiner = statePropertyCombiner;
    }

    @Override
    protected Map<State<S>, State<S>> computeInternal(U lhs, U rhs, RealMatrix scores) {
        // Declare the matrix that will be given as input to the Kuhn-Munkres algorithm.
        // The algorithm only works on square matrices. So the matrix needs to be suitably big.
        int matrixSize = Math.max(lhs.size(), rhs.size());
        RealMatrix matrix = new Array2DRowRealMatrix(matrixSize, matrixSize).scalarAdd(1.0d);

        // Fill-in the score assignments into the Kuhn-Munkres matrix.
        // Note that Kuhn-Munkres searches for minimal matchings, whereas we are interested in maximal matchings.
        // Therefore, we transform the problem of finding a maximal matching to finding a minimal one,
        // by inverting all scores before we insert them into the matrix.
        RealMatrix invertedScores = invert(scores);

        for (State<S> leftState: lhs.getStates()) {
            for (State<S> rightState: rhs.getStates()) {
                int leftStateId = leftState.getId();
                int rightStateId = rightState.getId();

                if (statePropertyCombiner.areCombinable(leftState.getProperty(), rightState.getProperty())) {
                    matrix.setEntry(leftStateId, rightStateId, invertedScores.getEntry(leftStateId, rightStateId));
                } else {
                    matrix.setEntry(leftStateId, rightStateId, Double.POSITIVE_INFINITY);
                }
            }
        }

        // Define a function for obtaining the (original) similarity score of any pair of (LHS, RHS)-states.
        BiFunction<State<S>, State<S>, Double> getScore = (l, r) -> scores.getEntry(l.getId(), r.getId());

        // Check whether a solution already exists before executing the algorithm.
        Map<State<S>, State<S>> matching = convert(lhs, rhs, constructMatching(matrix), getScore);
        if (isComplete(lhs, rhs, matching, getScore)) {
            return truncate(matching, getScore);
        }

        // Otherwise perform the steps of the Kuhn-Munkres algorithm.

        // Note that the Kuhn-Munkres matcher will only match state pairs that have a finite score. This is because each
        // step of the algorithm preserves the finiteness of the cells of 'matrix': all cells that are within the range
        // [0,1] will stay within that range, and all positive infinite values will stay positive infinite. Furthermore,
        // 'constructMatching' will only consider state pairs corresponding to cells in 'matrix' with a value of 0.

        // Perform the first step of Kuhn-Munkres: the row operations.
        step1(matrix);

        // Check whether the first step revealed a minimal matching.
        matching = convert(lhs, rhs, constructMatching(matrix), getScore);
        if (isComplete(lhs, rhs, matching, getScore)) {
            return truncate(matching, getScore);
        }

        // If not, perform the second step of Kuhn-Munkres: the column operations.
        step2(matrix);

        // Check whether the second step revealed a minimal matching.
        matching = convert(lhs, rhs, constructMatching(matrix), getScore);
        if (isComplete(lhs, rhs, matching, getScore)) {
            return truncate(matching, getScore);
        }

        while (true) {
            // If not, perform step 3 and 4 of the algorithm.
            Pair<Set<Integer>, Set<Integer>> lines = step3(lhs, rhs, matrix);
            step4(matrix, lines.getFirst(), lines.getSecond());

            // Check whether the 3rd and 4th step revealed a minimal matching, which is guaranteed to happen eventually.
            matching = convert(lhs, rhs, constructMatching(matrix), getScore);
            if (isComplete(lhs, rhs, matching, getScore)) {
                break;
            }
        }

        return truncate(matching, getScore);
    }

    /**
     * Computes the inversion of {@code scores}, i.e. change every element {@code e} to {@code 1 - e}.
     *
     * @param scores The scores that are to be inverted.
     * @return The inverted scores.
     */
    private RealMatrix invert(RealMatrix scores) {
        RealMatrix invertedScores = new Array2DRowRealMatrix(scores.getRowDimension(), scores.getColumnDimension());
        for (int row = 0; row < scores.getRowDimension(); row++) {
            for (int column = 0; column < scores.getColumnDimension(); column++) {
                invertedScores.setEntry(row, column, 1.0d - scores.getEntry(row, column));
            }
        }
        return invertedScores;
    }

    /**
     * Constructs a maximum matching for the given matrix by only considering its {@code 0.0d} entries. Here
     * <i>maximum</i> means that the returned matching contains as many edges as possible. That is, it is not possible
     * to construct a matching with more edges than the one returned.
     *
     * <p>
     * This is an implementation of Kuhn's algorithm for finding maximum bipartite matchings. Details about this
     * algorithm can be found at https://cp-algorithms.com/graph/kuhn_maximum_bipartite_matching.html.
     * </p>
     *
     * <p>
     * Note that more efficient algorithms exist for finding maximum matchings, like for example the
     * Hopcroft-Karp-Karzanov algorithm. Furthermore, the web page mentioned above lists some heuristics for improving
     * the performance of the current implementation. These improvements have not yet been applied.
     * </p>
     *
     * @param matrix The assignment matrix for which a matching is to be constructed.
     * @return A matching for the given matrix, so that every assignment corresponds to a {@code 0.0d} matrix entry.
     */
    private Map<Integer, Integer> constructMatching(RealMatrix matrix) {
        Preconditions.checkArgument(matrix.getRowDimension() == matrix.getColumnDimension());

        Map<Integer, List<Integer>> candidateMatches = new LinkedHashMap<>();

        // Initializing the map of candidate matches to be empty for every row.
        for (int row = 0; row < matrix.getRowDimension(); row++) {
            candidateMatches.put(row, new ArrayList<>());
        }

        // For every row, find all potentially matching columns: the ones with a current score of 0.0d.
        for (int row = 0; row < matrix.getRowDimension(); row++) {
            for (int column = 0; column < matrix.getColumnDimension(); column++) {
                if (matrix.getEntry(row, column) == 0.0d) {
                    candidateMatches.get(row).add(column);
                }
            }
        }

        // Find a maximum matching by using Kuhn's algorithm for finding maximum bipartite matchings.
        // Kuhn's algorithm requires 'currentMatching' to map from RHS states to LHS states. It must be inverted later.
        Map<Integer, Integer> currentMatching = new LinkedHashMap<>(matrix.getRowDimension());

        for (int row = 0; row < matrix.getRowDimension(); row++) {
            Set<Integer> usedStates = new LinkedHashSet<>(matrix.getRowDimension());
            alternateMatchingAlongAugmentingPath(candidateMatches, currentMatching, usedStates, row);
        }

        // Invert the maximum matching found, to get a map from LHS to RHS states.
        return HashBiMap.create(currentMatching).inverse();
    }

    /**
     * This is an implementation of the 'try_kuhn' function described in
     * https://cp-algorithms.com/graph/kuhn_maximum_bipartite_matching.html.
     *
     * <p>
     * This function searches for any augmenting paths starting from a given state {@code v}, and after having found
     * one, updates the current matching {@code currentMatching} by alternating it along the augmenting path found. A
     * definition of augmenting paths as well as details on the algorithm can be found on the web page mentioned above.
     * </p>
     *
     * @param candidateMatches A map from LHS states to the collection of RHS-states that are potential matches.
     * @param currentMatching The current intermediate matching that is updated while searching for augmenting paths.
     *     Note that this matching is a map from RHS states to LHS states (instead of the other way around).
     * @param usedStates The set of states that are already traversed during the search for an augmenting path starting
     *     from {@code v}.
     * @param v The state from which to start searching for an augmenting path.
     * @return {@code true} if there exists an augmenting path starting from {@code v}, {@code false} otherwise.
     */
    private boolean alternateMatchingAlongAugmentingPath(Map<Integer, List<Integer>> candidateMatches,
            Map<Integer, Integer> currentMatching, Set<Integer> usedStates, int v)
    {
        Preconditions.checkArgument(candidateMatches.containsKey(v));

        if (usedStates.contains(v)) {
            return false;
        }

        usedStates.add(v);

        for (int candidate: candidateMatches.get(v)) {
            Integer w = currentMatching.get(candidate);

            if (w == null || alternateMatchingAlongAugmentingPath(candidateMatches, currentMatching, usedStates, w)) {
                currentMatching.put(candidate, v);
                return true;
            }
        }

        return false;
    }

    /**
     * Converts a given matching that is constructed for the Kuhn-Munkres matrix, to a matching on (LHS, RHS)-states.
     *
     * @param lhs The left-hand-side (LHS) GLTS.
     * @param rhs The right-hand-side (RHS) GLTS.
     * @param matching The (LHS, RHS)-state matching to convert, where all states are represented by their identifiers.
     *     All matched pairs of states must have combinable state properties, as well as a finite similarity score with
     *     respect to {@code scores}.
     * @param scores The similarity scoring function for (LHS, RHS)-state pairs. All state similarity scores must either
     *     be within the range [0,1] or be {@link Double#POSITIVE_INFINITY}.
     * @return The converted (LHS, RHS)-state matching.
     */
    private Map<State<S>, State<S>> convert(U lhs, U rhs, Map<Integer, Integer> matching,
            BiFunction<State<S>, State<S>, Double> scores)
    {
        Map<State<S>, State<S>> stateMatching = new LinkedHashMap<>();

        for (Entry<Integer, Integer> entry: matching.entrySet()) {
            int leftIndex = entry.getKey();
            int rightIndex = entry.getValue();

            if (0 <= leftIndex && leftIndex < lhs.size() && 0 <= rightIndex && rightIndex < rhs.size()) {
                State<S> leftState = lhs.getStateById(leftIndex);
                State<S> rightState = rhs.getStateById(rightIndex);

                Preconditions.checkArgument(
                        statePropertyCombiner.areCombinable(leftState.getProperty(), rightState.getProperty()),
                        "Expected matched states to have combinable state properties.");
                Preconditions.checkArgument(Double.isFinite(scores.apply(leftState, rightState)),
                        "Expected matched states to have finite scores.");

                stateMatching.put(leftState, rightState);
            }
        }

        return stateMatching;
    }

    /**
     * Determines whether the given matching is a complete (LHS, RHS)-matching, in the sense that it cannot be extended.
     *
     * @param lhs The left-hand-side (LHS) GLTS.
     * @param rhs The right-hand-side (RHS) GLTS.
     * @param matching The (LHS, RHS)-state matching to check.
     * @param scores The similarity scoring function for (LHS, RHS)-state pairs. All state similarity scores must either
     *     be within the range [0,1] or be {@link Double#POSITIVE_INFINITY}.
     * @return {@code true} if {@code matching} is complete and cannot be extended further, {@code false} otherwise.
     */
    private boolean isComplete(U lhs, U rhs, Map<State<S>, State<S>> matching,
            BiFunction<State<S>, State<S>, Double> scores)
    {
        Set<State<S>> leftStates = new LinkedHashSet<>(lhs.getStates());
        Set<State<S>> rightStates = new LinkedHashSet<>(rhs.getStates());

        for (Entry<State<S>, State<S>> assignment: matching.entrySet()) {
            leftStates.remove(assignment.getKey());
            rightStates.remove(assignment.getValue());
        }

        // The given matching is complete if all leftover unmatched LHS and RHS state pairs are incompatible, i.e., have
        // uncombinable properties and/or have a similarity score that is not finite.
        return leftStates.stream().allMatch(leftState -> rightStates.stream()
                .noneMatch(rightState -> Double.isFinite(scores.apply(leftState, rightState))
                        && statePropertyCombiner.areCombinable(leftState.getProperty(), rightState.getProperty())));
    }

    /**
     * Filters-out all assignments from the given matching that have a score lower than {@code 0.1d} according to the
     * given similarity scoring function.
     *
     * @param matching The (LHS, RHS)-state matching that is to be filtered. All matched pairs of states must have
     *     combinable state properties, as well as a finite similarity score with respect to {@code scores}.
     * @param scores The similarity scoring function for (LHS, RHS)-state pairs that forms the basis for filtering. All
     *     state similarity scores must either be within the range [0,1] or be {@link Double#POSITIVE_INFINITY}.
     * @return A subset of matchings, containing only the assignments with a similarity score at least {@code 0.1d}.
     */
    private Map<State<S>, State<S>> truncate(Map<State<S>, State<S>> matching,
            BiFunction<State<S>, State<S>, Double> scores)
    {
        Map<State<S>, State<S>> reducedMatching = new LinkedHashMap<>();

        for (Entry<State<S>, State<S>> entry: matching.entrySet()) {
            State<S> leftState = entry.getKey();
            State<S> rightState = entry.getValue();

            double score = scores.apply(leftState, rightState);
            Preconditions.checkArgument(Double.isFinite(score), "Expected matched states to have finite scores.");

            if (0.1d <= score) {
                reducedMatching.put(leftState, rightState);
            }
        }

        return reducedMatching;
    }

    /**
     * Performs the first step of the Kuhn-Munkres algorithm: for each row in the given matrix, find the smallest entry
     * in that row, and if that entry is finite, subtract it from every entry in the row. This step ensures that all
     * entries of {@code matrix} within the range [0,1] stay within that range, and that all
     * {@link Double#POSITIVE_INFINITY} entries are preserved.
     *
     * @param matrix The matrix of score assignments, whose entries are either within the range [0,1] or are
     *     {@link Double#POSITIVE_INFINITY}.
     */
    private void step1(RealMatrix matrix) {
        // Iterate over every row.
        for (int row = 0; row < matrix.getRowDimension(); row++) {
            // Find the smallest value in the current row.
            double minValue = matrix.getEntry(row, 0);

            for (int column = 1; column < matrix.getColumnDimension(); column++) {
                minValue = Math.min(minValue, matrix.getEntry(row, column));
            }

            // Subtract 'minValue' from each entry in the current row. But only if this value is finite.
            // Otherwise the LHS state corresponding to 'row' is not combinable with any RHS state, and thus it would
            // not make sense for the algorithm to try to get closer to an optimal matching for that state.
            if (Double.isFinite(minValue)) {
                for (int column = 0; column < matrix.getColumnDimension(); column++) {
                    matrix.addToEntry(row, column, -minValue);
                }
            }
        }
    }

    /**
     * Performs the second step of the Kuhn-Munkres algorithm: for each column in the given matrix, find the smallest
     * finite entry in that column, and subtract it from every entry in the column. This step ensures that all entries
     * of {@code matrix} within the range [0,1] stay within that range, and that all {@link Double#POSITIVE_INFINITY}
     * entries are preserved.
     *
     * @param matrix The matrix of score assignments, whose entries are either within the range [0,1] or are
     *     {@link Double#POSITIVE_INFINITY}.
     */
    private void step2(RealMatrix matrix) {
        // Iterate over every column.
        for (int column = 0; column < matrix.getColumnDimension(); column++) {
            // Find the smallest value in the current column.
            double minValue = matrix.getEntry(0, column);

            for (int row = 1; row < matrix.getRowDimension(); row++) {
                minValue = Math.min(minValue, matrix.getEntry(row, column));
            }

            // Subtract 'minValue' from each entry in the current column. But only if this value is finite.
            // Otherwise the RHS state corresponding to 'column' is not combinable with any LHS state, and thus it would
            // not make sense for the algorithm to try to get closer to an optimal matching for that state.
            if (Double.isFinite(minValue)) {
                for (int row = 0; row < matrix.getRowDimension(); row++) {
                    matrix.addToEntry(row, column, -minValue);
                }
            }
        }
    }

    /**
     * Finds a minimal number of horizontal and vertical lines over the given matrix that together cover all the entries
     * in the matrix that are {@code 0.0d}. This operation does not modify {@code matrix}.
     *
     * @param lhs The left-hand-side (LHS) GLTS.
     * @param rhs The right-hand-side (RHS) GLTS.
     * @param matrix The matrix of score assignments, whose entries are either within the range [0,1] or are
     *     {@link Double#POSITIVE_INFINITY}.
     * @return The horizontal (row) and vertical (column) lines, that each come as a set of integers representing the
     *     row/column index. So the returned structure is a pair of sets of integers, where the first set contains the
     *     row lines and the second set contains the column lines.
     */
    private Pair<Set<Integer>, Set<Integer>> step3(U lhs, U rhs, RealMatrix matrix) {
        // Match as many (LHS, RHS)-states as possible.
        Map<Integer, Integer> matching = constructMatching(matrix);

        // Set-up the data structures for maintaining the row and column markings.
        Set<Integer> markedRows = new LinkedHashSet<>();
        Set<Integer> markedColumns = new LinkedHashSet<>();
        Set<Integer> newlyMarkedColumns = new LinkedHashSet<>();

        // Mark all rows that are not matched.
        Set<Integer> newlyMarkedRows = lhs.getStates().stream().map(State::getId).filter(s -> !matching.containsKey(s))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        markedRows.addAll(newlyMarkedRows);

        // Iteratively find new markings until no new markings arise (which must eventually be the case).
        while (true) {
            newlyMarkedColumns.clear();

            // Mark all columns that have 0.0d in every newly marked row.
            for (int column = 0; column < matrix.getColumnDimension(); column++) {
                for (int row: newlyMarkedRows) {
                    if (matrix.getEntry(row, column) == 0.0d && !markedColumns.contains(column)) {
                        newlyMarkedColumns.add(column);
                    }
                }
            }

            // Terminate if no new column markings have been found.
            if (newlyMarkedColumns.isEmpty()) {
                break;
            }

            markedColumns.addAll(newlyMarkedColumns);
            newlyMarkedRows.clear();

            // Mark all rows that are matched to a newly marked column.
            for (int row = 0; row < matrix.getRowDimension(); row++) {
                Integer rowMatch = matching.get(row);

                if (rowMatch == null) {
                    continue;
                }

                for (int column: newlyMarkedColumns) {
                    if (rowMatch == column && !markedRows.contains(row)) {
                        newlyMarkedRows.add(row);
                    }
                }
            }

            // Terminate if no new row markings have been found.
            if (newlyMarkedRows.isEmpty()) {
                break;
            }

            markedRows.addAll(newlyMarkedRows);
        }

        // Now we draw a line through every marked column and UN-marked row, and return both these sets of lines.
        // For the row lines, we thus need to iterate through all rows to find the ones that are not marked.
        Set<Integer> unmarkedRows = new LinkedHashSet<>();

        for (int row = 0; row < matrix.getRowDimension(); row++) {
            if (!markedRows.contains(row)) {
                unmarkedRows.add(row);
            }
        }

        return Pair.create(unmarkedRows, markedColumns);
    }

    /**
     * From the given matrix and sets of horizontal and vertical lines over the matrix:
     * <ul>
     * <li>Finds the lowest value in the matrix that is not covered by any line,</li>
     * <li>Subtracts this value from every entry that is not covered by a line, and</li>
     * <li>Adds it to all entries that are covered by two lines.</li>
     * </ul>
     * This step ensures that all entries of {@code matrix} within the range [0,1] stay within that range, and that all
     * {@link Double#POSITIVE_INFINITY} entries are preserved.
     *
     * @param matrix The matrix of score assignments, whose entries are either within the range [0,1] or are
     *     {@link Double#POSITIVE_INFINITY}.
     * @param horizontalLines A set of horizontal lines: the elements of this set are row indices in the matrix.
     * @param verticalLines A set of vertical lines: the elements of this set are column indices in the matrix.
     */
    @SuppressWarnings("null")
    private void step4(RealMatrix matrix, Set<Integer> horizontalLines, Set<Integer> verticalLines) {
        Double minValue = null;

        // Among all entries that are not covered by a line, find the smallest value.
        for (int row = 0; row < matrix.getRowDimension(); row++) {
            for (int column = 0; column < matrix.getColumnDimension(); column++) {
                if (!horizontalLines.contains(row) && !verticalLines.contains(column)) {
                    double currentValue = matrix.getEntry(row, column);

                    if (minValue == null) {
                        minValue = currentValue;
                    }

                    if (currentValue < minValue) {
                        minValue = currentValue;
                    }
                }
            }
        }

        // Note that 'minValue' cannot be null at this point if 'horizontalLines' and 'verticalLines' are together the
        // minimal number of lines needed to cover all zeroes. That should be the case, since otherwise all entries are
        // covered by a line, in which case a matching would have already been found before calling 'step4'.

        // Moreover, 'minValue' must be finite (and therefore be within the range [0,1]) since otherwise all values that
        // are not covered by a line are infinite, meaning that a best possible matching must already have been found
        // before calling 'step4', since all values that are not covered by a line correspond to matchings of
        // uncombinable states.
        Preconditions.checkArgument(Double.isFinite(minValue), "Expected the lowest value to be finite.");

        // Subtract the lowest value from all entries not covered by a line.
        for (int row = 0; row < matrix.getRowDimension(); row++) {
            for (int column = 0; column < matrix.getColumnDimension(); column++) {
                if (!horizontalLines.contains(row) && !verticalLines.contains(column)) {
                    matrix.addToEntry(row, column, -minValue);
                }
            }
        }

        // Add the lowest value to every entry that is covered by two lines.
        for (int row: horizontalLines) {
            for (int column: verticalLines) {
                matrix.addToEntry(row, column, minValue);
            }
        }
    }
}
