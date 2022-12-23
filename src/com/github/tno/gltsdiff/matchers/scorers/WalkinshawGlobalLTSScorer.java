
package com.github.tno.gltsdiff.matchers.scorers;

import com.github.tno.gltsdiff.glts.LTS;
import com.github.tno.gltsdiff.glts.State;
import com.github.tno.gltsdiff.operators.combiners.Combiner;

/**
 * Contains functionality for computing global similarity scores for pairs of (LHS, RHS)-states in {@link LTS LTSs},
 * thereby taking initial state information into account.
 *
 * @param <S> The type of state properties.
 * @param <T> The type of transition properties.
 * @param <U> The type of LTSs.
 */
public class WalkinshawGlobalLTSScorer<S, T, U extends LTS<S, T>> extends WalkinshawGlobalGLTSScorer<S, T, U> {
    /**
     * Instantiates a new Walkinshaw global scorer for LTSs.
     * 
     * @param lhs The left-hand-side LTS, which has at least one state.
     * @param rhs The right-hand-side LTS, which has at least one state.
     * @param statePropertyCombiner The combiner for state properties.
     * @param transitionPropertyCombiner The combiner for transition properties.
     */
    public WalkinshawGlobalLTSScorer(U lhs, U rhs, Combiner<S> statePropertyCombiner,
            Combiner<T> transitionPropertyCombiner)
    {
        super(lhs, rhs, statePropertyCombiner, transitionPropertyCombiner);
    }

    @Override
    protected double getNumeratorAdjustment(State<S> leftState, State<S> rightState, boolean isForward) {
        return getDefaultNumeratorAdjustmentForLTSs(lhs, rhs, leftState, rightState, isForward);
    }

    @Override
    protected double getDenominatorAdjustment(State<S> leftState, State<S> rightState, boolean isForward) {
        return getDefaultDenominatorAdjustmentForLTSs(lhs, rhs, leftState, rightState, isForward);
    }
}
