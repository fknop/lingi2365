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
        super(x[0].getSolver());
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
            x[i].propagateOnBind(this);

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

        for (int i = 0; i < x.length;  ++i) {
            System.out.println(dest[i]);
            System.out.println(orig[i]);
            System.out.println(lengthToDest[i]);
        }
        System.out.println("end");
    }


    private void bind(int i) throws InconsistencyException {
        int succ = x[i].getMin();

//        lengthToDest[i].setValue(1 + lengthToDest[succ].getValue());

        for (int j = 0; j < x.length; ++j) {
            if (dest[j].getValue() == i) {
                dest[j].setValue(dest[succ].getValue());
                orig[i].setValue(orig[j].getValue());
                lengthToDest[j].setValue(lengthToDest[succ].getValue() + lengthToDest[j].getValue());
            }
        }
    }

    @Override
    public void propagate() throws InconsistencyException {

        System.out.println("propagate");

        boolean allBound = true;
        int j = 0;
        while (allBound && j < x.length) {
            if (!x[j].isBound()) {
                allBound = false;
            }
            j++;
        }

        if (allBound) {
            if (x.length == 1 && lengthToDest[0].getValue() != 0) {
                throw new InconsistencyException();
            }

            if (x.length > 1 && lengthToDest[0].getValue() != x.length) {
                throw new InconsistencyException();
            }
        }
    }
}
