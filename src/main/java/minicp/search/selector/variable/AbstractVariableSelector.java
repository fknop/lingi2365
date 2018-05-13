package minicp.search.selector.variable;

import minicp.engine.core.Variable;

public abstract  class AbstractVariableSelector<V extends Variable> implements VariableSelector<V> {

    protected TieBreaker tieBreaker = new RandomTieBreaker();

    public void setTieBreaker(TieBreaker tieBreaker) {
        this.tieBreaker = tieBreaker;
    }
}
