package minicp.search.selector.variable;

import minicp.engine.core.IntVar;

public class MaxRegret implements VariableFilter<IntVar>, VariableSelector<IntVar>, VariableEvaluator<IntVar> {

    int[] domain;

    @Override
    public int getVariable(IntVar[] x) {
        return VariableSelector.selectMinVariable(x, this, this);

    }

    @Override
    public double evaluate(IntVar[] x, int index) {
        IntVar xi = x[index];
        if (domain == null || domain.length < xi.getSize()) {
            domain = new int[xi.getSize()];
        }

        int min = xi.getMin();
        int second = Integer.MAX_VALUE;

        int size = xi.fillArray(domain);

        for (int i = 0; i < size; ++i) {
            if (domain[i] < second && domain[i] > min) {
                second = domain[i];
            }
        }

        return -(second - min);
    }

    @Override
    public boolean take(IntVar variable) {
        return !variable.isBound();
    }
}
