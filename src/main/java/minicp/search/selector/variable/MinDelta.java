package minicp.search.selector.variable;

import minicp.engine.core.IntVar;

public class MinDelta implements VariableFilter<IntVar>, VariableSelector<IntVar>, VariableEvaluator<IntVar> {

    @Override
    public int getVariable(IntVar[] x) {
        return VariableSelector.selectMinVariable(x, this, this);

    }

    @Override
    public double evaluate(IntVar[] x, int i) {
        return x[i].getMax() - x[i].getMin();
    }

    @Override
    public boolean take(IntVar variable) {
        return !variable.isBound();
    }
}
