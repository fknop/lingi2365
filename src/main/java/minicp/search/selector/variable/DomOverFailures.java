package minicp.search.selector.variable;

import minicp.engine.core.Constraint;
import minicp.engine.core.IntVar;

import java.util.List;

public class DomOverFailures extends AbstractVariableSelector<IntVar> implements VariableEvaluator<IntVar>, VariableFilter<IntVar> {
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
        return (double) x[i].getSize() / getWeight(x[i]);
    }

    private double getWeight(IntVar x) {
        List<Constraint> constraints = x.getConstraints();
        int size = constraints.size();
        double sum = 1;
        for (int i = 0; i < size; ++i) {
            sum += constraints.get(i).getWeight();
        }

        return sum;
    }
}
