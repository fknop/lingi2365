package minicp.search.selector.variable;

import minicp.engine.core.IntVar;
import minicp.search.SearchOnFailure;
import minicp.search.branching.AbstractBranching;


public class LastConflict implements VariableFilter<IntVar>, VariableSelector<IntVar>, VariableEvaluator<IntVar>, SearchOnFailure<IntVar> {

    private VariableEvaluator<IntVar> evaluator;
    private IntVar[] x;
    private int lastConflict = -1;

    public LastConflict(IntVar[] x, AbstractBranching<IntVar> branching) {
        this(x, branching, new DomDivDegree());
    }

    public LastConflict(IntVar[] x, AbstractBranching<IntVar> branching, VariableEvaluator<IntVar> evaluator) {
        this.evaluator = evaluator;
        this.x = x;
        branching.registerFailureListener(this);
    }


    @Override
    public void onFailure(IntVar[] x, int index, int value) {
        lastConflict = index;
    }

    @Override
    public int getVariable(IntVar[] x) {
        return VariableSelector.selectMinVariable(x, this, this);
    }

    @Override
    public double evaluate(IntVar[] x, int i) {
        if (lastConflict == i) {
            return Double.NEGATIVE_INFINITY;
        }
        else {
            return evaluator.evaluate(x, i);
        }
    }

    @Override
    public boolean take(IntVar variable) {
        return !variable.isBound();
    }
}
