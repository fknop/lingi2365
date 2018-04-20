/*
 * mini-cp is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License  v3
 * as published by the Free Software Foundation.
 *
 * mini-cp is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY.
 * See the GNU Lesser General Public License  for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with mini-cp. If not, see http://www.gnu.org/licenses/lgpl-3.0.en.html
 *
 * Copyright (c)  2017. by Laurent Michel, Pierre Schaus, Pascal Van Hentenryck
 */


package minicp.engine.constraints;

import static minicp.cp.Factory.*;
import minicp.engine.core.Constraint;
import minicp.engine.core.IntVar;
import minicp.reversible.ReversibleInt;
import minicp.util.InconsistencyException;
import minicp.util.NotImplementedException;

public class Circuit extends Constraint {

    private final IntVar [] x;
    private final ReversibleInt [] dest;
    private final ReversibleInt [] orig;
    private final ReversibleInt [] lengthToDest;

    /**
     * x represents an Hamiltonian circuit on the cities {0..x.length-1}
     * where x[i] is the city visited after city i
     * @param x
     */
    public Circuit(IntVar [] x) {
        super(x[0].getSolver(), x.length);

        registerVariable(x);

        this.x = x;
        dest = new ReversibleInt[x.length];
        orig = new ReversibleInt[x.length];
        lengthToDest = new ReversibleInt[x.length];
        for (int i = 0; i < x.length; i++) {
            dest[i] = new ReversibleInt(cp.getTrail(), i);
            orig[i] = new ReversibleInt(cp.getTrail(), i);
            lengthToDest[i] = new ReversibleInt(cp.getTrail(),0);
        }
    }


    @Override
    public void post() throws InconsistencyException {
        cp.post(allDifferent(x));

        for (int i = 0; i < x.length; ++i) {
            int k = i;
            x[i].whenBind(() -> this.bind(k));

            if (x[i].isBound()) {
                bind(i);
            }
            // remove bounds
            x[i].removeBelow(0);
            x[i].removeAbove(x.length - 1);

            // remove selfloops
            if (x.length > 1) {
                x[i].remove(i);
            }
        }
    }


    private void bind(int i) throws InconsistencyException {

        // Successor of bound variable
        int succ = x[i].getMin();

        // Destination of successor of bound variable
        int d = dest[succ].getValue();

        // Origin of bound variable
        // Might be itself
        int origin = orig[i].getValue();

        // The destination of the origin becomes the destination of the successor
        dest[origin].setValue(d);

        // The origin of the destination becomes the origin of the bound variable
        orig[d].setValue(origin);

        // The total length from the origin to the new destination
        // Current length from origin to i + the length from succ to its destination + the length from i to succ (1)
        lengthToDest[origin].setValue(lengthToDest[origin].getValue() + lengthToDest[succ].getValue() + 1);

        int length = lengthToDest[origin].getValue();

        // If the path is not yet a circuit, we remove the origin as potential destination to avoid
        // subtours
        if (length < x.length - 1) {
            x[d].remove(origin);
        }
    }
}
