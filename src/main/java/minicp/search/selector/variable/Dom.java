package minicp.search.selector.variable;

import minicp.engine.core.IntVar;

public class Dom implements VariableSelector<IntVar>, VariableEvaluator<IntVar>, VariableFilter<IntVar> {

    private boolean breakTies = true;
    public Dom(boolean breakTies) {
        this.breakTies = breakTies;
    }

    public Dom() {}

    @Override
    public int getVariable(IntVar[] variables) {
        return VariableSelector.selectMinVariable(variables, this, this, breakTies);
    }

    @Override
    public boolean take(IntVar variable) {
        return !variable.isBound();
    }

    @Override
    public double evaluate(IntVar[] x, int i) {
        return x[i].getSize();
    }
}
