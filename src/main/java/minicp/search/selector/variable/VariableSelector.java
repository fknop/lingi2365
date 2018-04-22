package minicp.search.selector.variable;

import minicp.engine.core.Variable;

public interface VariableSelector<V extends Variable> {
    int getVariable(V[] x);

    static <V extends Variable> int selectMinVariable(V[] variables, VariableFilter<V> filter, VariableEvaluator<V> evaluator) {
        int variable = -1;
        double min = Double.MAX_VALUE;

        for (int i = 0; i < variables.length; ++i) {
            V v = variables[i];
            if (filter.take(v)) {
                double value = evaluator.evaluate(variables, i);
                if (variable == -1 || (value < min)) {
                    variable = i;
                    min = value;
                }
            }
        }

        return variable;
    }
}
