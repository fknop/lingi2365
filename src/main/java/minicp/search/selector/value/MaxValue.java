package minicp.search.selector.value;

import minicp.engine.core.IntVar;

public class MaxValue implements ValueSelector {
    @Override
    public int getValue(IntVar[] x, int i) {
        return x[i].getMax();
    }
}
