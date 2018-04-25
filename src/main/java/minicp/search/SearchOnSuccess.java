package minicp.search;

import minicp.engine.core.Variable;

public interface SearchOnSuccess<V extends Variable> {
    void onSuccess(V[] x, int index, int value);
}
