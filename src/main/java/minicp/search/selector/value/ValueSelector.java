package minicp.search.selector.value;

import minicp.engine.core.IntVar;
import minicp.engine.core.Variable;
import minicp.search.selector.variable.VariableEvaluator;
import minicp.search.selector.variable.VariableFilter;

@FunctionalInterface
public interface ValueSelector {
    int getValue(IntVar var);
}
