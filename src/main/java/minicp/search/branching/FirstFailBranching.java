package minicp.search.branching;

import minicp.engine.core.IntVar;
import minicp.search.Choice;
import minicp.search.selector.value.ValueSelector;
import minicp.search.selector.variable.VariableSelector;

import static minicp.cp.Factory.equal;
import static minicp.cp.Factory.notEqual;

public class FirstFailBranching implements Branching<IntVar> {

    @Override
    public Choice branch(IntVar[] x, VariableSelector<IntVar> selector, ValueSelector valueSelector) {
        return () -> {
            int index = selector.getVariable(x);
            if (index == -1) {
                return LEAF;
            }

            IntVar var = x[index];

            int value = valueSelector.getValue(var);
            return Branching.branch(
                () -> equal(var, value),
                () -> notEqual(var, value)
            );
        };
    }
}
