package minicp.search.selector.variable;

import minicp.engine.core.IntVar;
import minicp.engine.core.Variable;

@FunctionalInterface
public interface VariableEvaluator<V extends Variable> {
    double evaluate(V[] x, int i);
}
