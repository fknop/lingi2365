package minicp.search.selector.variable;

import minicp.engine.core.IntVar;

import java.util.Random;

public class RandomVariable implements VariableSelector<IntVar>, VariableEvaluator<IntVar>, VariableFilter<IntVar> {

    private Random rand = new Random(0);

    @Override
    public int getVariable(IntVar[] x) {
        return VariableSelector.selectMinVariable(x, this, this);
    }

    @Override
    public double evaluate(IntVar[] x, int i) {
        return rand.nextDouble();
    }

    @Override
    public boolean take(IntVar variable) {
        return !variable.isBound();
    }
}
