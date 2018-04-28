package minicp.search.selector.variable;

import minicp.engine.core.Constraint;
import minicp.engine.core.IntVar;
import minicp.reversible.ReversibleInt;
import minicp.search.selector.value.ValueSelector;

import java.util.List;

public class WDeg implements VariableFilter<IntVar>, VariableSelector<IntVar>, VariableEvaluator<IntVar> {


    private ReversibleInt[][] unassigned = null;
    private VariableEvaluator<IntVar> fallback = new DomDivDegree();
    private final static int SIZE_TRESHOLD = 2;

    private void setup(IntVar[] x) {
        unassigned = new ReversibleInt[x.length][];
        for (int i = 0; i < x.length; ++i) {
            List<Constraint> constraints = x[i].getConstraints();
            int size = constraints.size();

            unassigned[i] = new ReversibleInt[size];
            for (int j = 0; j < size; ++j) {
                unassigned[i][j] = new ReversibleInt(x[i].getSolver().getTrail(), constraints.get(j).getVars().size());
            }

            final int k = i;
            x[i].whenBind(() -> {
                for (int j = 0; j < size; ++j) {
                    unassigned[k][j].decrement();
                }
            });
        }
    }

    @Override
    public int getVariable(IntVar[] x) {

        if (unassigned == null) {
            setup(x);
        }
//
//        int max = Integer.MIN_VALUE;
//        for (int i = 0; i < x.length; ++i) {
//            int size = x[i].getSize();
//            if (size > max) {
//                max = size;
//            }
//        }

//        return VariableSelector.selectMinVariable(x, this, max > SIZE_TRESHOLD ? fallback : this);
        return VariableSelector.selectMinVariable(x, this, this);
    }
    @Override
    public boolean take(IntVar variable) {
        return !variable.isBound();
    }

    @Override
    public double evaluate(IntVar[] x, int i) {

        if (unassigned == null) {
            setup(x);
        }

        return (double) x[i].getSize() / getWeight(x, i);
    }

    private double getWeight(IntVar[] x, int index) {
        IntVar xi = x[index];
        List<Constraint> constraints = xi.getConstraints();
        int size = constraints.size();
        double sum = 1;
        for (int i = 0; i < size; ++i) {
            if (unassigned[index][i].getValue() > 1) {
                sum += constraints.get(i).getWeight();
            }
        }

        return sum;
    }
}
