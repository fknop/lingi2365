package minicp.search.branching;

import minicp.engine.core.IntVar;
import minicp.search.Choice;
import minicp.search.SearchOnFailure;
import minicp.search.SearchOnSuccess;
import minicp.search.selector.value.ValueSelector;
import minicp.search.selector.variable.VariableSelector;
import minicp.util.InconsistencyException;

import static minicp.cp.Factory.*;

public class FirstFailBranching extends AbstractBranching<IntVar> {

    private VariableSelector<IntVar> varSelector;
    private ValueSelector valSelector;
    private IntVar[] x;

    public FirstFailBranching(IntVar[] x, VariableSelector<IntVar> varSelector, ValueSelector valSelector) {
        this.x = x;
        this.varSelector = varSelector;
        this.valSelector = valSelector;
    }

    public FirstFailBranching(IntVar[] x) {
        this(x, null, null);
    }

    public void setVariableSelector(VariableSelector<IntVar> varSelector) {
        this.varSelector = varSelector;
    }

    public void setValueSelector(ValueSelector valSelector) {
        this.valSelector = valSelector;
    }

    @Override
    public Choice branch() {

        if (varSelector == null) {
            throw new RuntimeException("No variable selector defined for branching");
        }

        if (valSelector == null) {
            throw new RuntimeException("No value selector defined for branching");
        }

        return () -> {
            int index = varSelector.getVariable(x);
            if (index == -1) {
                return LEAF;
            }

            IntVar var = x[index];

            int value = valSelector.getValue(x, index);
            return Branching.branch(
                () -> {
                    try {
                        equal(var, value);
                        notifySuccessListeners(x, index, value);
                    }
                    catch (InconsistencyException e) {
                        notifyFailureListeners(x, index, value);
                        throw e;
                    }
                },
                () -> {
                    try {
                        notEqual(var, value);
                    }
                    catch (InconsistencyException e) {
                        notifyFailureListeners(x, index, value);
                        throw e;
                    }
                }
            );
        };
    }
}
