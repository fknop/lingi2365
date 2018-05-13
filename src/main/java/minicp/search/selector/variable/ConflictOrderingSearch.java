package minicp.search.selector.variable;

import minicp.engine.core.IntVar;
import minicp.search.SearchOnFailure;
import minicp.search.branching.AbstractBranching;

import java.util.Arrays;
import java.util.HashSet;


public class ConflictOrderingSearch extends AbstractVariableSelector<IntVar> implements VariableFilter<IntVar>,  VariableEvaluator<IntVar>, SearchOnFailure<IntVar> {

    private VariableEvaluator<IntVar> evaluator;
    private IntVar[] x;
    private long[] timestamps;
    private long counter = 0L;
    private boolean[] conflictedVariables;

    private long RESTART_TRESHOLD = 1000L;

    public ConflictOrderingSearch(IntVar[] x, AbstractBranching<IntVar> branching) {
        this(x, branching, new DomDivDegree());
    }

    public ConflictOrderingSearch(IntVar[] x, AbstractBranching<IntVar> branching, VariableEvaluator<IntVar> evaluator) {
        this.evaluator = evaluator;
        this.x = x;
        this.timestamps = new long[x.length];
        conflictedVariables = new boolean[x.length];
        for (int i = 0; i < x.length; ++i) {
            timestamps[i] = 0L;
            conflictedVariables[i] = false;
        }

        branching.registerFailureListener(this);
    }


    @Override
    public void onFailure(IntVar[] x, int index, int value) {
        ++counter;
        timestamps[index] = counter;
        conflictedVariables[index] = true;
    }

    private void reset() {
        for (int i = 0; i < timestamps.length; ++i) {
            timestamps[i] = 0L;
            conflictedVariables[i] = false;
        }
        counter = 0L;
//            RESTART_TRESHOLD = Math.min(RESTART_TRESHOLD * 2, 100000);
    }

    private boolean allConflictedBound() {
        boolean allBound = true;
        for (int i = 0; i < x.length; ++i) {
            if (conflictedVariables[i] && !x[i].isBound()) {
                allBound = false;
                break;
            }
        }

        return allBound;
    }

    @Override
    public int getVariable(IntVar[] x) {
        if (counter == 0L || allConflictedBound()) {
            if (counter > 0L) {
                reset();
            }

            return VariableSelector.selectMinVariable(x, this, evaluator, tieBreaker);
        }
        else {
            return VariableSelector.selectMinVariable(x, this, this, tieBreaker);
        }
    }

    @Override
    public double evaluate(IntVar[] x, int i) {
        if (counter == 0L) {
            return evaluator.evaluate(x, i);
        }
        else {
            return -timestamps[i];
        }
    }

    @Override
    public boolean take(IntVar variable) {
        return !variable.isBound();
    }
}
