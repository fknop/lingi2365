package minicp.search.selector.variable;

import minicp.engine.core.IntVar;

public class DomPlusDegree extends AbstractVariableSelector<IntVar> implements VariableEvaluator<IntVar>, VariableFilter<IntVar> {
    @Override
    public int getVariable(IntVar[] variables) {
        return VariableSelector.selectMinVariable(variables, this, this, tieBreaker);
    }

    @Override
    public boolean take(IntVar variable) {
        return !variable.isBound();
    }

    @Override
    public double evaluate(IntVar[] x, int i) {
        return x[i].getSize() + x[i].getDegree();
    }
}
