package minicp.util;

import minicp.engine.core.IntVar;

public class IntVarPair {

    public IntVar variable;
    public int index;

    public IntVarPair(IntVar variable, int index) {
        this.variable = variable;
        this.index = index;
    }
}
