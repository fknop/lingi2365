package minicp.search.selector.value;

import minicp.engine.core.IntVar;
import minicp.search.SearchOnSuccess;
import minicp.search.branching.AbstractBranching;

public class LastSuccessOrdering implements ValueSelector, SearchOnSuccess<IntVar> {

    private ValueSelector selector = null;
    private int[] lastValues;
    private boolean assigned[];

    private long[][] timestamps;
    private long counter = 0L;


    public LastSuccessOrdering(IntVar[] x, AbstractBranching<IntVar> branching, ValueSelector selector) {
        this.selector = selector;
        branching.registerSuccessListener(this);

        timestamps = new long[x.length][];

        lastValues = new int[x.length];
        assigned = new boolean[x.length];
        for (int i = 0; i < x.length; ++i) {
            lastValues[i] = x[i].getMin();
            assigned[i] = false;
            timestamps[i] = new long[x[i].getMax() - x[i].getMin() + 1];
        }
    }

    public LastSuccessOrdering(IntVar[] x, AbstractBranching<IntVar> branching) {
        this(x, branching, new MinValue());
    }

    private int mapValue(IntVar x, int value) {
        return value - x.getMin();
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
