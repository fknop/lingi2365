package minicp.search.branching;

import minicp.engine.core.Variable;
import minicp.search.SearchOnFailure;
import minicp.search.SearchOnSuccess;

import java.util.ArrayList;

public abstract  class AbstractBranching<V extends Variable> implements Branching<V> {

    private ArrayList<SearchOnSuccess<V>> successListeners = new ArrayList<>();
    private ArrayList<SearchOnFailure<V>> failureListeners = new ArrayList<>();

    public void registerFailureListener(SearchOnFailure<V> listener) {
        this.failureListeners.add(listener);
    }

    public void registerSuccessListener(SearchOnSuccess<V> listener) {
        this.successListeners.add(listener);
    }

    protected void notifySuccessListeners(V[] x, int index, int value) {
        int size = successListeners.size();
        for (int i = 0; i < size; ++i) {
            successListeners.get(i).onSuccess(x, index, value);
        }
    }

    protected void notifyFailureListeners(V[] x, int index, int value) {
        int size = failureListeners.size();
        for (int i = 0; i < size; ++i) {
            failureListeners.get(i).onFailure(x, index, value);
        }
    }
}
