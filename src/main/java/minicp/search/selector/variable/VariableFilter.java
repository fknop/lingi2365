package minicp.search.selector.variable;

import minicp.engine.core.Variable;

@FunctionalInterface
public interface VariableFilter<V extends Variable> {
    boolean take(V variable);
}
