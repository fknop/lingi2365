package minicp.search.selector.variable;

import minicp.engine.core.Variable;
import minicp.util.IntArrayList;

import java.util.Random;

public interface VariableSelector<V extends Variable> {
    int getVariable(V[] x);


    static Random rand = new Random(0);
    static IntArrayList bests = new IntArrayList();

    static <V extends Variable> int selectMinVariable(V[] variables, VariableFilter<V> filter, VariableEvaluator<V> evaluator) {
        bests.clear();

        double min = Double.MAX_VALUE;

        for (int i = 0; i < variables.length; ++i) {
            V v = variables[i];
            if (filter.take(v)) {
                double value = evaluator.evaluate(variables, i);
                if (bests.isEmpty() || (value < min)) {
                    bests.clear();
                    bests.add(i);
                    min = value;
                }
                else if (value == min) {
                    bests.add(i);
                }
            }
        }


        if (bests.isEmpty()) {
            return -1;
        }

        if (bests.size() == 1) {
            return bests.get(0);
        }

        int i = rand.nextInt(bests.size());
        return bests.get(i);
    }
}
