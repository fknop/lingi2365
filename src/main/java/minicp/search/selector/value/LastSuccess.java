package minicp.search.selector.value;

import minicp.engine.core.IntVar;
import minicp.search.SearchOnSuccess;
import minicp.search.branching.AbstractBranching;

public class LastSuccess implements ValueSelector, SearchOnSuccess<IntVar> {

    private ValueSelector selector = null;
    private int[] lastValues;
    private boolean assigned[];


    public LastSuccess(IntVar[] x, AbstractBranching<IntVar> branching, ValueSelector selector) {
        this.selector = selector;
        branching.registerSuccessListener(this);
        lastValues = new int[x.length];
        assigned = new boolean[x.length];
        for (int i = 0; i < x.length; ++i) {
            lastValues[i] = x[i].getMin();
            assigned[i] = false;
        }
    }

    public LastSuccess(IntVar[] x, AbstractBranching<IntVar> branching) {
        this(x, branching, new MinValue());
    }


    public int getValue(IntVar[] x, int index) {

        if (assigned[index]) {
            int value = lastValues[index];
            if (!x[index].contains(value)) {
                return selector.getValue(x, index);
            }
            else {
                return value;
            }
        }
        else {
            return selector.getValue(x, index);
        }
    }

    @Override
    public void onSuccess(IntVar[] x, int index, int value) {
        lastValues[index] = value;
        assigned[index] = true;
    }
}
