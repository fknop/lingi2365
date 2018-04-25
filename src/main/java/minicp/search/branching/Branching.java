package minicp.search.branching;

import minicp.engine.core.Variable;
import minicp.search.Choice;
import minicp.search.SearchOnFailure;
import minicp.search.SearchOnSuccess;
import minicp.search.selector.value.ValueSelector;
import minicp.search.selector.variable.VariableSelector;

@FunctionalInterface
public interface Branching<V extends Variable> {
    Choice branch();

    static Branch[] LEAF = new Branch[0];
    static Branch[] branch(Branch... alternatives) {
        return alternatives;
    }
}
