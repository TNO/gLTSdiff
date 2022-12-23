
package com.github.tno.gltsdiff.matchers.scorers;

import java.util.function.BiFunction;

import com.github.tno.gltsdiff.glts.LTS;
import com.github.tno.gltsdiff.operators.combiners.Combiner;

/**
 * Scorer that computes state similarity scores for {@link LTS LTSs} that makes a trade-off between computational
 * intensity and the quality of the computed scores.
 *
 * @param <S> The type of state properties.
 * @param <T> The type of transition properties.
 * @param <U> The type of LTSs.
 */
public class DynamicLTSScorer<S, T, U extends LTS<S, T>> extends DynamicGLTSScorer<S, T, U> {
    /**
     * Instantiates a new dynamic scoring algorithm for GLTSs, that uses a default configuration of scoring algorithms.
     * 
     * @param lhs The left-hand-side LTS, which has at least one state.
     * @param rhs The right-hand-side LTS, which has at least one state.
     * @param statePropertyCombiner The combiner for state properties.
     * @param transitionPropertyCombiner The combiner for transition properties.
     */
    public DynamicLTSScorer(U lhs, U rhs, Combiner<S> statePropertyCombiner, Combiner<T> transitionPropertyCombiner) {
        this(lhs, rhs, statePropertyCombiner, transitionPropertyCombiner,
                (l, r) -> (s, t) -> defaultScoringAlgorithmCreator(l, r, s, t));
    }

    /**
     * Instantiates a new dynamic scoring algorithm for LTSs.
     * 
     * @param lhs The left-hand-side LTS, which has at least one state.
     * @param rhs The right-hand-side LTS, which has at least one state.
     * @param statePropertyCombiner The combiner for state properties.
     * @param transitionPropertyCombiner The combiner for transition properties.
     * @param scoringAlgorithmCreator The scoring algorithm creator. Given the input LTSs and appropriate combiners,
     *     creates a suitable algorithm.
     */
    public DynamicLTSScorer(U lhs, U rhs, Combiner<S> statePropertyCombiner, Combiner<T> transitionPropertyCombiner,
            BiFunction<U, U, BiFunction<Combiner<S>, Combiner<T>, SimilarityScorer<S, T, U>>> scoringAlgorithmCreator)
    {
        super(lhs, rhs, statePropertyCombiner, transitionPropertyCombiner, scoringAlgorithmCreator);
    }

    private static final <S, T, U extends LTS<S, T>> SimilarityScorer<S, T, U> defaultScoringAlgorithmCreator(U lhs,
            U rhs, Combiner<S> statePropertyCombiner, Combiner<T> transitionPropertyCombiner)
    {
        int nrOfStates = Math.max(lhs.size(), rhs.size());

        if (nrOfStates <= 45) {
            return new WalkinshawGlobalLTSScorer<>(lhs, rhs, statePropertyCombiner, transitionPropertyCombiner);
        } else if (nrOfStates <= 500) {
            return new WalkinshawLocalLTSScorer<>(lhs, rhs, statePropertyCombiner, transitionPropertyCombiner, 5);
        } else {
            return new WalkinshawLocalLTSScorer<>(lhs, rhs, statePropertyCombiner, transitionPropertyCombiner, 1);
        }
    }
}
