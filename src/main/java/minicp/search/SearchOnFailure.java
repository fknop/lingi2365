package minicp.search;

import minicp.engine.core.Variable;

public interface SearchOnFailure<V extends Variable> {
    void onFailure(V[] x, int index, int value);
}
